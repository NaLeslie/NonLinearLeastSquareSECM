package structures;

import static utility.Search.FindSmaller;

/**
 *
 * @author Nathaniel
 */
public class SECMImage {
    
    /**
     * Constructs a new instance for <code>SECMImage</code>.
     * @param x_vals the x-coordinates of the elements in the SECM image
     * @param y_vals the y-coordinates of the elements in the SECM image
     * @param current the matrix representing the current measured at every x,y
     */
    public SECMImage(double[] x_vals, double[] y_vals, double[][] current){
        Xvals = new double[x_vals.length];
        System.arraycopy(x_vals, 0, Xvals, 0, x_vals.length);
        Yvals = new double[y_vals.length];
        System.arraycopy(y_vals, 0, Yvals, 0, y_vals.length);
        Current = new double[current.length][current[0].length];
        for(int x = 0; x < current.length; x++){
            for(int y = 0; y < current[0].length; y++){
                Current[x][y] = current[x][y];
            }
        }
    }
    
    /**
     * the x-coordinates of the elements in the SECM image
     */
    private final double[] Xvals;
    
    /**
     * the y-coordinates of the elements in the SECM image
     */
    private final double[] Yvals;
    
    /**
     * the matrix representing the current measured at every x,y
     */
    private final double[][] Current;
    
    /**
     * Returns a cropped SECMImage
     * @param xmin
     * @param xmax
     * @param ymin
     * @param ymax
     * @param inclusive
     * @return 
     */
    public SECMImage getCrop(double xmin, double xmax, double ymin, double ymax, boolean inclusive){
        int xmax_addr = FindSmaller(xmax, Xvals);
        int xmin_addr = FindSmaller(xmin, Xvals);
        int ymax_addr = FindSmaller(ymax, Yvals);
        int ymin_addr = FindSmaller(ymin, Yvals);
        if(inclusive){
            xmax_addr ++;
            ymax_addr ++;
        }
        else{
            if(xmin_addr + 1 <= xmax_addr){
                xmin_addr ++;
            }
            if(ymin_addr + 1 < ymax_addr){
                ymin_addr ++;
            }
        }
        //make sure there is no out-of bounds requests
        if(xmin_addr < 0){
            xmin_addr = 0;
        }
        if(xmax_addr >= Xvals.length){
            xmax_addr = Xvals.length - 1;
        }
        if(ymin_addr < 0){
            ymin_addr = 0;
        }
        if(ymax_addr >= Yvals.length){
            ymax_addr = Yvals.length - 1;
        }
        
        
        double[] new_xvals = new double[xmax_addr - xmin_addr + 1];
        System.arraycopy(Xvals, xmin_addr, new_xvals, 0, xmax_addr - xmin_addr + 1);
        double[] new_yvals = new double[ymax_addr - ymin_addr + 1];
        System.arraycopy(Yvals, ymin_addr, new_yvals, 0, ymax_addr - ymin_addr + 1);
        double[][] new_current = new double[xmax_addr - xmin_addr + 1][ymax_addr - ymin_addr + 1];
        
        for(int x = xmin_addr; x <= xmax_addr; x++){
            for(int y = ymin_addr; y <= ymax_addr; y++){
                new_current[x-xmin_addr][y-ymin_addr] = Current[x][y];
            }
        }
        return new SECMImage(new_xvals, new_yvals, new_current);
    }
    
