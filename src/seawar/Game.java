package seawar;

import java.io.IOException;
import java.io.PrintStream;
import seawar.board.BoardException;
import seawar.board.BoardViewAccess;
import seawar.board.LocalBoard;
import seawar.board.RemoteBoard;
import seawar.board.ShotResults;

/**
 * That interface is a facade for the sea war game.
 * 
 * @author thsc
 */
public interface Game {
    /**
     * get reference to the remote board - the opponents board.
     * It's a singleton - each call produces the same object reference.
     * @return reference to remote board object
     */
    RemoteBoard getRemoteBoard();
    
    /**
     * Produces reference to the BoardViewAccess interface of the
     * remote board.
     * This interfaces allows accessing the internal data to produce
     * a view.
     * 
     * @return reference to acccess data of remote board
     */
    BoardViewAccess getRemoteBoardAccessView();
    
    /**
     * get reference to the local board.
     * It's a singleton - each call produces the same object reference.
     * @return reference to local board object
     */
    LocalBoard getLocalBoard();

    /**
     * Produces reference to the BoardViewAccess interface of the
     * local board.
     * This interfaces allows accessing the internal data to produce
     * a view.
     * 
     * @return reference to acccess data of local board
     */
    BoardViewAccess getLocalBoardAccessView();
    
    /**
     * 
     * @return current game status
     */
    GameStatus getStatus();
        
    /**
     * Connected with game partner ?
     * @return true if so, false otherwise
     */
    boolean isConnected();
    
    /**
     * Change status to play.
     * @throws GameStatusException if not in prepare status
     */
    void startGame() throws GameStatusException;
    
    /**
     * Set status to end
     * @throws GameStatusException if already ended
     */
    void giveUp() throws GameStatusException;
    
    /**
     * 
     * @return true - game was won, false - not
     * @throws GameStatusException if game is not finished
     */
    boolean won() throws GameStatusException;

    /**
     * Player performs a shots. That call is actually forwarded to the remote
     * boards. Front ends must use this method but not methods 
     * @param column
     * @param row
     * @return
     * @throws GameStatusException
     * @throws BoardException 
     */
    ShotResults shot(int column, int row) throws GameStatusException, BoardException;

    public void setConnection(String remoteHost, int remotePort, int localPort, PrintStream userOutput);
    
    public void disconnect() throws IOException;
}
