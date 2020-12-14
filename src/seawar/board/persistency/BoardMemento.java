package seawar.board.persistency;

import java.io.IOException;
import seawar.board.FieldStatus;

/**
 *
 * @author thsc
 */
public interface BoardMemento {
    void saveBoard(FieldStatus[][] status) throws IOException;
    FieldStatus[][] restoreBoard() throws IOException;
}
