/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package structures;

/**
 *
 * @author Nathaniel
 */
public class Position {
    
    public Position(double x_coord, double y_coord, double angle_of_rotation){
        x = x_coord;
        y = y_coord;
        angle = angle_of_rotation;
    }
    
    public double getX(){
        return x;
    }
    
    public double getY(){
        return y;
    }
    
    public double getAngle(){
        return angle;
    }
    
    private final double x;
    
    private final double y;
    
    private final double angle;
    
}
