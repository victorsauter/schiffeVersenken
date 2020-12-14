package seawar;

/**
 * Some actions are only allowed in special status. Objects of this
 * exception are issued whenever a methods in a wrong status is called
 * 
 * @author thsc
 */
public class GameStatusException extends SeaWarException {

    /**
     * Creates a new instance of <code>GameStatusException</code> without detail
     * message.
     */
    public GameStatusException() {
    }

    /**
     * Constructs an instance of <code>GameStatusException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public GameStatusException(String msg) {
        super(msg);
    }
}