    /**
     * Performs <a href="https://en.wikipedia.org/wiki/Bilinear_interpolationbilinear">Bilinear interpolation</a> to return an approximate current for any given x,y coordinate.
     * @param x the x-coordinate of the desired current
     * @param y the y-coordinate of the desired current
     * @param out_of_bounds_behaviour Determines how the function will behave is 
     * given an x,y outside of the domain defined by this <code>SECMImage</code>. 
     * For (x,y) outside of the domain of this <code>SECMImage</code>:
     * <ul>
     * <li> <a href="#OUT_OF_BOUNDS_NAN">OUT_OF_BOUNDS_NAN</a> returns <code>Double.NaN</code> 
     * <li> <a href="#OUT_OF_BOUNDS_EXTRAPOLATE">OUT_OF_BOUNDS_EXTRAPOLATE</a> 
     * returns the closest extreme values of the currents held by this <code>SECMImage</code>.
     * </ul>
     * <a href="#OUT_OF_BOUNDS_EXTRAPOLATE">OUT_OF_BOUNDS_EXTRAPOLATE</a> is the default state of this function.
     * @return If the desired (x,y) is within the domain of this <code>SECMImage</code>, the bilinear interpolation 
     * of the current at this point will be returned. Otherwise, the return value will depend on the behaviour set 
     * by <code>out_of_bounds_behaviour</code>.
     */
    public double getCurrent(double x, double y, int out_of_bounds_behaviour){
        int x1 = FindSmaller(x, Xvals);
        int y1 = FindSmaller(y, Yvals);
        int x2 = x1 + 1;
        int y2 = y1 + 1;
        //handle out of bounds
        if(x1 < 0 || y1 < 0 || x2 >= Current.length || y2 >= Current[0].length){
            if(out_of_bounds_behaviour == OUT_OF_BOUNDS_NAN){
                return Double.NaN;
            }
            if(x1 < 0){
                if(y1 < 0){
                    return Current[0][0];
                }
                else if (y2 <= Current[0].length){
                    return Current[0][Current[0].length - 1];
                }
                else{
                    return Current[0][y1];
                }
            }
            else if (x2 >= Current.length){
                if(y1 < 0){
                    return Current[Current.length - 1][0];
                }
                else if (y2 <= Current[0].length){
                    return Current[Current.length - 1][Current[0].length - 1];
                }
                else{
                    return Current[Current.length - 1][y1];
                }
            }
            else{
                if(y1 < 0){
                    return Current[x1][0];
                }
                else if (y2 <= Current[0].length){
                    return Current[x1][Current[0].length - 1];
                }
                else{
                    //impossible case
                    return 0;
                }
            }
        }
        //bilinear interpolation
        double x_coord_1 = Xvals[x1];
        double x_coord_2 = Xvals[x2];
        double y_coord_1 = Yvals[y1];
        double y_coord_2 = Yvals[y2];
        
        double x_step = x_coord_2 - x_coord_1;
        double y_step = y_coord_2 - y_coord_1;
        
        double y_weighted_at_x1 = ((y - y_coord_1)*Current[x1][y2] + (y_coord_2 - y)*Current[x1][y1])/y_step;
        double y_weighted_at_x2 = ((y - y_coord_1)*Current[x2][y2] + (y_coord_2 - y)*Current[x2][y1])/y_step;
        
        return ((x - x_coord_1)*y_weighted_at_x2 + (x_coord_2 - x)*y_weighted_at_x1)/x_step;
    }
    
    /**
     * Performs <a href="https://en.wikipedia.org/wiki/Bilinear_interpolationbilinear">
     * Bilinear interpolation</a> to return a matrix of approximate current for 
     * every x-y coordinate combination.
     * 
     * @param x the x-coordinates of the desired current matrix
     * @param y the y-coordinates of the desired current matrix
     * @param out_of_bounds_behaviour Determines how the function will behave is 
     * given an x,y outside of the domain defined by this <code>SECMImage</code>. 
     * For (x,y) outside of the domain of this <code>SECMImage</code>:
     * <ul>
     * <li> <a href="#OUT_OF_BOUNDS_NAN">OUT_OF_BOUNDS_NAN</a> returns 
     * <code>Double.NaN</code> 
     * <li> <a href="#OUT_OF_BOUNDS_EXTRAPOLATE">OUT_OF_BOUNDS_EXTRAPOLATE</a> 
     * returns the closest extreme values of the currents held by this 
     * <code>SECMImage</code>.
     * </ul>
     * <a href="#OUT_OF_BOUNDS_EXTRAPOLATE">OUT_OF_BOUNDS_EXTRAPOLATE</a> is the 
     * default state of this function.
     * 
     * @return a matrix of size <code>[x.length][y.length]</code> that contains 
     * the bilinearly interpolated currents for every possible (x,y) pair. If 
     * (x,y) falls outside the domain of this <code>SECMImage</code>, the value 
     * at (x,y) will be determined by <code>out_of_bounds_behaviour</code>.
     */
    public double[][] getCurrents(double[] x, double[] y, int out_of_bounds_behaviour){
        double[][] currents = new double[x.length][y.length];
        for(int i = 0; i < x.length; i++){
            for(int j = 0; j < y.length; j++){
                currents[i][j] = getCurrent(x[i], y[j], out_of_bounds_behaviour);
            }
        }
        return currents;
    }
    
