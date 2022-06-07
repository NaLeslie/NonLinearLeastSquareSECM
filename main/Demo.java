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
import static io.Input.*;
import java.io.File;
import java.util.Scanner;

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
        double[][] iter_data = read_fitting_iteration_two_param(iterationFile);
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
        String obserevFile = "C:\\Users\\Nathaniel\\Documents\\COMSOL_DATA\\00_Edges\\Mart\\Canny\\more bg\\True.csv";
        int iter = 7;
        String iterationFile = "C:\\Users\\Nathaniel\\Documents\\COMSOL_DATA\\00_Edges\\Mart\\Canny\\more bg\\I" + iter + "_0.csv";
        double L = 0.92;
        double logk = -3.813;
        double[] obsim = read_observed(obserevFile);
        double[][] iter_data = read_fitting_iteration_two_param(iterationFile);
        double[] diagonal = new double[]{3.4250334616999997E-16, 7.05000000000019E-18};
        
        double lambda = 0.0;
        
        LSIterationData lsi = new LSIterationData(new double[]{L, logk}, new String[]{"L", "logk"});
        
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
        
        
        saveLSIterationData("C:\\Users\\Nathaniel\\Documents\\COMSOL_DATA\\00_Edges\\Mart\\Canny\\more bg\\Iter" + newlsi.iteration_number + ".xml", new LSIterationData[]{newlsi});
        
    }
    
    /**
     * fits L, R, and logk of a circular feature
     * @throws FileNotFoundException
     * @throws ImproperFileFormattingException
     * @throws TransformerException
     * @throws TransformerConfigurationException
     * @throws ParserConfigurationException 
     */
    public static void one_LRK_lm_iteration() throws FileNotFoundException, ImproperFileFormattingException, TransformerException, TransformerConfigurationException, ParserConfigurationException{
        String obserevFile = "C:\\Users\\Nathaniel\\Documents\\COMSOL_DATA\\00_Primitives\\Toy_6\\True.csv";
        int iter = 11;
        String iterationFile = "C:\\Users\\Nathaniel\\Documents\\COMSOL_DATA\\00_Primitives\\Toy_6\\I" + iter + "_E-1.csv";
        double r = 1.23;
        double logk = -2.589;
        double L = 0.91;
        double[] obsim = read_observed(obserevFile);
        double[][] iter_data = read_fitting_iteration_three_param(iterationFile);
        double[] diagonal = new double[]{1.3951599999999944E-17, 6.519999999998623E-18, 3.947230000000015E-17};
        
        double lambda = 0.0;
        
        LSIterationData lsi = new LSIterationData(new double[]{r, logk, L}, new String[]{"r", "logk", "L"});
        
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
        
        
        saveLSIterationData("C:\\Users\\Nathaniel\\Documents\\COMSOL_DATA\\00_Primitives\\Toy_6\\Iter" + newlsi.iteration_number + ".xml", new LSIterationData[]{newlsi});
        
    }
    
    /**
     * fits L, R, and logk of a circular feature
     * @throws FileNotFoundException
     * @throws ImproperFileFormattingException
     * @throws TransformerException
     * @throws TransformerConfigurationException
     * @throws ParserConfigurationException 
     */
    public static void one_LRK_lm_iteration_exp() throws FileNotFoundException, ImproperFileFormattingException, TransformerException, TransformerConfigurationException, ParserConfigurationException{
        String obserevFile = "C:\\Users\\Nathaniel\\Documents\\COMSOL_DATA\\00_Primitives\\E_AmT6\\2\\true.csv";
        int iter = 9;
        String iterationFile = "C:\\Users\\Nathaniel\\Documents\\COMSOL_DATA\\00_Primitives\\E_AmT6\\2\\I" + iter + "_E0.csv";
        double r = 1.75;
        double logk = -3.648;
        double L = 0.93;
        double[] obsim = read_observed(obserevFile);
        System.out.println("obs complete");
        double[][] iter_data = read_fitting_iteration_three_param(iterationFile, true, false);
        double[] diagonal = new double[]{3.269390000000012E-21, 5.392999999999716E-21, 6.1488200000000486E-21};
        
        double lambda = 0.0;
        
        LSIterationData lsi = new LSIterationData(new double[]{r, logk, L}, new String[]{"r", "logk", "L"});
        
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
        
        
        saveLSIterationData("C:\\Users\\Nathaniel\\Documents\\COMSOL_DATA\\00_Primitives\\E_AmT6\\2\\Iter" + newlsi.iteration_number + ".xml", new LSIterationData[]{newlsi});
        
    }
    
    /**
     * Aligns an experimental SECM image such that the centre is at 0,0 then exports the currents at specified coordinates to use in the fitting.
     * @throws FileNotFoundException
     * @throws ImproperFileFormattingException
     * @throws CannotMirrorException
     * @throws IOException 
     */
    public static void setup_experimental_image() throws FileNotFoundException, ImproperFileFormattingException, CannotMirrorException, IOException{
        String filename = "C:\\Users\\Nathaniel\\Documents\\COMSOL_DATA\\00_Primitives\\E_AmT6\\AmT6_data.txt";
        
        SECMImage expimg = read_secm(filename);
        SECMImage sim = read_alignment_data("C:\\Users\\Nathaniel\\Documents\\COMSOL_DATA\\00_Primitives\\E_AmT6\\2\\Align.csv");
        SECMImage sim_ext = sim.getMirrorExpanded();
        
        //crop the image
        SECMImage spot = expimg.getCrop(54, 74, 16, 38, true);
        
        SECMImage spot_norm = spot.getNormalized();
        //gauss newton
        Position a = ImageAlign.centreImages(spot_norm, sim_ext.getNormalized());
        //centering using chords
        Position b = ImageAlign.findCentre(spot_norm);
        System.out.println(a.toString());
        System.out.println(b.toString());
        //apply offset
        SECMImage centr_spot = spot.getOffset(-a.getX(), -a.getY());
        writeSECMImage("C:\\Users\\Nathaniel\\Documents\\COMSOL_DATA\\00_Primitives\\E_AmT6\\2\\AmT6.csv", centr_spot);
        //coordinates to use in fitting
        double[] xs = new double[]{-6.0, -4.5, -3.0, -1.5, 0.0, 1.5, 3.0, 4.5, 6.0};
        double[] ys = new double[]{-9.38, -5.38, -1.38, 2.62};
        //export currents at the fitting coordinates
        double[][] currs = centr_spot.getCurrents(xs, ys, SECMImage.OUT_OF_BOUNDS_NAN);
        SECMImage newspaces = new SECMImage(xs, ys, currs);
        writeSECMImage("C:\\Users\\Nathaniel\\Documents\\COMSOL_DATA\\00_Primitives\\E_AmT6\\2\\true.csv", newspaces);
        
    }
    
    public static void setup_edge_image() throws FileNotFoundException, ImproperFileFormattingException, CannotMirrorException, IOException{
        String filename = "C:\\Users\\Nathaniel\\Documents\\COMSOL_DATA\\00_Edges\\Mart\\True_lk-3_L95.csv";
        
        SECMImage expimg = read_secm(filename);
        SECMImage sim = read_alignment_data("C:\\Users\\Nathaniel\\Documents\\COMSOL_DATA\\00_Edges\\Mart\\Circ\\Align.csv");
        SECMImage sim_ext = sim.getMirrorExpanded();
        
        //crop the image
        SECMImage spot = expimg.getCrop(42, 58, 42, 58, true);
        
        SECMImage spot_norm = spot.getNormalized();
        Position a = ImageAlign.centreImages(spot_norm, sim_ext.getNormalized());
        Position b = ImageAlign.findCentre(spot_norm);
        System.out.println(a.toString());
        System.out.println(b.toString());
        SECMImage centr_spot = spot.getOffset(-a.getX(), -a.getY());
        writeSECMImage("C:\\Users\\Nathaniel\\Documents\\COMSOL_DATA\\00_Edges\\Mart\\Circ\\cent.csv", centr_spot);
        double[] xs = new double[]{-6.0, -4.0, -2.0, 0.0, 2.0, 4.0, 6.0};
        double[] ys = new double[]{-6.0, -4.0, -2.0, 0.0, 2.0, 4.0, 6.0};
        double[][] currs = centr_spot.getCurrents(xs, ys, SECMImage.OUT_OF_BOUNDS_NAN);
        SECMImage newspaces = new SECMImage(xs, ys, currs);
        writeSECMImage("C:\\Users\\Nathaniel\\Documents\\COMSOL_DATA\\00_Edges\\Mart\\Circ\\true.csv", newspaces);
        
    }
    
    public static void one_LK_lm_iteration_edge() throws FileNotFoundException, ImproperFileFormattingException, TransformerException, TransformerConfigurationException, ParserConfigurationException{
        String obserevFile = "C:\\Users\\Nathaniel\\Documents\\COMSOL_DATA\\00_Edges\\Mart\\Circ\\true.csv";
        int iter = 7;
        String iterationFile = "C:\\Users\\Nathaniel\\Documents\\COMSOL_DATA\\00_Edges\\Mart\\Circ\\I" + iter + "_E1.csv";
        double r = 3.34;
        double logk = -3.823;
        double L = 0.96;
        double[] obsim = read_observed(obserevFile);
        System.out.println("obs complete");
        double[][] iter_data = read_fitting_iteration_three_param(iterationFile, true, true);
        double[] diagonal = new double[]{9.957000000000353E-18, 2.010000000000273E-17, 5.931739999999887E-17};
        
        double lambda = 10.0;
        
        LSIterationData lsi = new LSIterationData(new double[]{r, logk, L}, new String[]{"r", "logk", "L"});
        
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
        
        
        saveLSIterationData("C:\\Users\\Nathaniel\\Documents\\COMSOL_DATA\\00_Edges\\Mart\\Circ\\Iter" + newlsi.iteration_number + ".xml", new LSIterationData[]{newlsi});
        
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
            double[][] iter_data = read_fitting_iteration_two_param(iterationFile);
//            double[] deltaC = DeltaCs(Jacobian(iter_data), Residual(obsim, iter_data));
            double[] deltaC = DeltaCs_LevenbergMarquardt(Jacobian(iter_data), lambda, true, residual);
//            double[] residuals = Residual(obsim, iter_data).toArray();
            
            double r_new = r + deltaC[0];
            double k_new = logk + deltaC[1];
            System.out.println("R: " + r_new + "a");
            System.out.println("logk: " + k_new);
            System.out.println("Are your units correct???");
    }
    
    /**
     * Code for generating the error surface plot in "Fitting Kinetics From Scanning Electrochemical Microscopy Images of Finite Circular Features"
     * @throws FileNotFoundException 
     */
    public static void error_surface() throws FileNotFoundException{
        double[][][][] data = new double[4][4][17][21];
        double[][] truedata = new double[4][4];
        String filename = "C:\\Users\\Nathaniel\\Documents\\COMSOL_DATA\\00_Primitives\\ErrorSurf\\AllScanData.csv";
        Scanner s = new Scanner(new File(filename));
        while(s.hasNextLine()){
            String line = s.nextLine();
            String[] inf = line.split(",");
            double x = Double.parseDouble(inf[0]);
            double y = Double.parseDouble(inf[1]);
            double r = Double.parseDouble(inf[2]);
            double lk = Double.parseDouble(inf[3]);
            double i = Double.parseDouble(inf[4]);
            if(x <= 50.0 && y <= 50.0){
                data[xy_to_addr(x)][xy_to_addr(y)][r_to_addr(r)][lk_to_addr(lk)] = i;
            }
        }
        filename = "C:\\Users\\Nathaniel\\Documents\\COMSOL_DATA\\00_Primitives\\Toy_1-Circle\\Subsample2\\True_10.csv";
        s = new Scanner(new File(filename));
        while(s.hasNextLine()){
            String line = s.nextLine();
            String[] inf = line.split(",");
            double x = Double.parseDouble(inf[0]);
            double y = Double.parseDouble(inf[1]);
            double i = Double.parseDouble(inf[2]);
            if(x <= 50.0 && y <= 50.0){
                truedata[xy_to_addr(x)][xy_to_addr(y)] = i;
            }
        }
        for(int ra = 0; ra < 17; ra ++){
            for(int lka = 0; lka < 21; lka ++){
                double sumsq = 0.0;
                for(int xa = 0; xa < 4; xa ++){
                    for(int ya = 0; ya < 4; ya ++){
                        double resid = data[xa][ya][ra][lka] - truedata[xa][ya];
                        sumsq += resid * resid;
                    }
                }
                sumsq /= 16.0;
                sumsq = Math.sqrt(sumsq);
                sumsq /= 4.0 * 1.25 * 6.7 * 9.6485 * 1.0E-11;
                System.out.println(addr_to_r(ra) + "," + addr_to_lk(lka) + "," + sumsq);
            }
            System.out.println("");
        }
        
    }
    
    /**
     * Converts an x or y coordinate to an array address
     * @param x_or_y the x or y coordinate whose corresponding array address is requested
     * @return the array address that corresponds to the x or y coordinate
     */
    private static int xy_to_addr(double x_or_y){
        double addrval = (x_or_y - 47.0);
        return (int)Math.round(addrval);
    }
    
    /**
     * Converts a feature radius to an array address
     * @param r the radius whose corresponding array address is requested
     * @return the array address that corresponds to the feature radius
     */
    private static int r_to_addr(double r){
        double addrval = (r - 0.00001)/0.0000003125;
        return (int)Math.round(addrval);
    }
    
    /**
     * Converts the logarithm of k to an array address
     * @param lk the logarithm of k whose corresponding array address is requested
     * @return the array address that corresponds to the logarithm of the feature k
     */
    private static int lk_to_addr(double lk){
        double addrval = (lk + 3.5)/0.05;
        return (int)Math.round(addrval);
    }
    
    /**
     * Converts the array address for the radius to the radius
     * @param addr the radius array address
     * @return the radius of the reactive feature
     */
    private static double addr_to_r(int addr){
        double addrval = ((double)addr)*0.0000003125 + 0.00001;
        return addrval;
    }
    
    /**
     * Converts the array address for the log k to the log k
     * @param addr the log k array address
     * @return the logarithm of k that corresponds to the given array address
     */
    private static double addr_to_lk(int addr){
        double addrval = ((double)addr)*0.05 - 3.5;
        return addrval;
    }
}
