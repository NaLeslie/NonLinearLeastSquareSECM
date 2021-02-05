package io;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import structures.SECMImage;
import static utility.Search.FindSmaller;

/**
 *
 * @author Nathaniel
 */
public class Input {
    
    /**
     * Parses a file and reads in currents for rectilinear, regularly spaced points 
     * on the xy plane. The output of this method is designed to be used by 
     * <code>fitting.NonlinearLeastSquares.Residual</code>
     * @param filename The directory and name of the file to be read from
     * @return a 1-dimensional vector of currents representing the points. This 
     * vector is ordered as successive scans along the x-direction for increasing 
     * y-coordinates.
     * <p>i.e.</p>
     * <p>123</p>
     * <p>456</p>
     * <p>789</p>
     * <p>would be ordered as: 123456789</p>
     * @throws FileNotFoundException If the specified file cannot be found
     * @throws ImproperFileFormattingException If the xy points are either not 
     * regularly spaced.
     */
    public static double[] read_observed(String filename) throws FileNotFoundException, ImproperFileFormattingException{
        ArrayList<Double> xs = new ArrayList();
        ArrayList<Double> ys = new ArrayList();
        try (Scanner scan = new Scanner(new File(filename))) {
            while(scan.hasNextLine()){//scan through the file
                String readline = scan.nextLine();
                if(!readline.startsWith("#")){//ignore "comment" lines
                    String[] readsplit = readline.split(",");
                    double x = Double.parseDouble(readsplit[0]);//read in the X-coordinate
                    double y = Double.parseDouble(readsplit[1]);//read in the Y-coordinate
                    boolean xexists = false;
                    for(int i = 0; i< xs.size(); i++){
                        double relative = Math.abs(x - xs.get(i))/xs.get(i);
                        if(relative < Constants.RELATIVE_ERR_CUTOFF){//see if the rho already exists
                            xexists = true;
                            break;//if the rho already exists, we do not need to iterate any further in the array
                        }
                    }
                    if(!xexists){
                        xs.add(x);//add the new rho to the list if it is not already
                    }
                    boolean yexists = false;
                    for(int i = 0; i< ys.size(); i++){
                        double relative = Math.abs(y - ys.get(i))/ys.get(i);
                        if(relative < Constants.RELATIVE_ERR_CUTOFF){//see if the rho already exists
                            yexists = true;
                            break;//if the rho already exists, we do not need to iterate any further in the array
                        }
                    }
                    if(!yexists){
                        ys.add(y);//add the new rho to the list if it is not already
                    }
                }
            }
        }
        Collections.sort(xs);
        Collections.sort(ys);
        //Check xs
        double Diff;
        double PrevDiff = 0.0;
        for(int i = 1; i< xs.size(); i++){
            Diff = xs.get(i) - xs.get(i-1);
            if((Diff - PrevDiff)/PrevDiff > Constants.RELATIVE_ERR_CUTOFF && PrevDiff != 0){
                throw new ImproperFileFormattingException("Non-uniform step in x direction detected.");// throw an exception if there is an inconsistent step size
            }
            PrevDiff = Diff;
        }
        //check ys
        PrevDiff = 0.0;
        for(int i = 1; i< ys.size(); i++){
            Diff = ys.get(i) - ys.get(i-1);
            if((Diff - PrevDiff)/PrevDiff > Constants.RELATIVE_ERR_CUTOFF && PrevDiff != 0){
                throw new ImproperFileFormattingException("Non-uniform step in y direction detected.");// throw an exception if there is an inconsistent step size
            }
            PrevDiff = Diff;
        }
        int xsize = xs.size();
        int ysize = ys.size();
        double xmin = xs.get(0);
        double ymin = ys.get(0);
        double xstep = (xs.get(xsize - 1) - xmin) / (double)(xsize - 1);
        double ystep = (ys.get(ysize - 1) - ymin) / (double)(ysize - 1);
        //second pass over the file
        double[] current = new double[xsize*ysize]; // This holds the relative current at each point x,y
        try (Scanner scan = new Scanner(new File(filename))) {
            while(scan.hasNextLine()){//scan through the file
                String readline = scan.nextLine();
                if(!readline.startsWith("#")){//ignore "comment" lines
                    String[] readsplit = readline.split(",");
                    double x = Double.parseDouble(readsplit[0]);//read in the X-coordinate
                    double y = Double.parseDouble(readsplit[1]);//read in the Y-coordinate
                    double i = Double.parseDouble(readsplit[2]);//read in the current
                    int xaddr = (int)Math.round((x - xmin) / xstep);//determine the x-address
                    int yaddr = (int)Math.round((y - ymin) / ystep);//determine the y-address
                    current[xaddr + yaddr*xsize] = i;
                }
            }
        }
        return current;
    }
    
