/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
        parameter_names = paramnames;
        lambda = 0;
        sum_square_residuals = Double.NaN;
    }
    
    
    public LSIterationData(LSIterationData last_iteration, double[] jtj_diag, double lamb, int diagonal_policy){
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
        sum_square_residuals = 0.0;
    }
    
    public void applyShiftVector(RealVector shift_vector){
        for(int i = 0; i < params.length; i++){
            params[i] = params[i] + shift_vector.getEntry(i);
        }
    }
    
    public int iteration_number;
    
    public double[] params;
    
    public double[] jacobian_diagonal;
    
    public double lambda;
    
    public double sum_square_residuals;
    
    public String[] parameter_names;
    
    public String data_file_path;
    
    public static final int DIAGONAL_POLICY_BIGGEST = 0;
    
    public static final int DIAGONAL_POLICY_LAST_NONZERO = 1;
    
    public static final int DIAGONAL_POLICY_IGNORE_HISTORY = 2;
}
