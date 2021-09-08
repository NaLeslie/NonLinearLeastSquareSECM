package fitting;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import structures.LSIterationData;

/**
 * <p>Static methods for handling <a href="https://en.wikipedia.org/wiki/Non-linear_least_squares">
 * non-linear least squares</a> regression.</p>
 * <p><b>ΔC</b>=(<b>J</b><sup>T</sup><b>J</b>)<sup>-1</sup><b>J</b><sup>T</sup><b>R</b></p>
 * <p>Where <b>C</b> is a vector representing parameters, <b>J</b> is the Jacobian matrix 
 * (partial derivative of the function with respect to the parameters at each point), and 
 * <b>R</b> is a vector representing the residuals at each point.</p>
 * @author Nathaniel Leslie
 */
public class NonlinearLeastSquares {
    
    /**
     * Creates the residual vector from the observed data and the 0<sup>th</sup> 
     * column of the iteration data array. Please note that <code>observed_data[i]</code> 
     * and <code>iteration_data[i][j]</code> must correspond to the same point in space.
     * @param observed_data the observed SECM image data that is to be fit. This 
     * is meant to be where the output from <code>io.Input.read_observed</code>
     * is ingested.
     * @param iteration_data the data from an iteration of non-linear least squares. 
     * <code>iteration_data[i][0]</code> is the simulated currents for parameters <var>c<sub>j</sub></var>. 
     * <code>iteration_data[i][j]</code> for <code>j &gt; 0</code> contains the 
     * partial derivatives of the simulated current with respect to <var>c<sub>j</sub></var> 
     * and is ignored by this function. This is meant to be where the output from 
     * <code>io.Input.read_fitting_iteration</code> is ingested.
     * @return returns the residuals vector. Residuals are calculated as 
     * <code>residual[i] = observed_data[i] - iteration_data[i][0]</code>
     */
    public static ArrayRealVector Residual(double[] observed_data, double[][] iteration_data){
        double[] residual = new double[observed_data.length];
        
        for(int r = 0; r < observed_data.length; r++){
            residual[r] = observed_data[r] - iteration_data[r][0];
        }
        return new ArrayRealVector(residual);
    }
    
    /**
     * Extracts the Jacobian matrix from the output of <code>io.Input.read_fitting_iteration</code>
     * @param iteration_data the data from an iteration of non-linear least squares. 
     * <code>iteration_data[i][0]</code> is the simulated currents for parameters <var>c<sub>j</sub></var> 
     * and is ignored by this function. <code>iteration_data[i][j]</code> for 
     * <code>j &gt; 0</code> contains the partial derivatives of the simulated 
     * current with respect to <var>c<sub>j</sub></var>. This is meant to be 
     * where the output from <code>io.Input.read_fitting_iteration</code> is ingested.
     * @return The Jacobian matrix for this step of the non-linear least-squares
     * iteration.
     */
    public static Array2DRowRealMatrix Jacobian(double[][] iteration_data){
        int rows = iteration_data.length;
        int cols = iteration_data[0].length - 1;
        Array2DRowRealMatrix jacob = new Array2DRowRealMatrix(rows, cols);
        for(int r = 0; r < rows; r++){
            for(int c = 0; c < cols; c++){
                jacob.setEntry(r, c, iteration_data[r][c+1]);
            }
        }
        return jacob;
    }
    
    /**
     * Computes the amount by which each parameter needs to change from this iteration to the next.
     * @param jacobian The Jacobian matrix. Computed by <code>Jacobian</code>.
     * @param residuals The residuals vector. Computed by <code>Residual</code>
     * @return <b>Δc</b> vector, where <var>c<sub>i+1</sub></var> = <var>c<sub>i</sub></var> + <var>Δc<sub>i</sub></var>
     */
    public static double[] DeltaCs(Array2DRowRealMatrix jacobian, ArrayRealVector residuals){
        RealMatrix JT = jacobian.transpose();
        RealMatrix JTJ = JT.multiply(jacobian);
        RealMatrix inv_JTJ = MatrixUtils.inverse(JTJ);
        RealMatrix pseudoinverse = inv_JTJ.multiply(JT);
        return pseudoinverse.operate(residuals).toArray();
    }
    
    /**
     * Computes the amount by which each parameter needs to change from this iteration to the next.
     * @param jacobian The Jacobian matrix. Computed by <code>Jacobian</code>.
     * @param lambda The Levenberg-Marquardt lambda parameter
     * @param use_diagonal Will use D<sup>T</sup>D if true, I if false when applying Levenberg-Marquardt
     * @param residuals The residuals vector. Computed by <code>Residual</code>
     * @return <b>Δc</b> vector, where <var>c<sub>i+1</sub></var> = <var>c<sub>i</sub></var> + <var>Δc<sub>i</sub></var>
     */
    public static double[] DeltaCs_LevenbergMarquardt(Array2DRowRealMatrix jacobian, double lambda, boolean use_diagonal, ArrayRealVector residuals){
        RealMatrix JT = jacobian.transpose();
        RealMatrix JTJ = JT.multiply(jacobian);
        RealMatrix inv_JTJ;
        if(use_diagonal){
            RealMatrix lDTD = lambda_DTD(JTJ, lambda);
            inv_JTJ = MatrixUtils.inverse(JTJ.add(lDTD));
        }
        else{
            RealMatrix lI = lambda_I(JTJ, lambda);
            inv_JTJ = MatrixUtils.inverse(JTJ.add(lI));
        }
        RealMatrix pseudoinverse = inv_JTJ.multiply(JT);
        return pseudoinverse.operate(residuals).toArray();
    }
    
