/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import structures.SECMImage;

/**
 *
 * @author Nathaniel
 */
public class Output {
    public static void writeSECMImage(String filepath, double[] x_vals, double[] y_vals, double[][] currents) throws IOException{
        File f = new File(filepath);
        f.createNewFile();
        try (PrintWriter pw = new PrintWriter(f)){
            pw.print("#x,y,i");
            for(int x = 0; x < x_vals.length; x++){
                for(int y = 0; y < y_vals.length; y++){
                    pw.print("\n" + x_vals[x] + "," + y_vals[y] + "," + currents[x][y]);
                }
            }
        }
    }
    
    public static void writeSECMImage(String filepath, SECMImage data) throws IOException{
        double[] x_vals = data.getDataXvals();
        double[] y_vals = data.getDataYvals();
        double[][] currents = data.getDataCurrent();
        File f = new File(filepath);
        f.createNewFile();
        try (PrintWriter pw = new PrintWriter(f)){
            pw.print("#x,y,i");
            for(int x = 0; x < x_vals.length; x++){
                for(int y = 0; y < y_vals.length; y++){
                    pw.print("\n" + x_vals[x] + "," + y_vals[y] + "," + currents[x][y]);
                }
            }
        }
    }
}
