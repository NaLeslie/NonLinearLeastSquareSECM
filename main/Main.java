package main;

import fitting.ImageAlign;
import static fitting.NonlinearLeastSquares.*;
import static io.Input.*;
import static io.Output.*;
import java.io.File;
import java.util.Scanner;
import static main.Demo.one_lm_iteration;
import org.apache.commons.math3.linear.ArrayRealVector;
import structures.LSIterationData;
import structures.Position;
import structures.SECMImage;
import static utility.Search.FindSmaller;

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
            one_lm_iteration();
//            SECMImage emtest = read_secm("C:\\Users\\Nathaniel\\Documents\\NetBeansProjects\\LeastSquaresSECMFit\\TEST_DATA\\Em1_Data_newFormat1.txt");
////            writeSECMImage("C:\\Users\\Nathaniel\\Documents\\NetBeansProjects\\LeastSquaresSECMFit\\TEST_DATA\\em1testout.csv", emtest);
//            SECMImage sim = read_alignment_data("C:\\Users\\Nathaniel\\Documents\\COMSOL_DATA\\00_Primitives\\Toy_2-difflim-circ\\True_10.csv");
//            writeSECMImage("C:\\Users\\Nathaniel\\Documents\\COMSOL_DATA\\00_Primitives\\Toy_2-difflim-circ\\Reflected-True_10.csv", sim.getOffset(-50, -50).getMirrorExpanded());
////            writeSECMImage("C:\\Users\\Nathaniel\\Documents\\NetBeansProjects\\LeastSquaresSECMFit\\TEST_DATA\\g1testout.csv", gtest);
//            SECMImage sim_ext = sim.getMirrorExpanded();
////            writeSECMImage("C:\\Users\\Nathaniel\\Documents\\NetBeansProjects\\LeastSquaresSECMFit\\TEST_DATA\\mirror_norm_sim.csv", sim_ext.getNormalized());
//            
//            SECMImage bigspot = emtest.getCrop(2, 35, 32, 58, true);
////            writeSECMImage("C:\\Users\\Nathaniel\\Documents\\NetBeansProjects\\LeastSquaresSECMFit\\TEST_DATA\\em1testout_crop1.csv", bigspot);
////            
////            SECMImage lilspot = emtest.getCrop(24, 36, 0, 18, true);
////            writeSECMImage("C:\\Users\\Nathaniel\\Documents\\NetBeansProjects\\LeastSquaresSECMFit\\TEST_DATA\\em1testout_crop2.csv", lilspot);
////            
//            //offset test... center the big spot
//            double xmin = bigspot.getXMin();
//            double xmax = bigspot.getXMax();
//            double ymin = bigspot.getYMin();
//            double ymax = bigspot.getYMax();
//            
//            SECMImage centr_bigspt = bigspot.getOffset(-(xmax + xmin)*0.5, -(ymax + ymin)*0.5);
////            writeSECMImage("C:\\Users\\Nathaniel\\Documents\\NetBeansProjects\\LeastSquaresSECMFit\\TEST_DATA\\em1testout_crop1_ctr.csv", centr_bigspt);
//            SECMImage norm_centr_bigspt = centr_bigspt.getNormalized();
////            writeSECMImage("C:\\Users\\Nathaniel\\Documents\\NetBeansProjects\\LeastSquaresSECMFit\\TEST_DATA\\em1testout_crop1_ctr_nrm.csv", norm_centr_bigspt);
////            SECMImage norm_emtest = emtest.getNormalized();
////            writeSECMImage("C:\\Users\\Nathaniel\\Documents\\NetBeansProjects\\LeastSquaresSECMFit\\TEST_DATA\\em1testout_norm.csv", norm_emtest);
////            SECMImage diff = norm_centr_bigspt.subtract(gtest_ext.getNormalized(), SECMImage.OUT_OF_BOUNDS_EXTRAPOLATE);
////            writeSECMImage("C:\\Users\\Nathaniel\\Documents\\NetBeansProjects\\LeastSquaresSECMFit\\TEST_DATA\\em1test_centredDiff.csv", diff);
//            
//            Position a = ImageAlign.centreImages(norm_centr_bigspt, sim_ext.getNormalized());
//            centr_bigspt = centr_bigspt.getOffset(-a.getX(), -a.getY());
//            writeSECMImage("C:\\Users\\Nathaniel\\Documents\\COMSOL_DATA\\00_Primitives\\Emmanuel_1\\AIAR_Big\\em1testout_Big_properCentred.csv", centr_bigspt);
//            SECMImage diff = centr_bigspt.subtract(sim, SECMImage.OUT_OF_BOUNDS_EXTRAPOLATE);
////            writeSECMImage("C:\\Users\\Nathaniel\\Documents\\NetBeansProjects\\LeastSquaresSECMFit\\TEST_DATA\\em1_I2Diff.csv", diff);
//            
//            ArrayRealVector residual = new ArrayRealVector(diff.linearize());
//            double[] residuals = diff.linearize();
//            double ssr = 0;
//            for(int i = 0; i < residuals.length; i++){
//                ssr += residuals[i]*residuals[i];
//            }
//            System.out.println("Sum. square residuals: " + ssr);

            
            
//            String obserevFile = "C:\\Users\\Nathaniel\\Documents\\COMSOL_DATA\\00_Primitives\\Toy_1-Circle\\Subsample2\\True_10.csv";
//            String iterationFile = "C:\\Users\\Nathaniel\\Documents\\COMSOL_DATA\\00_Primitives\\Emmanuel_1\\AIAR_Big\\I2_1.csv";
//            Demo.one_iteration();
//            double lambda = 1;
//            double r = 2.10;
//            double logk = -3.121;
////            double[] obsim = read_observed(obserevFile);
//            double[][] iter_data = read_fitting_iteration(iterationFile);
//            
//            for(int i = 0; i < iter_data.length; i++){
//                System.out.println(i + ":    " + iter_data[i][0] + "," + iter_data[i][1] + "," + iter_data[i][2]);
//            }
//            
//            System.out.println("\n\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
//            
//            
//            iter_data = read_fitting_iteration(iterationFile, false, true);
//            
//            for(int i = 0; i < iter_data.length; i++){
//                System.out.println(i + ":    " + iter_data[i][0] + "," + iter_data[i][1] + "," + iter_data[i][2]);
//            }
            
////            double[] deltaC = DeltaCs(Jacobian(iter_data), Residual(obsim, iter_data));
//            double[] deltaC = DeltaCs_LevenbergMarquardt(Jacobian(iter_data), lambda, true, residual);
////            double[] residuals = Residual(obsim, iter_data).toArray();
//            
//            double r_new = r + deltaC[0];
//            double k_new = logk + deltaC[1];
//            System.out.println("R: " + r_new + "a");
//            System.out.println("logk: " + k_new);
//            System.out.println("Are your units correct???");
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
}
