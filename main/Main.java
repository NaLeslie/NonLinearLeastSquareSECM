package main;

import fitting.ImageAlign;
import static fitting.NonlinearLeastSquares.*;
import static io.Input.*;
import static io.Output.*;
import java.io.File;
import java.util.Scanner;
import static main.Demo.*;
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
            //setup_experimental_image();
            //one_LRK_lm_iteration_exp();
            //one_LRK_lm_iteration();
            //error_surface();
            
            //setup_edge_image();
            //one_LK_lm_iteration_edge();
            
            one_lm_iteration();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
}
