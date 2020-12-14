package seawar;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author thsc
 */
@RunWith(Suite.class)
@Suite.SuiteClasses(
        {
            seawar.board.BoardTests.class, 
            seawar.board.LocalBoardTests.class,
            seawar.board.ViewTests.class,
            seawar.board.InputTests.class,
            seawar.board.PersistencyTests.class,
        }
)
public class AllTests {
}
