package structures;

/**
 *
 * @author Nathaniel
 */
public class CannotMirrorException extends Exception{
    /**
     * Creates a new instance of CannotMirrorException. 
     */
    public CannotMirrorException(){
        super();
    }
    
    /**
     * Creates a new instance of CannotMirrorException.
     * @param Message The Message to be included when this exception is thrown.
     */
    public CannotMirrorException(String Message){
        super(Message);
    }
}
