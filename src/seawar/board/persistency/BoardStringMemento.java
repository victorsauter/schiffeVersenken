package seawar.board.persistency;

import seawar.board.Board;
import seawar.board.BoardViewAccess;
import seawar.board.FieldStatus;
import seaware.view.console.BoardViewConsoleImpl;
import static seaware.view.console.BoardViewConsoleImpl.HIT_SYMBOL;
import static seaware.view.console.BoardViewConsoleImpl.SHIP_SYMBOL;
import static seaware.view.console.BoardViewConsoleImpl.SHOT_ON_WATER_SYMBOL;
import static seaware.view.console.BoardViewConsoleImpl.UNKNOWN_SYMBOL;
import static seaware.view.console.BoardViewConsoleImpl.WATER_SYMBOL;

/**
 *
 * @author local
 */
abstract class BoardStringMemento implements BoardMemento {

    private FieldStatus[][] fieldStatus;

    public String board2String(FieldStatus[][] status) {
        StringBuilder sb = new StringBuilder();
        
        // column / row
        this.fieldStatus = status;
        
        for(int column = Board.MIN_COLUMN_INDEX;
                column <= Board.MAX_COLUMN_INDEX; column++) {
            
            for(int row = Board.MIN_ROW_INDEX;
                    row <= Board.MAX_ROW_INDEX; row++) {
                
                switch(fieldStatus[column][row]) {
                    case SHIP: sb.append(SHIP_SYMBOL); break;
                    case WATER: sb.append(WATER_SYMBOL); break;
                    case HIT: sb.append(HIT_SYMBOL); break;
                    case SHOT_ON_WATER: sb.append(SHOT_ON_WATER_SYMBOL); break;
                    case UNKNOWN: sb.append(UNKNOWN_SYMBOL); break;
                }
            }
        }
        
        return sb.toString();
    }

    public FieldStatus[][] string2Board(String boardString) {
        int columnNumber = Board.MAX_COLUMN_INDEX - Board.MIN_COLUMN_INDEX + 1;
        int rowNumber = Board.MAX_ROW_INDEX - Board.MIN_ROW_INDEX + 1;
        
        // column / row
        this.fieldStatus = new FieldStatus[columnNumber][rowNumber];
        
        // parse String
        int sIndex = 0;
        
        for(int column = Board.MIN_COLUMN_INDEX;
                column <= Board.MAX_COLUMN_INDEX; column++) {
            
            for(int row = Board.MIN_ROW_INDEX;
                    row <= Board.MAX_ROW_INDEX; row++) {
        
                char symbol = boardString.charAt(sIndex++);
                
                switch(symbol) {
                    case BoardViewConsoleImpl.SHIP_SYMBOL: 
                        fieldStatus[column][row] = FieldStatus.SHIP; break;

                    case BoardViewConsoleImpl.WATER_SYMBOL:
                        fieldStatus[column][row] = FieldStatus.WATER; break;

                    case BoardViewConsoleImpl.HIT_SYMBOL:
                        fieldStatus[column][row] = FieldStatus.HIT; break;

                    case BoardViewConsoleImpl.SHOT_ON_WATER_SYMBOL:
                        fieldStatus[column][row] = FieldStatus.SHOT_ON_WATER; break;

                    case BoardViewConsoleImpl.UNKNOWN_SYMBOL:
                        fieldStatus[column][row] = FieldStatus.UNKNOWN; break;
                }
            }
        }
        
        return this.fieldStatus;
    }
}
