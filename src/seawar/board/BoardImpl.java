package seawar.board;

import java.io.IOException;
import seawar.Game;
import seawar.GameStatusException;
import seawar.board.persistency.BoardMemento;

/**
 *
 * @author thsc
 */
class BoardImpl implements Board, BoardViewAccess, Shot {
    /** two dimensional array keeping status of each field 
     dim 1 is column, dim 2 row */
    private FieldStatus[][] fieldStatus;
    protected final Game game;
    
    @Override
    public Game getGame() { return game; }
    
    BoardImpl(Game game, FieldStatus initialStatus) {
        this.game = game;
        
        this.fieldStatus = new FieldStatus[Board.MAX_COLUMN_INDEX+1][Board.MAX_ROW_INDEX+1];
        // intialized any fleid with water
        
        for(int c = Board.MIN_COLUMN_INDEX; c <= Board.MAX_COLUMN_INDEX; c++) {
            for(int r = Board.MIN_ROW_INDEX; r <= Board.MAX_ROW_INDEX; r++) {
                this.fieldStatus[c][r] = initialStatus;
            }
        }
    }
    
    boolean coordinatesOnBoard(int column, int row) {
        return !(column < Board.MIN_COLUMN_INDEX 
                || column > Board.MAX_COLUMN_INDEX
                || row < Board.MIN_ROW_INDEX
                || row > Board.MAX_ROW_INDEX);
    }
    
    /**
     * Should be called prior to operations on the board.Checks for
     * wrong coordinates and throws a BoardException in that case
     * @param column
     * @param row 
     * @throws seawar.board.BoardException if coordinates not inside the board
     */
    protected void check4BoardException(int column, int row) 
            throws BoardException {
            
        if(!this.coordinatesOnBoard(column, row)) {
            StringBuilder b = new StringBuilder();
            b.append("coordinates outside board: allowed ([");
            b.append(Board.MIN_COLUMN_INDEX);
            b.append("..");
            b.append(Board.MAX_COLUMN_INDEX);
            b.append("], [");
            b.append(Board.MIN_ROW_INDEX);
            b.append("..");
            b.append(Board.MAX_ROW_INDEX);
            b.append("])");
            b.append("found: (");
            b.append(column);
            b.append(", ");
            b.append(row);
            b.append(")");
            throw new BoardException(b.toString());
        }
    }

    /**
     * Shot on a field on a board: There are for cases:
     * (1) WATER hit: no field status change, report HIT WATER
     * (2) SHIP hit: field status changes to HIT, report HIT
     * (3) HIT hit: no field status change, report WATER (a second hit is water)
     * (4) SHOT_ON_WATER hit: no field status change, report WATER
     * @param column
     * @param row
     * @return
     * @throws GameStatusException
     * @throws BoardException 
     */
    @Override
    public ShotResults shot(int column, int row) throws GameStatusException, 
            BoardException {

        this.check4BoardException(column, row);
        
        FieldStatus status = this.fieldStatus[column][row];
        switch(status) {
            /* hit water: report this */
            case WATER:
                this.fieldStatus[column][row] = FieldStatus.SHOT_ON_WATER;
            
            /* field already hit - report as water */
            case HIT: 
                
            /* next shot on water - report water again */
            case SHOT_ON_WATER: 
                return ShotResults.HIT_WATER;
                
            /* hit ship, remember hit and report */
            case SHIP: 
                this.fieldStatus[column][row] = FieldStatus.HIT;
                return ShotResults.HIT;
                
            /* hit ship, remember hit and report */
            case UNKNOWN:
                // cannot be decided locally - requires network connection
                // this case mustbe overwritten in RemoteBoard
                return ShotResults.HIT_WATER; // debugging
        }
        
        throw new GameStatusException("serious internal failure: "
                + "field status was neither WATER; HIT, SHOT_ON_WATER, SHIP");
    }

    @Override
    public FieldStatus getFieldStatus(int column, int row) throws BoardException {
        this.check4BoardException(column, row);
        return this.fieldStatus[column][row];
    }
    
    @Override
    public void setFieldStatus(int column, int row, FieldStatus newStatus) 
            throws GameStatusException, BoardException {
        this.check4BoardException(column, row);
        
        this.fieldStatus[column][row] = newStatus;
    }
    
    /**
     * Create coordinate object that follows given on
     * @param coo current position
     * @param horizontal orientation
     * @throws BooardException if following coordinate would be outside the board
     * @return 
     */
    static Coordinates getNextCoordinate(Coordinates coo, boolean horizontal) throws BoardException {
        int nextColumn = coo.getColumn();
        int nextRow = coo.getRow();
        
        if(horizontal) {
            nextColumn++;
            if(nextColumn > Board.MAX_COLUMN_INDEX) {
                throw new BoardException("column would be too high");
            }
        } else {
            nextRow++;
            if(nextRow > Board.MAX_ROW_INDEX) {
                throw new BoardException("row would be too high");
            }
        }
        
        return new CoordinatesImpl(nextColumn, nextRow);
    }

    @Override
    public FieldStatus[][] getFieldStatus() {
        return this.fieldStatus;
    }
    
    @Override
    public void saveStatus(BoardMemento memento) throws IOException {
        memento.saveBoard(this.getFieldStatus());
    }

    @Override
    public void restoreStatus(BoardMemento memento) throws IOException {
        this.fieldStatus = memento.restoreBoard();
    }
    
}