    /**
     * Parses a file and reads in currents and partial derivatives of the current 
     * for rectilinear, regularly spaced points on the xy plane. The file should 
     * be comma separated with the columns in the following formatted as:
     * x, y, c1 perturbation, c2 perturbation, current
     * @param filename The directory and name of the file to be read from
     * @return a 3-column vector matrix represented as a 2D array. The first column 
     * vector is the current vector for this iteration. the following two columns 
     * are the Jacobian for this iteration
     * @throws FileNotFoundException If the specified file cannot be found
     * @throws ImproperFileFormattingException If the xy points are either not 
     * regularly spaced.
     */
    public static double[][] read_fitting_iteration(String filename) throws FileNotFoundException, ImproperFileFormattingException{
        ArrayList<Double> xs = new ArrayList();
        ArrayList<Double> ys = new ArrayList();
        try (Scanner scan = new Scanner(new File(filename))) {
            while(scan.hasNextLine()){//scan through the file
                String readline = scan.nextLine();
                if(!readline.startsWith("#")){//ignore "comment" lines
                    String[] readsplit = readline.split(",");
                    double x = Double.parseDouble(readsplit[0]);//read in the X-coordinate
                    double y = Double.parseDouble(readsplit[1]);//read in the Y-coordinate
                    boolean xexists = false;
                    for(int i = 0; i< xs.size(); i++){
                        double relative = Math.abs(x - xs.get(i))/xs.get(i);
                        if(relative < Constants.RELATIVE_ERR_CUTOFF){//see if the rho already exists
                            xexists = true;
                            break;//if the x already exists, we do not need to iterate any further in the array
                        }
                    }
                    if(!xexists){
                        xs.add(x);//add the new x to the list if it is not already
                    }
                    boolean yexists = false;
                    for(int i = 0; i< ys.size(); i++){
                        double relative = Math.abs(y - ys.get(i))/ys.get(i);
                        if(relative < Constants.RELATIVE_ERR_CUTOFF){//see if the rho already exists
                            yexists = true;
                            break;//if the y already exists, we do not need to iterate any further in the array
                        }
                    }
                    if(!yexists){
                        ys.add(y);//add the new y to the list if it is not already
                    }
                }
            }
        }
        Collections.sort(xs);
        Collections.sort(ys);
        //Check xs
        double Diff;
        double PrevDiff = 0.0;
        for(int i = 1; i< xs.size(); i++){
            Diff = xs.get(i) - xs.get(i-1);
            if((Diff - PrevDiff)/PrevDiff > Constants.RELATIVE_ERR_CUTOFF && PrevDiff != 0){
                throw new ImproperFileFormattingException("Non-uniform step in x direction detected.");// throw an exception if there is an inconsistent step size
            }
            PrevDiff = Diff;
        }
        //check ys
        PrevDiff = 0.0;
        for(int i = 1; i< ys.size(); i++){
            Diff = ys.get(i) - ys.get(i-1);
            if((Diff - PrevDiff)/PrevDiff > Constants.RELATIVE_ERR_CUTOFF && PrevDiff != 0){
                throw new ImproperFileFormattingException("Non-uniform step in y direction detected.");// throw an exception if there is an inconsistent step size
            }
            PrevDiff = Diff;
        }
        int xsize = xs.size();
        int ysize = ys.size();
        double xmin = xs.get(0);
        double ymin = ys.get(0);
        double xstep = (xs.get(xsize - 1) - xmin) / (double)(xsize - 1);
        double ystep = (ys.get(ysize - 1) - ymin) / (double)(ysize - 1);
        double expc1 = 1;
        double expc2 = 1;
        //second pass over the file
        double[][] current = new double[xsize*ysize][3]; // This holds the relative current at each point x,y
        try (Scanner scan = new Scanner(new File(filename))) {
            while(scan.hasNextLine()){//scan through the file
                String readline = scan.nextLine();
                if(!readline.startsWith("#")){//ignore "comment" lines
                    String[] readsplit = readline.split(",");
                    double x = Double.parseDouble(readsplit[0]);//read in the X-coordinate
                    double y = Double.parseDouble(readsplit[1]);//read in the Y-coordinate
                    double pc1 = Double.parseDouble(readsplit[2]);//read in the perturbation of the first parameter
                    double pc2 = Double.parseDouble(readsplit[3]);//read in the perturbation of the second parameter
                    double i = Double.parseDouble(readsplit[4]);//read in the current
                    int xaddr = (int)Math.round((x - xmin) / xstep);//determine the x-address
                    int yaddr = (int)Math.round((y - ymin) / ystep);//determine the y-address
                    int type = 0;
                    if(pc1 != 0){
                        type = 1;
                        expc1 = pc1;
                    }
                    else if(pc2 != 0){
                        type = 2;
                        expc2 = pc2;
                    }
                    current[xaddr + yaddr*xsize][type] = i;
                }
            }
        }
        //overwrite the non-first row with the Jacobian
        for(int r = 0; r < current.length; r++){
            current[r][1] = (current[r][1]-current[r][0])/expc1;
            current[r][2] = (current[r][2]-current[r][0])/expc2;
        }
        return current;
    }
    
