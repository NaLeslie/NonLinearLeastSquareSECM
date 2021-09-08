package structures;

/**
 *
 * @author Nathaniel
 */
public class Position {
    
    /**
     * 
     * @param x_coord
     * @param y_coord
     * @param angle_of_rotation 
     */
    public Position(double x_coord, double y_coord, double angle_of_rotation){
        x = x_coord;
        y = y_coord;
        angle = angle_of_rotation;
    }
    
    /**
     * 
     * @return 
     */
    public double getX(){
        return x;
    }
    
    /**
     * 
     * @return 
     */
    public double getY(){
        return y;
    }
    
    /**
     * 
     * @return 
     */
    public double getAngle(){
        return angle;
    }
    
    /**
     * 
     */
    private final double x;
    
    /**
     * 
     */
    private final double y;
    
    /**
     * 
     */
    private final double angle;
    
    /**
     * 
     * @return 
     */
    @Override
    public String toString(){
        return x + ", " + y + ", " + angle;
    }
    
}
