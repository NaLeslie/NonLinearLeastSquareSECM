package structures;

import org.apache.commons.math3.linear.RealVector;

/**
 *
 * @author Nathaniel
 */
public class LSIterationData {
    
    /**
     * 
     * @param initial_guess 
     */
    public LSIterationData(double[] initial_guess){
        params = new double[initial_guess.length];
        System.arraycopy(initial_guess, 0, params, 0, initial_guess.length);
        jacobian_diagonal = new double[initial_guess.length];
        iteration_number = 0;
        parameter_names = new String[0];
        parameter_names[0] = "";
        lambda = 0;
        sum_square_residuals = Double.NaN;
    }
    
    /**
     * 
     * @param initial_guess
     * @param paramnames 
     */
    public LSIterationData(double[] initial_guess, String[] paramnames){
        params = new double[initial_guess.length];
        System.arraycopy(initial_guess, 0, params, 0, initial_guess.length);
        jacobian_diagonal = new double[initial_guess.length];
        iteration_number = 0;
        covariance = new double[initial_guess.length][initial_guess.length];
        degrees_of_freedom = 1;
        parameter_names = paramnames;
        lambda = 0;
        sum_square_residuals = Double.NaN;
    }
    
    /**
     * 
     * @param last_iteration
     * @param jtj_diag
     * @param covariance the covariance of the previous iteration
     * @param degrees_of_freedom the number of degrees of freedom of the last iteration
     * @param lamb
     * @param diagonal_policy 
     */
    public LSIterationData(LSIterationData last_iteration, double[] jtj_diag, double[][] covariance, int degrees_of_freedom, double lamb, int diagonal_policy){
        int numparams = last_iteration.params.length;
        params = new double[numparams];
        jacobian_diagonal = new double[numparams];
        for(int i = 0; i < numparams; i++){
            if(diagonal_policy == DIAGONAL_POLICY_BIGGEST && Math.abs(jtj_diag[i]) > Math.abs(last_iteration.jacobian_diagonal[i])){
                jacobian_diagonal[i] = jtj_diag[i];
            }
            else if(diagonal_policy == DIAGONAL_POLICY_LAST_NONZERO && Math.abs(jtj_diag[i]) > 0){
                jacobian_diagonal[i] = jtj_diag[i];
            }
            else{
                jacobian_diagonal[i] = last_iteration.jacobian_diagonal[i];
            }
            
            params[i] = last_iteration.params[i];
        }
        
        iteration_number = last_iteration.iteration_number + 1;
        parameter_names = last_iteration.parameter_names;
        lambda = lamb;
        this.covariance = covariance;
        this.degrees_of_freedom = degrees_of_freedom;
        sum_square_residuals = 0.0;
    }
    
    /**
     * 
     * @param shift_vector 
     */
    public void applyShiftVector(RealVector shift_vector){
        for(int i = 0; i < params.length; i++){
            params[i] = params[i] + shift_vector.getEntry(i);
        }
    }
    
    /**
     * the overall number of iterations that have taken place (note: an iteration can have more than one sub-iteration due to lambdas)
     */
    public int iteration_number;
    
    /**
     * The degrees of freedom for the iteration (number_of_data_points - number_of_parameters
     */
    public int degrees_of_freedom;
    
    /**
     * <p>The covariance matrix for this iteration. This covariance can be calculated from J<sup>T</sup>J by:</p>
     * <p>cov<sub>ij</sub>=J<sup>T</sup>J<sub>ij</sub> - average(J<sub>i</sub>)average(J<sub>j</sub>)</p>
     * 
     */
    public double[][] covariance;
    
    /**
     * The values for the parameters for this iteration
     */
    public double[] params;
    
    /**
     * The diagonal components of J<sup>T</sup>J
     */
    public double[] jacobian_diagonal;
    
    /**
     * the value of lambda that was used to produce the parameters for this iteration
     */
    public double lambda;
    
    /**
     * the sum of squares for this iteration
     */
    public double sum_square_residuals;
    
    /**
     * The names of the parameters being optimized
     */
    public String[] parameter_names;
    
    /**
     * The directory and file name for the file of the previous iteration 
     * (that is, the iteration responsible for producing this iteration's parameters)
     */
    public String data_file_path;
    
    /**
     * This diagonal policy means that the largest value between the Jacobian and the previous diagonal will be taken
     * <p>D<sup>T</sup>D = max(J<sup>T</sup>J, D<sup>T</sup>D(previous iteration))</p>
     */
    public static final int DIAGONAL_POLICY_BIGGEST = 0;
    
    /**
     * This diagonal policy means that D<sup>T</sup>D will be the non-zero diagonal components of J<sup>T</sup>J. 
     * If there is a zero in one of the elements, the corresponding DTD element will be taken from the previous iteration.
     */
    public static final int DIAGONAL_POLICY_LAST_NONZERO = 1;
    
    /**
     * This diagonal policy means that D<sup>T</sup>D will be the diagonal components of J<sup>T</sup>J for this iteration.
     */
    public static final int DIAGONAL_POLICY_IGNORE_HISTORY = 2;
}
