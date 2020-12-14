package seawar.board;

import seawar.Game;
import seawar.GameImpl;

/**
 *
 * @author local
 */
public class Root {
    
    public Root() {
    }
    
    final LocalBoardImpl getLocalBoard() {
        return new LocalBoardImpl(GameImpl.createGame());
    }
    
    final RemoteBoardImpl getRemoteBoard() {
        return new RemoteBoardImpl(GameImpl.createGame());
    }
    
    final Game getGame() {
        return GameImpl.createGame();
    }
    
}
