package seawar;

/**
 * That game has some stati which are deklared in this enumation
 * @author thsc
 */
public enum GameStatus {
    /**
    prepation phase - ships are set, anything can be changed, no
    shooting allowed.
    */
    PREPARE,
    
    /** play status activ. Shooting allowed but no further movement of ships */
    PLAY_ACTIVE,
    
    /** play status. Shooting not allowed but no further movement of ships, wait
     for enemy fire */
    PLAY_PASSIVE,
    
    /** Game ended. Someone won */
    END
}
