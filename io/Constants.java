package io;

/**
 *
 * @author Nathaniel
 */
public class Constants {
    /**
     * The relative error between two values that will be interpreted as noise introduced by floating point imprecision.
     */
    public static final double RELATIVE_ERR_CUTOFF = 0.0001;
    /**
     * The complement of the relative error between two values that will be interpreted as noise introduced by floating point imprecision.
     */
    public static final double COMP_RELATIVE_ERR_CUTOFF = 1 - RELATIVE_ERR_CUTOFF;
}
