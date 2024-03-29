package io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;
import structures.SECMImage;
import static utility.Search.FindSmaller;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import structures.LSIterationData;

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
                        double relative = Math.abs((x - xs.get(i))/xs.get(i));
                        if(xs.get(i) == 0){
                            relative = Math.abs(x - xs.get(i));
                        }
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
                        double relative = Math.abs((y - ys.get(i))/ys.get(i));
                        if(ys.get(i) == 0){
                            relative = Math.abs(y - ys.get(i));
                        }
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
                    int thingy = xaddr + yaddr*xsize;
                    System.out.println(thingy + ": x: " + x + ", y: " + y);
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
     * vector is the current vector for this iteration. The following two columns 
     * are the Jacobian for this iteration
     * @throws FileNotFoundException If the specified file cannot be found
     * @throws ImproperFileFormattingException If the xy points are either not 
     * regularly spaced.
     */
    public static double[][] read_fitting_iteration_two_param(String filename) throws FileNotFoundException, ImproperFileFormattingException{
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
                        double relative = Math.abs((x - xs.get(i))/xs.get(i));
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
                        double relative = Math.abs((y - ys.get(i))/ys.get(i));
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
     * Parses a file and reads in currents and partial derivatives of the current 
     * for rectilinear, regularly spaced points on the xy plane. The file should 
     * be comma separated with the columns in the following formatted as:
     * x, y, c1 perturbation, c2 perturbation, c3 perturbation, current
     * @param filename The directory and name of the file to be read from
     * @return a 4-column vector matrix represented as a 2D array. The first column 
     * vector is the current vector for this iteration. The following three columns 
     * are the Jacobian for this iteration
     * @throws FileNotFoundException
     * @throws ImproperFileFormattingException 
     */
    public static double[][] read_fitting_iteration_three_param(String filename) throws FileNotFoundException, ImproperFileFormattingException{
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
                        double relative = Math.abs((x - xs.get(i))/xs.get(i));
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
                        double relative = Math.abs((y - ys.get(i))/ys.get(i));
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
        double expc3 = 1;
        //second pass over the file
        double[][] current = new double[xsize*ysize][4]; // This holds the relative current at each point x,y
        try (Scanner scan = new Scanner(new File(filename))) {
            while(scan.hasNextLine()){//scan through the file
                String readline = scan.nextLine();
                if(!readline.startsWith("#")){//ignore "comment" lines
                    String[] readsplit = readline.split(",");
                    double x = Double.parseDouble(readsplit[0]);//read in the X-coordinate
                    double y = Double.parseDouble(readsplit[1]);//read in the Y-coordinate
                    double pc1 = Double.parseDouble(readsplit[2]);//read in the perturbation of the first parameter
                    double pc2 = Double.parseDouble(readsplit[3]);//read in the perturbation of the second parameter
                    double pc3 = Double.parseDouble(readsplit[4]);//read in the perturbation of the third parameter
                    double i = Double.parseDouble(readsplit[5]);//read in the current
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
                    else if(pc3 != 0){
                        type = 3;
                        expc3 = pc3;
                    }
                    current[xaddr + yaddr*xsize][type] = i;
                }
            }
        }
        //overwrite the non-first row with the Jacobian
        for(int r = 0; r < current.length; r++){
            current[r][1] = (current[r][1]-current[r][0])/expc1;
            current[r][2] = (current[r][2]-current[r][0])/expc2;
            current[r][3] = (current[r][3]-current[r][0])/expc3;
        }
        return current;
    }
    
    /**
     * <p>Parses a file and reads in currents and partial derivatives of the current 
     * for rectilinear, regularly spaced points on the xy plane. The file should 
     * be comma separated with the columns in the following formatted as:
     * x, y, c1 perturbation, c2 perturbation, c3 perturbation, current.</p>
     * <p>Optionally mirrors the currents spatially along x and y axes.</p> 
     * @param filename The directory and name of the file to be read from
     * @param mirror_along_x the mirroring policy along the x-direction
     * @param mirror_along_y the mirroring policy along the y-direction
     * @return a 4-column vector matrix represented as a 2D array. The first column 
     * vector is the current vector for this iteration. The following three columns 
     * are the Jacobian for this iteration
     * @throws FileNotFoundException
     * @throws ImproperFileFormattingException 
     */
    public static double[][] read_fitting_iteration_three_param(String filename, boolean mirror_along_x, boolean mirror_along_y) throws FileNotFoundException, ImproperFileFormattingException{
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
                        double relative = Math.abs(((x - xs.get(i))/xs.get(i)));
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
                        if(mirror_along_x && x != 0.0){
                            xs.add(-x);
                        }
                    }
                    boolean yexists = false;
                    for(int i = 0; i< ys.size(); i++){
                        double relative = Math.abs((y - ys.get(i))/ys.get(i));
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
                        if(mirror_along_y && y != 0.0){
                            ys.add(-y);
                        }
                    }
                }
            }
        }
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
        int xsize = xs.size();
        int ysize = ys.size();
        double expc1 = 1;
        double expc2 = 1;
        double expc3 = 1;
        //second pass over the file
        double[][] current = new double[xsize*ysize][4]; // This holds the relative current at each point x,y
        try (Scanner scan = new Scanner(new File(filename))) {
            while(scan.hasNextLine()){//scan through the file
                String readline = scan.nextLine();
                if(!readline.startsWith("#")){//ignore "comment" lines
                    String[] readsplit = readline.split(",");
                    double x = Double.parseDouble(readsplit[0]);//read in the X-coordinate
                    double y = Double.parseDouble(readsplit[1]);//read in the Y-coordinate
                    double pc1 = Double.parseDouble(readsplit[2]);//read in the perturbation of the first parameter
                    double pc2 = Double.parseDouble(readsplit[3]);//read in the perturbation of the second parameter
                    double pc3 = Double.parseDouble(readsplit[4]);//read in the perturbation of the third parameter
                    double i = Double.parseDouble(readsplit[5]);//read in the current
                    int xaddr = FindSmaller(x, x_coordinates) + 1;//determine the x-address
                    int yaddr = FindSmaller(y, y_coordinates) + 1;//determine the y-address
                    int xaddr2 = xaddr;
                    int yaddr2 = yaddr;
                    int thingy = xaddr + yaddr*xsize;
                    System.out.println(thingy + ": x: " + x + ", y: " + y);
                    if(mirror_along_x){
                        xaddr2 = FindSmaller(-x, x_coordinates) + 1;//determine the negative x-address
                    }
                    if(mirror_along_y){
                        yaddr2 = FindSmaller(-y, y_coordinates) + 1;//determine the negative y-address
                    }
                    int type = 0;
                    if(pc1 != 0){
                        type = 1;
                        expc1 = pc1;
                    }
                    else if(pc2 != 0){
                        type = 2;
                        expc2 = pc2;
                    }
                    else if(pc3 != 0){
                        type = 3;
                        expc3 = pc3;
                    }
                    current[xaddr + yaddr*xsize][type] = i;
                    if(mirror_along_x && mirror_along_y){
                        current[xaddr2 + yaddr2*xsize][type] = i;
                        current[xaddr2 + yaddr*xsize][type] = i;
                        current[xaddr + yaddr2*xsize][type] = i;
                    }
                    else if(mirror_along_x){
                        current[xaddr2 + yaddr*xsize][type] = i;
                        thingy = xaddr2 + yaddr*xsize;
                        System.out.println(thingy + ": x: " + -x + ", y: " + y);
                    }
                    else if(mirror_along_y){
                        current[xaddr + yaddr2*xsize][type] = i;
                        thingy = xaddr + yaddr2*xsize;
                        System.out.println(thingy + ": x: " + x + ", y: " + -y);
                    }
                }
            }
        }
        //overwrite the non-first row with the Jacobian
        for(int r = 0; r < current.length; r++){
            current[r][1] = (current[r][1]-current[r][0])/expc1;
            current[r][2] = (current[r][2]-current[r][0])/expc2;
            current[r][3] = (current[r][3]-current[r][0])/expc3;
        }
        return current;
    }
    
    
    
    /**
     * Parses a file and reads in currents and partial derivatives of the current 
     * for rectilinear, regularly spaced points on the xy plane.The file should 
 be comma separated with the columns in the following format:
 x, y, c1 perturbation, c2 perturbation, current
     * @param filename The directory and name of the file to be read from
     * @param mirror_along_x the mirroring policy along the x-direction
     * @param mirror_along_y the mirroring policy along the y-direction
     * @return a 3-column vector matrix represented as a 2D array. The first column 
     * vector is the current vector for this iteration. the following two columns 
     * are the Jacobian for this iteration
     * @throws FileNotFoundException If the specified file cannot be found
     * @throws ImproperFileFormattingException If the xy points are either not 
     * regularly spaced.
     */
    public static double[][] read_fitting_iteration_two_param(String filename, boolean mirror_along_x, boolean mirror_along_y) throws FileNotFoundException, ImproperFileFormattingException{
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
                        double relative = Math.abs((x - xs.get(i))/xs.get(i));
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
                        if(mirror_along_x && x != 0.0){
                            xs.add(-x);
                        }
                    }
                    boolean yexists = false;
                    for(int i = 0; i< ys.size(); i++){
                        double relative = Math.abs((y - ys.get(i))/ys.get(i));
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
                        if(mirror_along_y && y != 0.0){
                            ys.add(-y);
                        }
                    }
                }
            }
        }
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
        int xsize = xs.size();
        int ysize = ys.size();
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
                    int xaddr = FindSmaller(x, x_coordinates) + 1;//determine the x-address
                    int yaddr = FindSmaller(y, y_coordinates) + 1;//determine the y-address
                    int xaddr2 = xaddr;
                    int yaddr2 = yaddr;
                    if(mirror_along_x){
                        xaddr2 = FindSmaller(-x, x_coordinates) + 1;//determine the negative x-address
                    }
                    if(mirror_along_y){
                        yaddr2 = FindSmaller(-y, y_coordinates) + 1;//determine the negative y-address
                    }
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
                    if(mirror_along_x && mirror_along_y){
                        current[xaddr2 + yaddr2*xsize][type] = i;
                        current[xaddr2 + yaddr*xsize][type] = i;
                        current[xaddr + yaddr2*xsize][type] = i;
                    }
                    else if(mirror_along_x){
                        current[xaddr2 + yaddr*xsize][type] = i;
                    }
                    else if(mirror_along_y){
                        current[xaddr + yaddr2*xsize][type] = i;
                    }
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
     * Version of read_secm that expects two additional columns of data between x,y and i. Lines where data in these lines are nonzero are ignored.
     * @param filename The directory and name of the file to be read from
     * @return An SECM image representing the data in the file
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
                        double relative = Math.abs((x - xs.get(i))/xs.get(i));
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
                        double relative = Math.abs((y - ys.get(i))/ys.get(i));
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
                    double pc1 = Double.parseDouble(readsplit[2]);//read in the perturbation of the first parameter
                    double pc2 = Double.parseDouble(readsplit[3]);//read in the perturbation of the second parameter
                    double pc3 = Double.parseDouble(readsplit[4]);//read in the perturbation of the second parameter
                    double i = Double.parseDouble(readsplit[5]);//read in the current
                    int xaddr = FindSmaller(x, x_coordinates) + 1;//determine the x-address
                    int yaddr = FindSmaller(y, y_coordinates) + 1;//determine the y-address
                    if(pc1 == 0 && pc2 == 0 && pc3 == 0){
                        current[xaddr][yaddr] = i;
                    }
                }
            }
        }
        SECMImage ret = new SECMImage(x_coordinates, y_coordinates, current);
        return ret;
    }
    
    /**
     * Reads an SECM image in from a file. Expects the file to be formatted as a 3-column csv with the columns representing x,y,i
     * @param filename The directory and name of the file to be read from
     * @return An SECM image representing the data in the file
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
                        double relative = Math.abs((x - xs.get(i))/xs.get(i));
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
                        double relative = Math.abs((y - ys.get(i))/ys.get(i));
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
    
    /**
     * Reads-in metadata about iterations from an xml file.
     * @param filename The directory and name of the file to be read from
     * @return An array containing information about each iteration and lambda in the file
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws NumberFormatException 
     */
    public static LSIterationData[] read_iteration_metadata(String filename) throws ParserConfigurationException, SAXException, IOException, NumberFormatException{
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(filename);
        Element root_element = document.getDocumentElement();
        NodeList nl = root_element.getElementsByTagName("Iteration");
        int size = nl.getLength();
        
        LSIterationData[] iterations = new LSIterationData[size];
        
        for(int i = 0; i < size; i++){
            Element branch = (Element)nl.item(i);
            String it_nu = getValue(branch, "Iteration_number").strip();
            int iteration_number = Integer.parseInt(it_nu);
            String pa_na = getValue(branch, "Parameter_names").strip();
            String[] parameter_names = pa_na.split("\n");
            String pa_gu = getValue(branch, "Parameter_guesses").strip();
            String[] guesses = pa_gu.split("\n");
            String ja_di = getValue(branch, "Jacobian_diagonal").strip();
            String[] jac = ja_di.split("\n");
            
            int num_params = jac.length;
            
            double[] parameter_guesses = new double[num_params];
            double[] jacobian_diagonal = new double[num_params];
            
            for(int ii = 0; ii < num_params; ii++){
                parameter_names[ii] = parameter_names[ii].strip();
                parameter_guesses[ii] = Double.parseDouble(guesses[ii].strip());
                jacobian_diagonal[ii] = Double.parseDouble(jac[ii].strip());
            }
            
            String lambd = getValue(branch, "Lambda").strip();
            double lambda = Double.parseDouble(lambd);
            String su_sq = getValue(branch, "Sum_of_square_residuals").strip();
            double sum_of_squares = Double.parseDouble(su_sq);
            String data_filepath = getValue(branch, "Data_filepath").strip();
            iterations[i] = new LSIterationData(parameter_guesses);
            iterations[i].data_file_path = data_filepath;
            iterations[i].iteration_number = iteration_number;
            iterations[i].jacobian_diagonal = jacobian_diagonal;
            iterations[i].lambda = lambda;
            iterations[i].parameter_names = parameter_names;
            iterations[i].sum_square_residuals = sum_of_squares;
        }
        
        return iterations;
    }
    
    /**
     * Fetches the value associated with tag under the parent node in the xml tree
     * @param parent the parent node to be searched under
     * @param tag the tag from which a value is to be fetched
     * @return the value associated with tag under parent
     */
    private static String getValue(Element parent, String tag){
        String value = "";
        NodeList nl = parent.getElementsByTagName(tag);
        if (nl.getLength() > 0 && nl.item(0).hasChildNodes()) {
            value = nl.item(0).getFirstChild().getNodeValue();
        }
        return value;
    }
}