    /**
     * Fetches the raw current data for this image
     * @return the raw currents as an array [x-coordinate][y-coordinate]
     */
    public double[][] getDataCurrent(){
        return Current;
    }
    
    /**
     * Fetches the x-positions that correspond to the x-addresses in the raw current data
     * @return the x-positions represented by this SECM image
     */
    public double[] getDataXvals(){
        return Xvals;
    }
    
    /**
     * Fetches the y-positions that correspond to the x-addresses in the raw current data
     * @return the y-positions represented by this SECM image
     */
    public double[] getDataYvals(){
        return Yvals;
    }
    
    /**
     * Expands the SECM image by reflecting it along both axes. 
     * This method reflects across x=0 and y=0. 
     * If data in the SECM image straddles these boundaries, an error will be thrown.
     * <p>Example:</p>
     * <p>  Input</p>
     * <p>    x data: -2 -1  0</p>
     * <p>    y data:  1  3  5</p>
     * <p>  Output</p>
     * <p>    x data: -2 -1  0  1  2</p>
     * <p>    y data: -5 -3 -1  1  3  5</p>
     * @return the reflected SECM image
     * @throws CannotMirrorException 
     */
    public SECMImage getMirrorExpanded() throws CannotMirrorException{
        //find the domain
        double xmax = getXMax();
        double xmin = getXMin();
        double ymax = getYMax();
        double ymin = getYMin();
        if(xmax*xmin < 0 || ymax*ymin < 0){
            throw new CannotMirrorException("Mirroring may cause data overlap");
        }
        //initialize the new arrays
        int xlen = Xvals.length*2;
        int ylen = Yvals.length*2;
        if(xmax*xmin == 0.0){
            xlen --;
        }
        if(ymax*ymin == 0.0){
            ylen --;
        }
        double[] new_xvals = new double[xlen];
        double[] new_yvals = new double[ylen];
        double[][] new_currents = new double[xlen][ylen];
        //set-up scan and offset directions for copying the arrays. Default to 
        //the 3rd quadrant -x,-y being where this SECM image is located.
        // 2 | 1
        // --+--
        // 3 | 4
        int x_addr_offset = 0;
        int y_addr_offset = 0;
        int x_scan_dir = 1;
        int y_scan_dir = 1;
        if(xmin >= 0){//quadrant one or four
            x_addr_offset = Xvals.length - 1;
            x_scan_dir = -1;
        }
        if(ymin >= 0){//quadrant one or two
            y_addr_offset = Yvals.length - 1;
            y_scan_dir = -1;
        }
        //copy to the arrays
        for(int x = 0; x < Xvals.length; x++){
            new_xvals[x] = - Xvals[x_addr_offset + x_scan_dir*x];
            new_xvals[xlen - 1 - x] = Xvals[x_addr_offset + x_scan_dir*x];
            for(int y = 0; y < Yvals.length; y++){
                new_currents[x][y] = Current[x_addr_offset + x_scan_dir*x][y_addr_offset + y_scan_dir*y];//Q3
                new_currents[xlen - 1 - x][y] = Current[x_addr_offset + x_scan_dir*x][y_addr_offset + y_scan_dir*y];//Q4
                new_currents[x][ylen - 1 - y] = Current[x_addr_offset + x_scan_dir*x][y_addr_offset + y_scan_dir*y];//Q2
                new_currents[xlen - 1 - x][ylen - 1 - y] = Current[x_addr_offset + x_scan_dir*x][y_addr_offset + y_scan_dir*y];//Q1
            }
        }
        for(int y = 0; y < Yvals.length; y++){
            new_yvals[y] = - Yvals[y_addr_offset + y_scan_dir*y];
            new_yvals[ylen - 1 - y] = Yvals[y_addr_offset + y_scan_dir*y];
        }
        return new SECMImage(new_xvals, new_yvals, new_currents);
    }
    
