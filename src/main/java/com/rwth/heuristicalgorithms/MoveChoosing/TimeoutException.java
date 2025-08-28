package com.rwth.heuristicalgorithms.MoveChoosing;
/**
 * Exception used for signalling and imminent timeout. Offers no Functionality
 */
public class TimeoutException extends Exception {
    /**
     * standard constructor. Offers no custom Functionality
     */
    public TimeoutException(){

    }

    /**
     * standard constructor. Offers no custom Functionality
     * @param message passed to super
     */
    public TimeoutException(String message){
        super(message);    
    }

    /**
     * standard constructor. Offers no custom Functionality
     * @param cause passed to super
     */
    public TimeoutException(Throwable cause){
        super(cause);
    }

    /**
     * standard constructor. Offers no custom Functionality
     * @param message passed to super
     * @param cause passed to super
     */
    public TimeoutException(String message,Throwable cause){
        super(message, cause);
    }
    
}
