/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import fitting.ImageAlign;
import fitting.NonlinearLeastSquares;
import static fitting.NonlinearLeastSquares.DeltaCs;
import static fitting.NonlinearLeastSquares.DeltaCs_LevenbergMarquardt;
import static fitting.NonlinearLeastSquares.Jacobian;
import static fitting.NonlinearLeastSquares.Residual;
import io.ImproperFileFormattingException;
import static io.Input.read_alignment_data;
import static io.Input.read_fitting_iteration;
import static io.Input.read_observed;
import static io.Input.read_secm;
import static io.Output.saveLSIterationData;
import static io.Output.writeSECMImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.commons.math3.linear.ArrayRealVector;
import structures.CannotMirrorException;
import structures.LSIterationData;
import structures.Position;
import structures.SECMImage;

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
        String obserevFile = "C:\\Users\\Nathaniel\\Documents\\COMSOL_DATA\\00_Primitives\\Toy_2-difflim-circ\\True_10.csv";
        String iterationFile = "C:\\Users\\Nathaniel\\Documents\\COMSOL_DATA\\00_Primitives\\Toy_2-difflim-circ\\Iter8.csv";
        double r = 1.34;
        double logk = -2.195;
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
        System.out.println("R: " + r_new + "*a");
        System.out.println("logk: " + k_new);
        System.out.println("Sum. square residuals: " + ssr);
    }
    
    /**
     * Should perform one LM fitting iteration on toy models with known L.
     * @throws FileNotFoundException
     * @throws ImproperFileFormattingException 
     * @throws javax.xml.transform.TransformerException 
     * @throws javax.xml.transform.TransformerConfigurationException 
     * @throws javax.xml.parsers.ParserConfigurationException 
     */
    public static void one_lm_iteration() throws FileNotFoundException, ImproperFileFormattingException, TransformerException, TransformerConfigurationException, ParserConfigurationException{
        String obserevFile = "C:\\Users\\Nathaniel\\Documents\\COMSOL_DATA\\00_Primitives\\Toy_1-Circle\\Subsample2\\True_10.csv";
        int iter = 7;
        String iterationFile = "C:\\Users\\Nathaniel\\Documents\\COMSOL_DATA\\00_Primitives\\Toy_1-Circle\\Subs2_dir6\\Iter" + iter + "_E-2.csv";
        double r = 1.03;
        double logk = -3.194;
        double[] obsim = read_observed(obserevFile);
        double[][] iter_data = read_fitting_iteration(iterationFile);
        double[] diagonal = new double[]{1.2628000000000459E-17, 3.4000000000017553E-18};
        
        double lambda = 10.0;
        
        LSIterationData lsi = new LSIterationData(new double[]{r, logk}, new String[]{"r", "logk"});
        
        lsi.data_file_path = iterationFile;
        lsi.lambda = lambda;
        lsi.iteration_number = iter;
        lsi.jacobian_diagonal = diagonal;
        
        double[] residuals = Residual(obsim, iter_data).toArray();
        double ssr = 0;
        for(int i = 0; i < residuals.length; i++){
            ssr += residuals[i]*residuals[i];
        }
        System.out.println("Sum. square residuals: " + ssr);
        
        
        LSIterationData newlsi = NonlinearLeastSquares.NextIteration(lsi, Jacobian(iter_data), lambda, true, Residual(obsim, iter_data), LSIterationData.DIAGONAL_POLICY_BIGGEST);
        
        newlsi.data_file_path = iterationFile;
        
        
        saveLSIterationData("C:\\Users\\Nathaniel\\Documents\\COMSOL_DATA\\00_Primitives\\Toy_1-Circle\\Subs2_dir6\\Iter" + newlsi.iteration_number + ".xml", new LSIterationData[]{newlsi});
        
    }
    
    /**
     * How experimental data reading was first handled and tested.
     * @throws IOException
     * @throws FileNotFoundException
     * @throws ImproperFileFormattingException 
     * @throws structures.CannotMirrorException 
     */
    public static void first_experimental_type() throws IOException, FileNotFoundException, ImproperFileFormattingException, CannotMirrorException{
        SECMImage emtest = read_secm("C:\\Users\\Nathaniel\\Documents\\NetBeansProjects\\LeastSquaresSECMFit\\TEST_DATA\\Em1_Data_newFormat1.txt");
//            writeSECMImage("C:\\Users\\Nathaniel\\Documents\\NetBeansProjects\\LeastSquaresSECMFit\\TEST_DATA\\em1testout.csv", emtest);
            SECMImage sim = read_alignment_data("C:\\Users\\Nathaniel\\Documents\\COMSOL_DATA\\00_Primitives\\Emmanuel_1\\AIAR_Big\\I2_1.csv");
//            writeSECMImage("C:\\Users\\Nathaniel\\Documents\\NetBeansProjects\\LeastSquaresSECMFit\\TEST_DATA\\g1testout.csv", gtest);
            SECMImage sim_ext = sim.getMirrorExpanded();
//            writeSECMImage("C:\\Users\\Nathaniel\\Documents\\NetBeansProjects\\LeastSquaresSECMFit\\TEST_DATA\\mirror_norm_sim.csv", sim_ext.getNormalized());
            
            SECMImage bigspot = emtest.getCrop(2, 35, 32, 58, true);
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
            
            Position a = ImageAlign.centreImages(norm_centr_bigspt, sim_ext.getNormalized());
            centr_bigspt = centr_bigspt.getOffset(-a.getX(), -a.getY());
            writeSECMImage("C:\\Users\\Nathaniel\\Documents\\COMSOL_DATA\\00_Primitives\\Emmanuel_1\\AIAR_Big\\em1testout_Big_properCentred.csv", centr_bigspt);
            SECMImage diff = centr_bigspt.subtract(sim, SECMImage.OUT_OF_BOUNDS_EXTRAPOLATE);
//            writeSECMImage("C:\\Users\\Nathaniel\\Documents\\NetBeansProjects\\LeastSquaresSECMFit\\TEST_DATA\\em1_I2Diff.csv", diff);
            
            ArrayRealVector residual = new ArrayRealVector(diff.linearize());
            double[] residuals = diff.linearize();
            double ssr = 0;
            for(int i = 0; i < residuals.length; i++){
                ssr += residuals[i]*residuals[i];
            }
            System.out.println("Sum. square residuals: " + ssr);

            
            
//            String obserevFile = "C:\\Users\\Nathaniel\\Documents\\COMSOL_DATA\\00_Primitives\\Toy_1-Circle\\Subsample2\\True_10.csv";
            String iterationFile = "C:\\Users\\Nathaniel\\Documents\\COMSOL_DATA\\00_Primitives\\Emmanuel_1\\AIAR_Big\\I2_1.csv";
            double lambda = 1;
            double r = 2.10;
            double logk = -3.121;
//            double[] obsim = read_observed(obserevFile);
            double[][] iter_data = read_fitting_iteration(iterationFile);
//            double[] deltaC = DeltaCs(Jacobian(iter_data), Residual(obsim, iter_data));
            double[] deltaC = DeltaCs_LevenbergMarquardt(Jacobian(iter_data), lambda, true, residual);
//            double[] residuals = Residual(obsim, iter_data).toArray();
            
            double r_new = r + deltaC[0];
            double k_new = logk + deltaC[1];
            System.out.println("R: " + r_new + "a");
            System.out.println("logk: " + k_new);
            System.out.println("Are your units correct???");
    }
}
