/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import static fitting.NonlinearLeastSquares.DeltaCs;
import static fitting.NonlinearLeastSquares.Jacobian;
import static fitting.NonlinearLeastSquares.Residual;
import io.ImproperFileFormattingException;
import static io.Input.read_fitting_iteration;
import static io.Input.read_observed;
import java.io.FileNotFoundException;

/**
 *
 * @author Nathaniel
 */
public class Demo {
    /**
     * How this was done before I first used experimental data
     * @throws FileNotFoundException
     * @throws ImproperFileFormattingException 
     */
    public static void one_iteration() throws FileNotFoundException, ImproperFileFormattingException{
        String obserevFile = "C:\\Users\\Nathaniel\\Documents\\COMSOL_DATA\\00_Primitives\\Toy_1-Circle\\Subsample2\\True_10.csv";
        String iterationFile = "C:\\Users\\Nathaniel\\Documents\\COMSOL_DATA\\00_Primitives\\Toy_1-Circle\\Subs2_dir5\\I8.csv";
        double r = 1.00;
        double logk = -3.063;
        double[] obsim = read_observed(obserevFile);
        double[][] iter_data = read_fitting_iteration(iterationFile);
        double[] deltaC = DeltaCs(Jacobian(iter_data), Residual(obsim, iter_data));
        double[] residuals = Residual(obsim, iter_data).toArray();
        double ssr = 0;
        for(int i = 0; i < residuals.length; i++){
            ssr += residuals[i]*residuals[i];
        }
        double r_new = r + deltaC[0];
        double k_new = logk + deltaC[1];
        System.out.println("R: " + r_new + "a");
        System.out.println("logk: " + k_new);
        System.out.println("Sum. square residuals: " + ssr);
    }
}