    /**
     * Computes parameters and metadata for the next Lavanberg-Marquardt iteration.
     * @param last_iteration Metadata describing the old iteration
     * @param jacobian The Jacobian matrix. Computed by <code>Jacobian</code>.
     * @param lambda The Levenberg-Marquardt lambda parameter
     * @param use_diagonal Will use D<sup>T</sup>D (possibly computed by <code>lambda_DTD</code> depending on <code>diagonal_policy</code>) if true, I (computed by <code>lambda_I</code>) if false when applying Levenberg-Marquardt
     * @param residuals The residuals vector. Computed by <code>Residual</code>
     * @param diagonal_policy the policy that determines how the diagonal of past iterations' J<sup>T</sup>J influences D<sup>T</sup>D for this calculation
     * @return Metadata describing the new iteration
     */
    public static LSIterationData NextIteration(LSIterationData last_iteration, Array2DRowRealMatrix jacobian, double lambda, boolean use_diagonal, ArrayRealVector residuals, int diagonal_policy){
        RealMatrix JT = jacobian.transpose();
        RealMatrix JTJ = JT.multiply(jacobian);
        RealMatrix inv_JTJ;
        
        double[] diagonal = getDiagonal(JTJ);
        LSIterationData current_iteration = new LSIterationData(last_iteration, diagonal, lambda, diagonal_policy);
        
        if(use_diagonal){
            RealMatrix lDTD = lambda_DTD(current_iteration.jacobian_diagonal, lambda);
            inv_JTJ = MatrixUtils.inverse(JTJ.add(lDTD));
        }
        else{
            RealMatrix lI = lambda_I(JTJ, lambda);
            inv_JTJ = MatrixUtils.inverse(JTJ.add(lI));
        }
        RealMatrix pseudoinverse = inv_JTJ.multiply(JT);
        current_iteration.applyShiftVector(pseudoinverse.operate(residuals));
        return current_iteration;
    }
    
    /**
     * Fetches the diagonal elements of J<sup>T</sup>J
     * @param JTJ J<sup>T</sup>J
     * @return the diagonal elements of J<sup>T</sup>J
     */
    public static double[] getDiagonal(RealMatrix JTJ){
        int size = JTJ.getColumnDimension();
        double[] diag = new double[size];
        for(int i = 0; i < size; i++){
            diag[i] = JTJ.getEntry(i, i);
        }
        return diag;
    }
    
    /**
     * Creates a matrix where the diagonal elements are the diagonal elements of <code>diag</code> times lambda, and 0 elsewhere
     * @param diag the diagonal elements of J<sup>T</sup>J
     * @param lambda the Levenberg-Marquardt lambda parameter
     * @return a matrix where the diagonal elements are the diagonal elements of <code>diag</code> times lambda, and 0 elsewhere
     */
    public static RealMatrix lambda_DTD(double[] diag, double lambda){
        RealMatrix DTD = new Array2DRowRealMatrix(diag.length, diag.length);
        for(int i = 0; i < DTD.getColumnDimension(); i++){
            for(int j = 0; j < DTD.getRowDimension(); j++){
                if(i == j){
                    double value = diag[i] * lambda;
                    DTD.setEntry(j, i, value);
                } 
                else{
                    DTD.setEntry(j, i, 0.0);
                }
            }
        }
        return DTD;
    }
    
    /**
     * Creates a matrix where the diagonal elements are the diagonal elements of <code>JTJ</code> times lambda, and 0 elsewhere
     * @param JTJ J<sup>T</sup>J
     * @param lambda the Levenberg-Marquardt lambda parameter
     * @return a matrix where the diagonal elements are the diagonal elements of <code>JTJ</code> times lambda, and 0 elsewhere
     */
    public static RealMatrix lambda_DTD(RealMatrix JTJ, double lambda){
        RealMatrix DTD = JTJ.copy();
        for(int i = 0; i < DTD.getColumnDimension(); i++){
            for(int j = 0; j < DTD.getRowDimension(); j++){
                if(i == j){
                    double value = DTD.getEntry(j, i)*lambda;
                    DTD.setEntry(j, i, value);
                } 
                else{
                    DTD.setEntry(j, i, 0.0);
                }
            }
        }
        return DTD;
    }
    
    /**
     * Multiplies an identity matrix with the same dimensions as JTJ with lambda
     * @param JTJ matrix to which the result of the function will be added later. Exists only for the purpose of taking its size
     * @param lambda the Levenberg-Marquardt lambda parameter
     * @return lambda*I with the same size as JTJ
     */
    public static RealMatrix lambda_I(RealMatrix JTJ, double lambda){
        RealMatrix DTD = JTJ.copy();
        for(int i = 0; i < DTD.getColumnDimension(); i++){
            for(int j = 0; j < DTD.getRowDimension(); j++){
                if(i == j){
                    DTD.setEntry(j, i, lambda);
                } 
                else{
                    DTD.setEntry(j, i, 0.0);
                }
            }
        }
        return DTD;
    }
}
