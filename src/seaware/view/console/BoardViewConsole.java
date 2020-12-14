package seaware.view.console;

import java.io.PrintStream;
import seawar.board.BoardViewAccess;

/**
 *
 * @author thsc
 */
public interface BoardViewConsole {
    static BoardViewConsole getBoardViewConsole() 
        {return new BoardViewConsoleImpl();}
    
    void printBoard(BoardViewAccess board, 
            PrintStream screen);
}
