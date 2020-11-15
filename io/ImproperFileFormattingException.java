package io;

/**
 * Used to indicate that something is wrong with the file.
 * @author Nathaniel
 */
public class ImproperFileFormattingException extends Exception{
    /**
     * Creates a new instance of ImproperFileFormattingException. 
     */
    public ImproperFileFormattingException(){
        super();
    }
    
    /**
     * Creates a new instance of ImproperFileFormattingException.
     * @param Message The Message to be included when this exception is thrown.
     */
    public ImproperFileFormattingException(String Message){
        super(Message);
    }
}
