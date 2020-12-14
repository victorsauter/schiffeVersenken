package seawar.board;

import seawar.Game;

/**
 *
 * @author thsc
 */
public interface RemoteBoard extends Board {
    static RemoteBoard getBoard(Game game) {
        return new RemoteBoardImpl(game);
    }   
}
