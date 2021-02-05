package main;

import fitting.ImageAlign;
import static fitting.NonlinearLeastSquares.*;
import static io.Input.*;
import static io.Output.*;
import java.io.File;
import java.util.Scanner;
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
            SECMImage emtest = read_secm("C:\\Users\\Nathaniel\\Documents\\NetBeansProjects\\LeastSquaresSECMFit\\TEST_DATA\\Em1_Data_newFormat1.txt");
//            writeSECMImage("C:\\Users\\Nathaniel\\Documents\\NetBeansProjects\\LeastSquaresSECMFit\\TEST_DATA\\em1testout.csv", emtest);
            SECMImage gtest = read_secm("C:\\Users\\Nathaniel\\Documents\\NetBeansProjects\\LeastSquaresSECMFit\\TEST_DATA\\G1.csv");
//            writeSECMImage("C:\\Users\\Nathaniel\\Documents\\NetBeansProjects\\LeastSquaresSECMFit\\TEST_DATA\\g1testout.csv", gtest);
            SECMImage gtest_ext = gtest.getMirrorExpanded();
//            writeSECMImage("C:\\Users\\Nathaniel\\Documents\\NetBeansProjects\\LeastSquaresSECMFit\\TEST_DATA\\g1mirrortestout.csv", gtest_ext);
            SECMImage subTest = gtest_ext.subtract(gtest, SECMImage.OUT_OF_BOUNDS_EXTRAPOLATE);
//            writeSECMImage("C:\\Users\\Nathaniel\\Documents\\NetBeansProjects\\LeastSquaresSECMFit\\TEST_DATA\\g1subtestout.csv", subTest);
            
            SECMImage bigspot = emtest.getCrop(7.5, 29.5, 32, 58, true);
//            writeSECMImage("C:\\Users\\Nathaniel\\Documents\\NetBeansProjects\\LeastSquaresSECMFit\\TEST_DATA\\em1testout_crop1.csv", bigspot);
//            
//            SECMImage lilspot = emtest.getCrop(24, 36, 0, 18, true);
//            writeSECMImage("C:\\Users\\Nathaniel\\Documents\\NetBeansProjects\\LeastSquaresSECMFit\\TEST_DATA\\em1testout_crop2.csv", lilspot);
//            
            //offset test... center the big spot
            double xmin = bigspot.getXMin();
            double xmax = bigspot.getXMax();
            double ymin = bigspot.getYMin();
            double ymax = bigspot.getYMax();
            
            SECMImage centr_bigspt = bigspot.getOffset(-(xmax + xmin)*0.5, -(ymax + ymin)*0.5);
//            writeSECMImage("C:\\Users\\Nathaniel\\Documents\\NetBeansProjects\\LeastSquaresSECMFit\\TEST_DATA\\em1testout_crop1_ctr.csv", centr_bigspt);
            SECMImage norm_centr_bigspt = centr_bigspt.getNormalized();
//            writeSECMImage("C:\\Users\\Nathaniel\\Documents\\NetBeansProjects\\LeastSquaresSECMFit\\TEST_DATA\\em1testout_crop1_ctr_nrm.csv", norm_centr_bigspt);
//            SECMImage norm_emtest = emtest.getNormalized();
//            writeSECMImage("C:\\Users\\Nathaniel\\Documents\\NetBeansProjects\\LeastSquaresSECMFit\\TEST_DATA\\em1testout_norm.csv", norm_emtest);
//            SECMImage diff = norm_centr_bigspt.subtract(gtest_ext.getNormalized(), SECMImage.OUT_OF_BOUNDS_EXTRAPOLATE);
//            writeSECMImage("C:\\Users\\Nathaniel\\Documents\\NetBeansProjects\\LeastSquaresSECMFit\\TEST_DATA\\em1test_centredDiff.csv", diff);
            
            Position a = ImageAlign.centreImages(norm_centr_bigspt, gtest_ext);
            centr_bigspt = centr_bigspt.getOffset(-a.getX(), -a.getX());
            norm_centr_bigspt = centr_bigspt.getNormalized();
            SECMImage diff = norm_centr_bigspt.subtract(gtest_ext.getNormalized(), SECMImage.OUT_OF_BOUNDS_EXTRAPOLATE);
            writeSECMImage("C:\\Users\\Nathaniel\\Documents\\NetBeansProjects\\LeastSquaresSECMFit\\TEST_DATA\\em1test_centredDiff_aligned.csv", diff);
//            double[] newX = new double[36];
//            double[] newY = new double[36];
//            for(int i = 0; i < 36; i++){
//                newX[i] = ((double)i)*0.5 - 9.25;
//                newY[i] = ((double)i)*0.5 - 9.25;
//            }
//            double[][] newI = centr_bigspt.getCurrents(newX, newY, SECMImage.OUT_OF_BOUNDS_EXTRAPOLATE);
//            
//            SECMImage centr_bigspt_resampled = new SECMImage(newX, newY, newI);
//            writeSECMImage("C:\\Users\\Nathaniel\\Documents\\NetBeansProjects\\LeastSquaresSECMFit\\TEST_DATA\\em1testout_crop1_ctr_res.csv", centr_bigspt_resampled);
//            double[] currents = centr_bigspt_resampled.linearize();
//            for(double j:currents){
//                System.out.println(j);
//            }
            
            
//            String obserevFile = "C:\\Users\\Nathaniel\\Documents\\COMSOL_DATA\\00_Primitives\\Toy_1-Circle\\Subsample2\\True_10.csv";
//            String iterationFile = "C:\\Users\\Nathaniel\\Documents\\COMSOL_DATA\\00_Primitives\\Toy_1-Circle\\Subs2_dir5\\I8.csv";
//            double r = 1.00;
//            double logk = -3.063;
//            double[] obsim = read_observed(obserevFile);
//            double[][] iter_data = read_fitting_iteration(iterationFile);
//            double[] deltaC = DeltaCs(Jacobian(iter_data), Residual(obsim, iter_data));
//            double[] residuals = Residual(obsim, iter_data).toArray();
//            double ssr = 0;
//            for(int i = 0; i < residuals.length; i++){
//                ssr += residuals[i]*residuals[i];
//            }
//            double r_new = r + deltaC[0];
//            double k_new = logk + deltaC[1];
//            System.out.println("R: " + r_new + "a");
//            System.out.println("logk: " + k_new);
//            System.out.println("Sum. square residuals: " + ssr);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
}
