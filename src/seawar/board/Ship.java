package seawar.board;

/**
 *
 * @author thsc
 */
public interface Ship {
    /**
     * @return current ship status
     */
    public ShipStatus getStatus();
    
    /**
     * Return wether a ship is already place on the board
     * @return true - if already set, false otherwise
     */
    public boolean isSet();
    
    /**
     * Remove ship from board. Ship removes its coordinates. Nothing
     * happens if this ship was not set at all
     */
    public void remove();

    /**
     * Put ship on the board.NOTE: It is assumed that this vessel can 
 be placed on the board with those coordinates and orientation. This check must be done be caller of the method
     * @param column
     * @param row
     * @param horizontal 
     * @throws seawar.board.BoardException if ship wouldn't fit on the board
     */
    public void putShip(int column, int row, boolean horizontal) throws BoardException;
    
    /**
     * @return coordinates of that ship
     * @throws seawar.board.BoardException if not set
     */
    Coordinates[] getCoordinates() throws BoardException;

    public ShotResults shot(int column, int row) throws BoardException;
    
    /**
     * length of that ship
     * @return 
     */
    int getLength();
}
