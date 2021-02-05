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
