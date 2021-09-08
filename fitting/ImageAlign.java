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
     * Returns the position of the centre of SECM image obs by performing a Gauss-Newton fit of the position of obs relative to sim that minimizes residuals.  
     * @param obs Experimental SECM image. Not centred at 0,0
     * @param sim Simulated SECM image. Centred at 0,0
     * @return the position of the centre of SECM image obs
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
     * Computes the sum of squares of the array
     * @param residuals the array holding the data to be squared and summed
     * @return the sum of squares of residuals
     */
    private static double squareSum(double[] residuals){
        double sum = 0.0;
        for(double r:residuals){
            sum += r*r;
        }
        return sum;
    }
    
    /**
     * Finds the points along x-linescans of obs that intersect with a 20% base-to-peak threshold.
     * This first takes obs and re-scales the currents such that they fall between 0 and 1.
     * Then scans along the x-direction and reports locations where the threshold 0.2 is crossed.
     * @param obs The SECM image that is to be analyzed.
     * @return the location of points points along x-linescans of obs that intersect with a 20% base-to-peak threshold. 
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
    
    /**
     * Finds the centre of obs by: 
     * <p>Finding the points along x-linescans of obs that intersect with a 20% base-to-peak threshold to a circle.</p>
     * <p>Middle x-coordinate is the average of these intercepts</p>
     * <p>Gauss-Newton is used to fit a circle with a centre y-coordinate and radius, r</p>
     * @param obs The SECM image that is to be analyzed.
     * @return The position of the centre of obs
     */
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
    
    /**
     * Calculates the positive x-coordinate of a circle that has centre (0,y0) and radius r at y=y
     * @param y the y-coordinate of the x-coordinate of interest
     * @param r the radius of the circle
     * @param y0 the y-coordinate of the centre of the circle
     * @return the positive x-coordinate of the circle at y.
     */
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
    
    /**
     * Calculates the derivative of the x-coordinate of a circle with respect to the radius at height y
     * @param y the y-coordinate at which the derivative is to be evaluated
     * @param r the radius of the circle
     * @param y0 the y-coordinate of the centre of the circle
     * @return dx/dr of the circle at position y 
     */
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
    
    /**
     * Calculates the derivative of the x-coordinate of a circle with respect to the y-coordinate of the circle's centre at height y
     * @param y the y-coordinate at which the derivative is to be evaluated
     * @param r the radius of the circle
     * @param y0 the y-coordinate of the centre of the circle
     * @return dx/dy0 at position y
     */
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
    
    /**
     * Calculates the residuals for a circle with respect to experimental data.
     * <p>The experimental data is expected to be already modified such that all x-positions are positive and 0 was the average x-position before the absolute values of the x-positions were taken.</p>
     * <p>the residuals are the difference in the x-coordinate between the circle and the observed position</p>
     * @param data the position data where the edges of the circular-like shape were observed
     * @param r the radius of the circle
     * @param y0 the y-coordinate of the centre of the circle
     * @return the residuals for each position.
     */
    private static double[] getResiduals(Position[] data, double r, double y0){
        double[] residuals = new double[data.length];
        for(int i = 0; i < data.length; i++){
            double y = data[i].getY();
            residuals[i] = data[i].getX() - circleXCoord(y, r, y0);
        }
        return residuals;
    }
}
