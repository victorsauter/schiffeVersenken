package seaware.view.console;

import java.io.PrintStream;
import seawar.board.Board;
import seawar.board.BoardViewAccess;
import seawar.board.FieldStatus;

/**
 * This class implements a visualization of sea war games.
 * Visualization is a number of simple ASCII characters written
 * to a PrintStream.
 * 
 * @author thsc
 */
public class BoardViewConsoleImpl implements BoardViewConsole {
    public static final char WATER_SYMBOL = '~';
    public static final char SHIP_SYMBOL = 'S';
    public static final char SHOT_ON_WATER_SYMBOL = 'w';
    public static final char HIT_SYMBOL = 'X';
    public static final char UNKNOWN_SYMBOL = '-';

    @Override
    public void printBoard(BoardViewAccess board, PrintStream screen) {
        FieldStatus[][] fieldStatus = board.getFieldStatus();
        
        // assumed [column,row] - iterate rows and columns inside

        screen.print("\n");
        screen.print("  ");
        screen.print("A B C D E F G H I J");
        screen.print("\n");
        
        for(int row = Board.MIN_ROW_INDEX; row <= Board.MAX_ROW_INDEX; row++) {
            screen.print(row + " ");
            for(int column = Board.MIN_COLUMN_INDEX; 
                    column <= Board.MAX_COLUMN_INDEX; column++) {
                
                
                switch(fieldStatus[column][row]) {
                    case SHIP: screen.print(SHIP_SYMBOL); break;
                    case WATER: screen.print(WATER_SYMBOL); break;
                    case HIT: screen.print(HIT_SYMBOL); break;
                    case SHOT_ON_WATER: screen.print(SHOT_ON_WATER_SYMBOL); break;
                    case UNKNOWN: screen.print(UNKNOWN_SYMBOL); break;
                }
                screen.print(" ");
            }
            screen.print("\n");
        }        
    }
}
