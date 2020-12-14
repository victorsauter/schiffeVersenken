package seawar.board;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import seawar.Game;
import seawar.GameStatus;
import seawar.GameStatusException;
import seawar.board.persistency.BoardMemento;

/**
 *
 * @author thsc
 */
public class LocalBoardImpl extends BoardImpl implements LocalBoard {
    
    /** 
     * object references of each ship is stored in that matrix
     */
    private Ship[][] shipsOnBoard;

    /** Ships belonging to that board
     * ships[0] contains 4 ships length 2
     * ships[1] contains 3 ships length 3
     * ships[2] contains 2 ships length 4
     * ships[3] contains 1 ship length 5
     */
    private Ship[][] ships;
    
    public LocalBoardImpl(Game game) {
        super(game, FieldStatus.WATER);
        this.createUnsetShips();
    }
    
    private void createUnsetShips() {
        this.shipsOnBoard = 
                new Ship[Board.MAX_COLUMN_INDEX+1][Board.MAX_ROW_INDEX+1];
        
        this.ships = new Ship[LocalBoard.MAX_SHIP_LENGTH-1][];
        
        // create ships.. start with little ones
        for(int length = MIN_SHIP_LENGTH; length <= MAX_SHIP_LENGTH; length++) {
            // create array holding ships with same length
            Ship[] shipArray = new Ship[this.getMaxShipNumber(length)];
            
            // add actual ship objects with that length
            for(int i = 0; i < this.getMaxShipNumber(length); i++) {
                shipArray[i] = new ShipImpl(length);
            }
            
            // keep that new list in overall ships list
            this.ships[length-MIN_SHIP_LENGTH] = shipArray;
        }
    }
    
    /**
     * Return maximal number of ships with that length
     * @param length
     * @return 
     */
    private int getMaxShipNumber(int length) {
        return MAX_SHIP_LENGTH+1 - length;
    }
    
    private Ship[] getShipsArray(int length) throws BoardException {
        if(length > MAX_SHIP_LENGTH || length < MIN_SHIP_LENGTH) 
            throw new BoardException("length outside allowed range: " + length);
        
        return this.ships[length-MIN_SHIP_LENGTH];
    }
    
    @Override
    public Ship getUnsetShip(int length) throws BoardException {
        Ship[] shipsArray = this.getShipsArray(length);
        
        for(int i = 0; i < this.getMaxShipNumber(length); i++) {
            if(!shipsArray[i].isSet()) {
                return shipsArray[i];
            }
        }
        
        throw new BoardException("no ship left with length: " + length);
    }
    
    
    @Override
    public int getNumberShipsNotSet(int length) throws BoardException {
        int number = 0;
        Ship[] shipsArray = this.getShipsArray(length);
        
        for(Ship ship : shipsArray) {
            if(!ship.isSet()) number++;
        }
        
        return number;
    }

    @Override
    public Ship getShip(int column, int row) throws BoardException {
        this.check4BoardException(column, row);
        
        if(this.shipsOnBoard[column][row] == null) {
            throw new BoardException("no ship on those coordinates");
        }
        
        return this.shipsOnBoard[column][row];
    }
    
    /**
     * Keeps all field in sync with ship movement.
     * @param coordinates on which something happens
     * @param put true: ship is placed on those coordinates. false removed
     * @throws GameStatusException
     * @throws BoardException 
     */
    private void moveShipOnFields(Ship ship, boolean put) 
            throws GameStatusException, BoardException {
        
        if(ship == null) throw new BoardException("no ship");
        Coordinates[] coordinates = ship.getCoordinates();
        
        // ship is put - we set SHIP on the field, otherwise WATER
        FieldStatus status = put ? FieldStatus.SHIP : FieldStatus.WATER;
        
        // if put we remember object reference - otherwise remove it
        Ship shipToSet = put ? ship : null;
        
        for (Coordinates coordinate : coordinates) {
            // sync field status
            this.setFieldStatus(
                    coordinate.getColumn(), 
                    coordinate.getRow(), 
                    status);
        
            // sync ships on board
            this.shipsOnBoard[coordinate.getColumn()][coordinate.getRow()] =
                    shipToSet;
        }
    }

    @Override
    public void removeShip(Ship ship) throws GameStatusException {
                
        if(this.game.getStatus() != GameStatus.PREPARE) {
            throw new GameStatusException("cannot remove ships - already playing or finished");
        }

        try {
            this.moveShipOnFields(ship, false);
        } catch (BoardException ex) {
            // cannot occure - tested when ship was placed
            throw new GameStatusException("serious internal error: ship was placed"
                    + "on wrong coordinates");
        }
        
        ship.remove();
    }
    
    
    