    /**
     * Expands the SECM image by reflecting it along the specified axes. 
     * This method reflects across x=0 and y=0, if applicable. 
     * If data in the SECM image straddles these boundaries, an error will be thrown.
     * if <code>reflect_x</code> and <code>reflect_y</code> are both false, 
     * @param reflect_x Controls whether or not x-coordinate reflection occurs
     * @param reflect_y Controls whether or not y-coordinate reflection occurs
     * @return the reflected SECM image
     * @throws CannotMirrorException 
     */
    public SECMImage getMirrorExpanded(boolean reflect_x, boolean reflect_y) throws CannotMirrorException{
        //find the domain
        double xmax = getXMax();
        double xmin = getXMin();
        double ymax = getYMax();
        double ymin = getYMin();
        if((xmax*xmin < 0 && reflect_x) || (ymax*ymin < 0 && reflect_y)){
            throw new CannotMirrorException("Mirroring may cause data overlap");
        }
        //initialize the new arrays
        int xlen = Xvals.length;
        int ylen = Yvals.length;
        if(reflect_x){
            xlen = Xvals.length*2;
            if(xmax*xmin == 0.0){
                xlen --;
            }
        }
        if(reflect_y){
            ylen = Yvals.length*2;
            if(ymax*ymin == 0.0){
                ylen --;
            }
        }
        double[] new_xvals = new double[xlen];
        double[] new_yvals = new double[ylen];
        double[][] new_currents = new double[xlen][ylen];
        //set-up scan and offset directions for copying the arrays. Default to 
        //the 3rd quadrant -x,-y being where this SECM image is located.
        int x_addr_offset = 0;
        int y_addr_offset = 0;
        int x_scan_dir = 1;
        int y_scan_dir = 1;
        if(xmin >= 0 && reflect_x){//quadrant one or four
            x_addr_offset = Xvals.length - 1;
            x_scan_dir = -1;
        }
        if(ymin >= 0 && reflect_y){//quadrant one or two
            y_addr_offset = Yvals.length - 1;
            y_scan_dir = -1;
        }
        //copy to the arrays
        for(int x = 0; x < Xvals.length; x++){
            new_xvals[x] = - Xvals[x_addr_offset + x_scan_dir*x];
            new_xvals[xlen - 1 - x] = Xvals[x_addr_offset + x_scan_dir*x];
            for(int y = 0; y < Yvals.length; y++){
                new_currents[x][y] = Current[x_addr_offset + x_scan_dir*x][y_addr_offset + y_scan_dir*y];//Q3
                if(reflect_x){
                    new_currents[xlen - 1 - x][y] = Current[x_addr_offset + x_scan_dir*x][y_addr_offset + y_scan_dir*y];//Q4
                }
                if(reflect_y){
                    new_currents[x][ylen - 1 - y] = Current[x_addr_offset + x_scan_dir*x][y_addr_offset + y_scan_dir*y];//Q2
                }
                if(reflect_x && reflect_y){
                    new_currents[xlen - 1 - x][ylen - 1 - y] = Current[x_addr_offset + x_scan_dir*x][y_addr_offset + y_scan_dir*y];//Q1
                }
            }
        }
        for(int y = 0; y < Yvals.length; y++){
            new_yvals[y] = - Yvals[y_addr_offset + y_scan_dir*y];
            if(reflect_y){
                new_yvals[ylen - 1 - y] = Yvals[y_addr_offset + y_scan_dir*y];
            }
        }
        return new SECMImage(new_xvals, new_yvals, new_currents);
    }
    
    
    /**
     * Re-scales the currents in the image such that the lowest current will be zero, and the highest current will be 1
     * @return the re-scaled SECM image
     */
    public SECMImage getNormalized(){
        double min = Current[0][0];
        double max = Current[0][0];
        double[][] new_current = new double[Current.length][Current[0].length];
        for(int x = 0; x < Current.length; x++){
            for(int y = 0; y < Current[0].length; y++){
                if(Current[x][y] < min){
                    min = Current[x][y];
                }
                else if(Current[x][y] > max){
                    max = Current[x][y];
                }
            }
        }
        double amplitude = max - min;
        for(int x = 0; x < Current.length; x++){
            for(int y = 0; y < Current[0].length; y++){
                new_current[x][y] = (Current[x][y] - min)/amplitude;
            }
        }
        return new SECMImage(Xvals, Yvals, new_current);
    }
    
