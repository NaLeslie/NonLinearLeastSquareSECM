/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fitting;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import structures.Position;
import structures.SECMImage;

/**
 *
 * @author Nathaniel
 */
public class ImageAlign {
    /**
     * 
     * @param obs
     * @param sim
     * @return 
     */
    public static Position centreImages(SECMImage obs, SECMImage sim){
        int Max_iter = 15;
        int iterations = 0;
        
        SECMImage norm_obs = obs.getNormalized();
        SECMImage norm_sim = sim.getNormalized();
        
        System.out.println("obs_Xmin: " + obs.getXMin() + ", obs_Xmax: " + obs.getXMax() + ", sim_Xmin: " + sim.getXMin() + ", sim_Xmax: " + sim.getXMax());
        
        double x_offs = ((obs.getXMax() + obs.getXMin()) - (sim.getXMax() + sim.getXMin())) / 2.0;
        double y_offs = ((obs.getYMax() + obs.getYMin()) - (sim.getYMax() + sim.getYMin())) / 2.0;
        
        double x_disp = 0.01 * (sim.getXMax() - sim.getXMin());
        double y_disp = 0.01 * (sim.getYMax() - sim.getYMin());
        
        double new_x_offs = x_offs;
        double new_y_offs = y_offs;
        
        SECMImage displaced_obs = norm_obs.getOffset(-x_offs, -y_offs);
        SECMImage difference = displaced_obs.subtract(norm_sim, SECMImage.OUT_OF_BOUNDS_EXTRAPOLATE);
        double[] residuals = difference.linearize();
        double SSR = squareSum(residuals);
        double oldSSR;
        double rel_change_SSR;
        System.out.println("x: " + x_offs + ", y: " + y_offs + ", SSR: " + SSR);
        do{
            oldSSR = SSR;
            x_offs = new_x_offs;
            y_offs = new_y_offs;
            
            
            SECMImage step_neg = norm_obs.getOffset(-x_offs - x_disp, -y_offs);
            SECMImage step_pos = norm_obs.getOffset(-x_offs + x_disp, -y_offs);
            
            SECMImage partial_derivative = step_pos.subtract(norm_sim, SECMImage.OUT_OF_BOUNDS_EXTRAPOLATE).subtract(step_neg.subtract(norm_sim, SECMImage.OUT_OF_BOUNDS_EXTRAPOLATE), SECMImage.OUT_OF_BOUNDS_EXTRAPOLATE);
            double[] x_partial = partial_derivative.linearize();
            
            step_neg = norm_obs.getOffset(-x_offs, -y_offs - y_disp);
            step_pos = norm_obs.getOffset(-x_offs, -y_offs + y_disp);
            
            partial_derivative = step_pos.subtract(norm_sim, SECMImage.OUT_OF_BOUNDS_EXTRAPOLATE).subtract(step_neg.subtract(norm_sim, SECMImage.OUT_OF_BOUNDS_EXTRAPOLATE), SECMImage.OUT_OF_BOUNDS_EXTRAPOLATE);
            double[] y_partial = partial_derivative.linearize();
            
            Array2DRowRealMatrix Jacobian = new Array2DRowRealMatrix(x_partial.length, 2);
            for(int i = 0; i < x_partial.length; i++){
                Jacobian.setEntry(i, 0, x_partial[i] / 2.0 / x_disp);
                Jacobian.setEntry(i, 1, y_partial[i] / 2.0 / y_disp);
            }
            
            ArrayRealVector resid = new ArrayRealVector(residuals);
            
            double[] changes = NonlinearLeastSquares.DeltaCs(Jacobian, resid);
            new_x_offs = x_offs + changes[0];
            new_y_offs = y_offs + changes[1];
            
            displaced_obs = norm_obs.getOffset(-new_x_offs, -new_y_offs);
            difference = displaced_obs.subtract(norm_sim, SECMImage.OUT_OF_BOUNDS_EXTRAPOLATE);
            residuals = difference.linearize();
            SSR = squareSum(residuals);
            System.out.println("x: " + new_x_offs + ", y: " + new_y_offs + ", SSR: " + SSR);

            rel_change_SSR = (oldSSR - SSR)/SSR;
            iterations ++;
        }while(rel_change_SSR > 0.0001 && iterations < Max_iter);
        
        return new Position(x_offs,y_offs,0);
    }
    
    /**
     * 
     * @param residuals
     * @return 
     */
    private static double squareSum(double[] residuals){
        double sum = 0.0;
        for(double r:residuals){
            sum += r*r;
        }
        return sum;
    }
    
}
