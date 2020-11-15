package main;

import static fitting.NonlinearLeastSquares.*;
import static io.Input.*;

/**
 *
 * @author Nathaniel
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    @SuppressWarnings({"UseSpecificCatch", "CallToPrintStackTrace"})
    public static void main(String[] args) {
        
        try {
            String obserevFile = "C:\\Users\\Nathaniel\\Documents\\COMSOL_DATA\\00_Primitives\\Toy_1-Circle\\True.csv";
            String iterationFile = "C:\\Users\\Nathaniel\\Documents\\COMSOL_DATA\\00_Primitives\\Toy_1-Circle\\I1.csv";
            double r = 1.5;
            double k = 0.0001;
            double[] obsim = read_observed(obserevFile);
            double[][] iter_data = read_fitting_iteration(iterationFile);
            double[] deltaC = DeltaCs(Jacobian(iter_data), Residual(obsim, iter_data));
            double[] residuals = Residual(obsim, iter_data).toArray();
            double ssr = 0;
            for(int i = 0; i < residuals.length; i++){
                ssr += residuals[i]*residuals[i];
            }
            double r_new = r - deltaC[0];
            double k_new = k - deltaC[1];
            System.out.println("R: " + r_new + "a");
            System.out.println("k: " + k_new);
            System.out.println("Sum. square residuals: " + ssr);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
}