    /**
     * 
     * @param filename
     * @return
     * @throws FileNotFoundException
     * @throws ImproperFileFormattingException 
     */
    public static SECMImage read_alignment_data(String filename) throws FileNotFoundException, ImproperFileFormattingException{
        ArrayList<Double> xs = new ArrayList();
        ArrayList<Double> ys = new ArrayList();
        try (Scanner scan = new Scanner(new File(filename))) {
            while(scan.hasNextLine()){//scan through the file
                String readline = scan.nextLine();
                if(!readline.startsWith("#")){//ignore "comment" lines
                    String[] readsplit = readline.split(",");
                    double x = Double.parseDouble(readsplit[0]);//read in the X-coordinate
                    double y = Double.parseDouble(readsplit[1]);//read in the Y-coordinate
                    boolean xexists = false;
                    for(int i = 0; i< xs.size(); i++){
                        double relative = Math.abs(x - xs.get(i))/xs.get(i);
                        if(relative < Constants.RELATIVE_ERR_CUTOFF){//see if the rho already exists
                            xexists = true;
                            break;//if the x already exists, we do not need to iterate any further in the array
                        }
                    }
                    if(!xexists){
                        xs.add(x);//add the new x to the list if it is not already
                    }
                    boolean yexists = false;
                    for(int i = 0; i< ys.size(); i++){
                        double relative = Math.abs(y - ys.get(i))/ys.get(i);
                        if(relative < Constants.RELATIVE_ERR_CUTOFF){//see if the rho already exists
                            yexists = true;
                            break;//if the y already exists, we do not need to iterate any further in the array
                        }
                    }
                    if(!yexists){
                        ys.add(y);//add the new y to the list if it is not already
                    }
                }
            }
        }
        //sort the x&y coordinates and create arrays out of them
        Collections.sort(xs);
        Collections.sort(ys);
        double[] x_coordinates = new double[xs.size()];
        double[] y_coordinates = new double[ys.size()];
        for(int i = 0; i < x_coordinates.length; i++){
            x_coordinates[i] = xs.get(i);
        }
        for(int i = 0; i < y_coordinates.length; i++){
            y_coordinates[i] = ys.get(i);
        }
        //second pass over the file
        double[][] current = new double[x_coordinates.length][y_coordinates.length]; // This holds the relative current at each point x,y
        try (Scanner scan = new Scanner(new File(filename))) {
            while(scan.hasNextLine()){//scan through the file
                String readline = scan.nextLine();
                if(!readline.startsWith("#")){//ignore "comment" lines
                    String[] readsplit = readline.split(",");
                    double x = Double.parseDouble(readsplit[0]);//read in the X-coordinate
                    double y = Double.parseDouble(readsplit[1]);//read in the Y-coordinate
                    double pc1 = Double.parseDouble(readsplit[2]);//read in the perturbation of the first parameter
                    double pc2 = Double.parseDouble(readsplit[3]);//read in the perturbation of the second parameter
                    double i = Double.parseDouble(readsplit[4]);//read in the current
                    int xaddr = FindSmaller(x, x_coordinates) + 1;//determine the x-address
                    int yaddr = FindSmaller(y, y_coordinates) + 1;//determine the y-address
                    if(pc1 == 0 && pc2 == 0){
                        current[xaddr][yaddr] = i;
                    }
                }
            }
        }
        SECMImage ret = new SECMImage(x_coordinates, y_coordinates, current);
        return ret;
    }
    
