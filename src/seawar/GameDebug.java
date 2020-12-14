package seawar;

/**
 *
 * @author thsc
 */
public interface GameDebug {
    static Game getGameInStatus(GameStatus status) {
        return new GameImpl(status);
    }
    
    static GameDebug getGameDebug(Game game) {
        if(game instanceof GameDebug) {
            return (GameDebug) game;
        }
        
        return null;
    }
    
    void setStatus(GameStatus status);
}