    /**
     * Produces a version of this SECM image that has had its position data changed by the given offsets.
     * The offsets are added to every x and y position represented by this SECM image
     * @param x_offset the quantity to be added to x-positions
     * @param y_offset the quantity to be added to y-positions
     * @return The result of all of the x and y-coordinates of this SECM image being increased by <code>x_offset</code> and <code>y_offset</code> respectively.
     */
    public SECMImage getOffset(double x_offset, double y_offset){
        double[][] new_currents = new double[Xvals.length][Yvals.length];
        double[] new_xvals = new double[Xvals.length];
        double[] new_yvals = new double[Yvals.length];
        
        for(int x  = 0; x < Xvals.length; x++){
            new_xvals[x] = Xvals[x] + x_offset;
            System.arraycopy(Current[x], 0, new_currents[x], 0, Yvals.length);
        }
        for(int y = 0; y < Yvals.length; y++){
            new_yvals[y] = Yvals[y] + y_offset;
        }
        return new SECMImage(new_xvals, new_yvals, new_currents);
    }
    
    /**
     * Fetches the smallest x-coordinate represented by the SECM image
     * @return the smallest x-coordinate represented by the SECM image
     */
    public double getXMin(){
        return Xvals[0];
    }
    
    /**
     * Fetches the largest x-coordinate represented by the SECM image
     * @return the largest x-coordinate represented by the SECM image
     */
    public double getXMax(){
        return Xvals[Xvals.length - 1];
    }
    
    /**
     * Fetches the smallest y-coordinate represented by the SECM image
     * @return the smallest y-coordinate represented by the SECM image
     */
    public double getYMin(){
        return Yvals[0];
    }
    
    /**
     * Fetches the largest y-coordinate represented by the SECM image
     * @return the largest y-coordinate represented by the SECM image
     */
    public double getYMax(){
        return Yvals[Yvals.length - 1];
    }
    
    /**
     * Linearizes the 2D-current map using (starting with the x-direction
     * <p>Example:</p>
     * <p>  1 3 4</p>
     * <p>  5 7 6 -> x</p>
     * <p>  9 2 8</p>
     * <p>    |  </p>
     * <p>    V y</p>
     * <p>Becomes:</p>
     * <p>  1 3 4 5 7 6 9 2 8</p>
     * @return the currents of this SECM image as a 1-D array
     */
    public double[] linearize(){
        double[] lin = new double[Xvals.length*Yvals.length];
        for(int x = 0; x < Xvals.length; x++){
            for(int y = 0; y < Yvals.length; y++){
                lin[x + y*Xvals.length] = Current[x][y];
            }
        }
        return lin;
    }
    
    /**
     * Subtracts SECM images
     * Uses 'this' as the minuend and 'subtrahend' as the subtrahend
     * @param subtrahend the subtrahend of the subtraction
     * @param out_of_bounds_behaviour used to determine what will happen if currents outside of 'this' SECM image will be handled, if applicable.
     * @return the result of this - subtrahend. Will have the same points in space as the subtrahend
     */
    public SECMImage subtract(SECMImage subtrahend, int out_of_bounds_behaviour){
        double[][] minuend_current = getCurrents(subtrahend.Xvals, subtrahend.Yvals, out_of_bounds_behaviour);
        for(int x = 0; x < subtrahend.Xvals.length; x++){
            for(int y = 0; y < subtrahend.Yvals.length; y++){
                minuend_current[x][y] -= subtrahend.Current[x][y];
            }
        }
        return new SECMImage(subtrahend.Xvals, subtrahend.Yvals, minuend_current);
    }
    
    
    /**
     * <a id="OUT_OF_BOUNDS_EXTRAPOLATE"></a>
     * Sets the behaviour of getCurrent(s) functions to return their closest extreme value if out-of-domain coordinates are requested.
     */    
    public static final int OUT_OF_BOUNDS_EXTRAPOLATE = 0;
    
    /**
     * <a id="OUT_OF_BOUNDS_NAN"></a>
     * Sets the behaviour of getCurrent(s) functions to return <code>Double.NaN</code> if out-of-domain coordinates are requested.
     */
    public static final int OUT_OF_BOUNDS_NAN = 1;
}
