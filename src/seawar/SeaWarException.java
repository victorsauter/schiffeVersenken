package seawar;

/**
 * Root class of all game exceptions.
 * 
 * @author thsc
 */
public class SeaWarException extends Exception {

    /**
     * Creates a new instance of <code>SeaWarException</code> without detail
     * message.
     */
    public SeaWarException() {
    }

    /**
     * Constructs an instance of <code>SeaWarException</code> with the specified
     * detail message.
     *
     * @param msg the detail message.
     */
    public SeaWarException(String msg) {
        super(msg);
    }
}
