package seaware.view.console;

/**
 * Describes methods for that very old-fashioned console I/O human 
 * users interface.
 * 
 * @author thsc
 */
public interface BoardCommands {
    String PRINT_LOCAL = "pl";
    String PRINT_REMOTE = "pr";
    String SHOT = "o";
    String SET_SHIP = "s";
    String REMOVE_SHIP = "r";
    String START_GAME = "start";
    String GIVE_UP = "giveup";
    String TALK = "talk";
    String GET_STATUS = "status";
    String EXIT = "exit";
    String SAVE = "save";
    String RESTORE = "restore";
    String CONNECT = "connect";
    String FILL = "fill";
    
    String HIT_STRING = "Hit but not destroyed yet";
    String HIT_DESTROYED_STRING = "Hit and ship destroyed";
    String HIT_FINISHED_STRING = "Game over!";
    String HIT_WATER_STRING = "Water - you are under fire now";
    String SAVED_STRING = "Boards saved";
    String RESTORED_STRING = "Boards restored";
    
    String WON_STRING = "you won";
    String LOST_STRING = "you lost";
    
    /**
     * Run the game - wait for input, process it and present output to
     * human users.
     */
    void runGame();
    
    /**
     * Print out user commands
     */
    void printUsage();
    
    /**
     * Print out help text for a specific user command.
     * 
     * @param cmdString
     * @param comment 
     */
    void printUsage(String cmdString, String comment); 
}
