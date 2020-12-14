package seawar.board;

import seawar.Game;
import seawar.GameStatusException;

/**
 *
 * @author thsc
 */
public interface LocalBoard extends Board {
    static LocalBoard getBoard(Game game) {
        return new LocalBoardImpl(game);
    }
    
    /**
     * Return a ship that is not yet set on the board. According to
     * the (German) wikipedia description there are:
     * 1 ship length 5 
     * 2 ship length 4
     * 3 ship length 3 
     * 4 ship length 2 
     * @param length of the ship
     * @return
     * @throws BoardException if no such ship exists
     */
    Ship getUnsetShip(int length) throws BoardException;
    
    int getNumberShipsNotSet(int length) throws BoardException;
    
    /**
     * @return true if all ships are set, false otherwise 
     */
    boolean allShipsSet();
    
    /**
     * Get ship object (if any on that position).An object is return
            regardless of ship status (hit, sunk)
     * @param column
     * @param row
     * @return ship object - can be null
     * @throws BoardException 
     */
    Ship getShip(int column, int row) throws BoardException;
    
    /**
     * Take ship from bord - nothing happens if ship not on the board
     * @param ship ship to take away
     * @throws GameStatusException will be thrown if status is not PREPARE
     */
    void removeShip(Ship ship) throws GameStatusException;
    
   /**
    * Put ship on the board.
    * @param ship ship to place
    * @param column index of first coordinate
    * @param row index of first coordinate
    * @param horizontal true if ship is to be place horizontal / false vertical
    * @throws BoardException if coordinates outside the board
    * @throws GameStatusException wrong game stats
    */ 
    void putShip(Ship ship, int column, int row, boolean horizontal) throws BoardException, 
            GameStatusException;
    
}
