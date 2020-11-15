package fitting;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

/**
 *
 * @author Nathaniel
 */
public class NonlinearLeastSquares {
    
    public static ArrayRealVector Residual(double[] observed_data, double[][] iteration_data){
        double[] residual = new double[observed_data.length];
        
        for(int r = 0; r < observed_data.length; r++){
            residual[r] = iteration_data[r][0] - observed_data[r];
        }
        return new ArrayRealVector(residual);
    }
    
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
    
    public static double[] DeltaCs(Array2DRowRealMatrix jacobian, ArrayRealVector residuals){
        RealMatrix JT = jacobian.transpose();
        RealMatrix JTJ = JT.multiply(jacobian);
        RealMatrix inv_JTJ = MatrixUtils.inverse(JTJ);
        RealMatrix pseudoinverse = inv_JTJ.multiply(JT);
        return pseudoinverse.operate(residuals).toArray();
    }
}
