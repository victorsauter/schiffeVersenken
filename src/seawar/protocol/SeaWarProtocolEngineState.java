package seawar.protocol;

/**
 *
 * @author thsc
 */
public enum SeaWarProtocolEngineState {
    NOT_READY, // game is not yet ready to play
    READY, // game is ready to play
    READY_SEND, // game is ready to play, random number was sent
    READY_RECEIVED, // game is ready to play, random number was received
    READY_EXCHANGED, // game is ready to play, random number are exchanged
    ACTIVE, // game is active, not shot issued yet
    ACTIVE_SHOT, // game is active, shot was issued
    ACTIVE_GOT_STATUS, // game is active, shot was issued, status received
    PASSIVE, // game is in passive mode, no shot received now
    PASSIVE_SHOT, // game is in passive mode, shot received
    PASSIVE_SENT_STATUS, // game is in passive mode, shot received, status sent
    LOST, // lost game
    WON // won game
}