    /**
     * 
     * @param filename
     * @return
     * @throws FileNotFoundException 
     */
    public static SECMImage read_secm(String filename) throws FileNotFoundException{
        ArrayList<Double> xs = new ArrayList();
        ArrayList<Double> ys = new ArrayList();
        try (Scanner scan = new Scanner(new File(filename))) {
            while(scan.hasNextLine()){//scan through the file
                String readline = scan.nextLine();
                if(!readline.startsWith("#")){//ignore "comment" lines
                    String[] readsplit = readline.split(",");
                    double x = Double.parseDouble(readsplit[0]);//read in the X-coordinate
                    double y = Double.parseDouble(readsplit[1]);//read in the Y-coordinate
                    boolean xexists = false;
                    for(int i = 0; i< xs.size(); i++){
                        double relative = Math.abs(x - xs.get(i))/xs.get(i);
                        if(xs.get(i) == 0){
                            relative = Math.abs(x - xs.get(i));
                        }
                            
                        if(relative < Constants.RELATIVE_ERR_CUTOFF){//see if the rho already exists
                            xexists = true;
                            break;//if the x already exists, we do not need to iterate any further in the array
                        }
                    }
                    if(!xexists){
                        xs.add(x);//add the new x to the list if it is not already
                    }
                    boolean yexists = false;
                    for(int i = 0; i< ys.size(); i++){
                        double relative = Math.abs(y - ys.get(i))/ys.get(i);
                        if(ys.get(i) == 0){
                            relative = Math.abs(y - ys.get(i));
                        }
                        if(relative < Constants.RELATIVE_ERR_CUTOFF){//see if the rho already exists
                            yexists = true;
                            break;//if the y already exists, we do not need to iterate any further in the array
                        }
                    }
                    if(!yexists){
                        ys.add(y);//add the new y to the list if it is not already
                    }
                }
            }
        }
        //sort the x&y coordinates and create arrays out of them
        Collections.sort(xs);
        Collections.sort(ys);
        double[] x_coordinates = new double[xs.size()];
        double[] y_coordinates = new double[ys.size()];
        for(int i = 0; i < x_coordinates.length; i++){
            x_coordinates[i] = xs.get(i);
        }
        for(int i = 0; i < y_coordinates.length; i++){
            y_coordinates[i] = ys.get(i);
        }
        //second pass over the file
        double[][] current = new double[x_coordinates.length][y_coordinates.length]; // This holds the relative current at each point x,y
        try (Scanner scan = new Scanner(new File(filename))) {
            while(scan.hasNextLine()){//scan through the file
                String readline = scan.nextLine();
                if(!readline.startsWith("#")){//ignore "comment" lines
                    String[] readsplit = readline.split(",");
                    double x = Double.parseDouble(readsplit[0]);//read in the X-coordinate
                    double y = Double.parseDouble(readsplit[1]);//read in the Y-coordinate
                    double cur = Double.parseDouble(readsplit[2]);//read in the current
                    int xaddr = FindSmaller(x, x_coordinates) + 1;//determine the x-address
                    int yaddr = FindSmaller(y, y_coordinates) + 1;//determine the y-address
                    
                    current[xaddr][yaddr] = cur;
                }
            }
        }
        SECMImage ret = new SECMImage(x_coordinates, y_coordinates, current);
        return ret;
    }
}
