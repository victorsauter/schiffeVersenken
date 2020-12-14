package seawar.board;

import java.io.IOException;
import seawar.board.persistency.BoardMemento;

/**
 *
 * @author local
 */
public interface BoardPersistency {
    void saveStatus(BoardMemento memento) throws IOException;
    
    void restoreStatus(BoardMemento memento) throws IOException;
}
