package seawar.board;

/**
 * A shot onto a board can produce different results.
 * 
 * @author thsc
 */
public enum ShotResults {
    /** A ship was hit but not completly destroyed */
    HIT, 
    
    /**A ship was hit an destroy */
    HIT_DESTROYED, 
    
    /** The final ship was hit and destroyed - game ended*/
    HIT_FINISHED, 
    
    /**Nothing but water was hit */
    HIT_WATER 
}
