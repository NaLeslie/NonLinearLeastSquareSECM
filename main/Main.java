package main;

import static main.Demo.*;

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
