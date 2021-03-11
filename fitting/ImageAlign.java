/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fitting;

import java.util.Stack;
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
    
    /**
     * 
     * @param obs
     * @return 
     */
    private static Position[] findCrossovers(SECMImage obs){
        double threshold = 0.2;
        SECMImage normalized = obs.getNormalized();
        double[][] signals = normalized.getDataCurrent();
        double[] x_coordinates = normalized.getDataXvals();
        double[] y_coordinates = normalized.getDataYvals();
        Stack <Position> crossings = new Stack();
        
        for(int y = 0; y < signals[0].length; y++){
            boolean last_above = false;
            Stack <Double> y_level_crossovers = new Stack();
            for(int x = 0; x < signals.length; x++){
                boolean above = signals[x][y] > threshold;
                if(last_above ^ above){
                    if(above){
                        y_level_crossovers.push(x_coordinates[x]);
                    }
                    else{
                        y_level_crossovers.push(x_coordinates[x - 1]);
                    }
                }
                last_above = above;
            }
            int crosses = y_level_crossovers.size(); //should be an even number.
            if(crosses == 2){
                crossings.push(new Position(y_level_crossovers.pop(), y_coordinates[y], 0.0));
                crossings.push(new Position(y_level_crossovers.pop(), y_coordinates[y], 0.0));
            }
            else if(crosses <= 3 || last_above){//last_above = true if odd number of crossings
            } else {
                double biggest = 0.0;
                double x1 = 0;
                double x2 = 0;
                for(int i = 1; i < crosses; i++){
                    double upper = y_level_crossovers.pop();
                    double lower = y_level_crossovers.pop();
                    double dist = Math.abs(upper - lower);
                    if(dist > biggest){
                        biggest = dist;
                        x1 = lower;
                        x2 = upper;
                    }
                }
                crossings.push(new Position(x1, y_coordinates[y], 0.0));
                crossings.push(new Position(x2, y_coordinates[y], 0.0));
            }
        }
        int len = crossings.size();
        Position[] solution = new Position[len];
        for(int i = 0; i < len; i++){
            solution[i] = crossings.pop();
        }
        return solution;
    }
    
    public static Position findCentre(SECMImage obs){
        int Max_iter = 15;
        int iterations = 0;
        Position[] crossings = findCrossovers(obs);
        
        double xsum = 0.0;
        double ysum = 0.0;
        for(int i = 0; i < crossings.length; i++){
            xsum += crossings[i].getX();
            ysum += crossings[i].getY();
        }
        //find the x-coordinate of the centre
        double numpoints = (double)crossings.length;
        double x0 = xsum / numpoints;
        //first guess for the y coordinate of the centre
        double y0 = ysum / numpoints;
        
        //transform the x-coordinates to the distance in x from the x-centre
        double rsum = 0.0;
        for(int i = 0; i < crossings.length; i++){
            double xpos = crossings[i].getX();
            double ypos = crossings[i].getY();
            crossings[i] = new Position(Math.abs(xpos - x0), ypos, 0.0);
            rsum += Math.sqrt((xpos - x0)*(xpos - x0) + (ypos - y0)*(ypos - y0));
        }
        double r = rsum / numpoints;
        
        double[] residuals = getResiduals(crossings, r, y0);
        double SSR = squareSum(residuals);
        double oldSSR;
        double rel_change_SSR;
        double old_r;
        double old_y0;
        System.out.println("y0: " + y0 + ", r: " + r + ", SSR: " + SSR);
        do{
            oldSSR = SSR;
            old_r = r;
            old_y0 = y0;
            
            Array2DRowRealMatrix Jacobian = new Array2DRowRealMatrix(crossings.length, 2);
            for(int i = 0; i < crossings.length; i++){
                Jacobian.setEntry(i, 0, dxBydr(crossings[i].getY(), r, y0));//r
                Jacobian.setEntry(i, 1, dxBydy0(crossings[i].getY(), r, y0));//y0
            }
            
            ArrayRealVector resid = new ArrayRealVector(residuals);
            
            double[] changes = NonlinearLeastSquares.DeltaCs(Jacobian, resid);
            r = old_r + changes[0];
            y0 = old_y0 + changes[1];
            
            residuals = getResiduals(crossings, r, y0);
            SSR = squareSum(residuals);
            System.out.println("y0: " + y0 + ", r: " + r + ", SSR: " + SSR);

            rel_change_SSR = (oldSSR - SSR)/SSR;
            iterations ++;
        }while(rel_change_SSR > 0.0001 && iterations < Max_iter);
        
        return new Position(x0, old_y0, 0.0);
    }
    
    private static double circleXCoord(double y, double r, double y0){
        double y_y0_sq = (y - y0)*(y - y0);
        double r_sq = r*r;
        if(y_y0_sq < r_sq){
            return Math.sqrt(r_sq - y_y0_sq);
        }
        else{
            return 0.0;
        }
    }
    
    private static double dxBydr(double y, double r, double y0){
        double y_y0_sq = (y - y0)*(y - y0);
        double r_sq = r*r;
        if(y_y0_sq < r_sq){
            return r / Math.sqrt(r_sq - y_y0_sq);
        }
        else{
            return 0.0;
        }
    }
    
    private static double dxBydy0(double y, double r, double y0){
        double y_y0_sq = (y - y0)*(y - y0);
        double r_sq = r*r;
        if(y_y0_sq < r_sq){
            return (y - y0) / Math.sqrt(r_sq - y_y0_sq);
        }
        else{
            return 0.0;
        }
    }
    
    private static double[] getResiduals(Position[] data, double r, double y0){
        double[] residuals = new double[data.length];
        for(int i = 0; i < data.length; i++){
            double y = data[i].getY();
            residuals[i] = data[i].getX() - circleXCoord(y, r, y0);
        }
        return residuals;
    }
}