    private boolean isShipOnCoordinates(List<Coordinates> coos) throws BoardException {
        for(Coordinates coo : coos) {
            FieldStatus status = 
                    this.getFieldStatus(coo.getColumn(), coo.getRow());
            
            if(status == FieldStatus.SHIP) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public void putShip(Ship ship, int column, int row, boolean horizontal) 
            throws BoardException, GameStatusException {
        
        if(this.game.getStatus() != GameStatus.PREPARE) {
            throw new GameStatusException("cannot put ships - already playing or finished");
        }
        
        this.check4BoardException(column, row);
        
        // check for existing ships too close neighbours.
        List<Coordinates> cooList = new ArrayList<>();
        
        // first - put future coordinates into that list
        
        // BoardException would lead to method abortion - ok: vessel would be 
        // on board.
        Coordinates coo = new CoordinatesImpl(column, row);
        cooList.add(coo);
        // start with index 1: one coordinate is already handled
        for(int i = 1; i < ship.getLength(); i++) {
            coo = BoardImpl.getNextCoordinate(coo, horizontal);
            cooList.add(coo);
        }
        
        
        /* v vessel, (V first coordinate, v next ones)
        // S stern (Heck), B bow (Bug), 
        // s starboard (Steuerbord), p portside (Backbord)
        example : ship length = 3
        horizontal = true

         sss
        BVvvS
         ppp
        
        horizontal = false = vertical
         B
        pVs
        pvs
        pvs
         S
        
        There must no other vessel within S, s, B and p
        */
        // bow (B) Bug
        int columnBow = horizontal ? column - 1 : column;
        int rowBow = horizontal ? row : row - 1;
        
        // if those coordinates are still on board (wordplay :-)) :/) - add it.
        if(this.coordinatesOnBoard(columnBow, rowBow)) {
            cooList.add(new CoordinatesImpl(columnBow, rowBow));
        }
        
        // stern (S) Heck
        int columnStern = horizontal ? column + ship.getLength() : column;
        int rowStern = horizontal ? row : row  + ship.getLength();
        
        // if those coordinates are on board - add it.
        if(this.coordinatesOnBoard(columnStern, rowStern)) {
            cooList.add(new CoordinatesImpl(columnStern, rowStern));
        }
        
        // add startbord and portside
        
        // horizontal
        if(horizontal) {
            // starboard
            if(row > Board.MIN_ROW_INDEX) {
                this.addCoordinates(cooList, column, row-1, 
                        ship.getLength(), horizontal);

            }
            // portside
            if(row < Board.MAX_ROW_INDEX) {
                this.addCoordinates(cooList, column, row+1, 
                        ship.getLength(), horizontal);
            }
        // vertical    
        } else {  
            // starboard
            if(column < Board.MAX_COLUMN_INDEX) {
                this.addCoordinates(cooList, column+1, row, 
                        ship.getLength(), horizontal);
            }
            
            // portside
            if(column > Board.MIN_COLUMN_INDEX) {
                this.addCoordinates(cooList, column-1, row, 
                        ship.getLength(), horizontal);
            }
        }
        
        if(this.isShipOnCoordinates(cooList)) {
            throw new BoardException("vessel too close to or ontop of another");
        }
        
        // there are no other ships - put ship
        ship.putShip(column, row, horizontal);
        
        // adjust fields status
        this.moveShipOnFields(ship, true);
    }
    
    /**
     * Add coordinate to list. Add also all following number fields.
     * @param list
     * @param coo
     * @param number
     * @param horizontal 
     */
    private void addCoordinates(List<Coordinates> list, int column, int row, 
            int number, boolean horizontal) {
        
        Coordinates coo = new CoordinatesImpl(column, row);
        list.add(coo);
        // start with 1 - one coordinate is already stored
        for(int i = 1; i < number; i++) {
            try {
                coo = BoardImpl.getNextCoordinate(coo, horizontal);
            } catch (BoardException ex) {
                // can happen in last call - ok
                return;
            }
            list.add(coo);
        }
    }

    /**
     * We follow the decorator pattern: Add functionality to board management
     * @param column
     * @param row
     * @return
     * @throws GameStatusException
     * @throws BoardException 
     */
    @Override
    public ShotResults shot(int column, int row) throws GameStatusException, 
            BoardException {
        
        if(this.game.getStatus() != GameStatus.PLAY_PASSIVE) {
            throw new GameStatusException("cannot be under fire - not passive player now");
        }

        FieldStatus fieldStatus = this.getFieldStatus(column, row);
        
        // lets handle superclass the board first
        ShotResults shotResult = super.shot(column, row);
        
        if( fieldStatus != FieldStatus.SHIP) {
            // there was no ship on that field - we are done here
            return shotResult;
        }
        
        // there was a ship
        Ship ship = this.shipsOnBoard[column][row];
        
        shotResult = ship.shot(column, row);
        
        // lost?
        if(shotResult == ShotResults.HIT_DESTROYED) {
            // final ship?
            if(!this.shipOnBoard()) {
                return ShotResults.HIT_FINISHED;
            }
        }
        
        return shotResult;
    }
    
    /**
     * Are there atill / already at least one (piece of a) ship on the board?
     * @return 
     */
    private boolean shipOnBoard() {
        for(Ship[] shipArray : this.ships) {
            for(Ship ship : shipArray) {
                if(ship.getStatus() != ShipStatus.SUNK) {
                    // there is at least one ship on board
                    return true;
                }
            }
        }
        
        return false;
    }

    @Override
    public boolean allShipsSet() {
        // is there any unset ship?
        for(Ship[] shipArray : this.ships) {
            for(int i = 0; i < shipArray.length; i++) {
                if(!shipArray[i].isSet()) {
                    // yes
                    return false; // not all ships set
                }
            }
        }
        
        // no
        return true; // all ships set
    }
    
    @Override
    public void restoreStatus(BoardMemento memento) throws IOException {
        // fill board
        super.restoreStatus(memento);
        
        // empty redundant ships memory
        this.createUnsetShips();
        
        // restore shipsOnBoard from that field
        boolean[][] checked = 
                new boolean[Board.MAX_COLUMN_INDEX - Board.MIN_COLUMN_INDEX + 1]
                [Board.MAX_ROW_INDEX-Board.MIN_ROW_INDEX + 1];
        
        // clean checked matrix
        for(int column = Board.MIN_COLUMN_INDEX;
                column <= Board.MAX_COLUMN_INDEX; column++) {
            for(int row = Board.MIN_ROW_INDEX;
                row <= Board.MAX_ROW_INDEX; row++) {
                checked[column][row] = false;
            }
        }
        
        // no restore ship
        for(int column = Board.MIN_COLUMN_INDEX;
                column <= Board.MAX_COLUMN_INDEX; column++) {
            for(int row = Board.MIN_ROW_INDEX;
                row <= Board.MAX_ROW_INDEX; row++) {
                
                // ship already detected on that field? go ahead
                if(checked[column][row]) continue;
                
                try {
                    // no ship detected here
                    FieldStatus fieldStatus = this.getFieldStatus(column, row);
                    
                    if(fieldStatus == FieldStatus.HIT
                       || fieldStatus == FieldStatus.SHIP) {
                        
                        // there is a ship (even a hit one)
                        this.restoreShip(column, row, checked);
                    }
                } catch (BoardException ex) {
                    // will not happen
                }
            }
        }
    }

    /**
     * restore a ship from field status
     * @param column first column
     * @param row first row
     * @param checked check field
     */
    private void restoreShip(int columnFirst, int rowFirst, boolean[][] checked) {
        // column and row are the first(!) field of that ship - find
        // following fields
        
        int column = columnFirst;
        int row = rowFirst;
        
        List<Coordinates> coordinates = new ArrayList<>();
        // add first known coordinate
        coordinates.add(new CoordinatesImpl(column, row));
        
        try {
            // horizontal or vertical?
            FieldStatus nextStatus = this.getFieldStatus(column+1, row);

            int rowIncrement = 0;
            int columnIncrement = 0;

            if(nextStatus == FieldStatus.HIT 
                    || nextStatus == FieldStatus.SHIP) {
                columnIncrement = 1;
            } else {
                rowIncrement = 1;
            }

            // now collect coordinates
            row += rowIncrement;
            column += columnIncrement;

            do {
                coordinates.add(new CoordinatesImpl(column, row));
                // remember that this field is already checked
                checked[column][row] = true;
                
                // check next field
                row += rowIncrement;
                column += columnIncrement;

                nextStatus = this.getFieldStatus(column, row);
            } while(nextStatus == FieldStatus.HIT 
                    || nextStatus == FieldStatus.SHIP);
            
            // got coordinates - resfresh ship
            Ship ship = this.getUnsetShip(coordinates.size());
            ship.putShip(column, row, columnIncrement == 1);

            // put ship on second board and re-shot if already hit
            for (Coordinates coordinate : coordinates) {
                column = coordinate.getColumn();
                row = coordinate.getRow();
                
                this.shipsOnBoard[column][row] = ship;
                
                if(this.getFieldStatus(column, row) == FieldStatus.HIT) {
                    ship.shot(column, row);
                }
            }
        }
        catch(BoardException be) {
            // cannot happen 
        }
    }
}
