package seawar.board;

import java.io.File;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import seawar.GameDebug;
import seawar.GameStatus;
import seawar.GameStatusException;
import seawar.SeaWarException;
import seawar.board.persistency.BoardFileMemento;
import seawar.board.persistency.BoardMemento;

/**
 *
 * @author local
 */
public class PersistencyTests extends Root {
    public static final String LOCALBOARD_MEMENTO_FILENAME = "localboard";
    
    public PersistencyTests() {
    }
    
    @Test
    public void writeAndRead() throws BoardException, GameStatusException, IOException {
        // get a local board
        LocalBoardImpl localBoard = this.getLocalBoard();

        // get a ship length 5
        Ship ship = localBoard.getUnsetShip(5);

        // put in upper left corner - vertical
        localBoard.putShip(ship, 0, 0, false);

        // there should be a ship
        Assert.assertNotNull(localBoard.getShip(0, 0));
        Assert.assertNotNull(localBoard.getShip(0, 1));
        Assert.assertNotNull(localBoard.getShip(0, 2));
        Assert.assertNotNull(localBoard.getShip(0, 3));
        Assert.assertNotNull(localBoard.getShip(0, 4));

        // create file object
        File mementoFile = new File(LOCALBOARD_MEMENTO_FILENAME);

        BoardMemento memento = new BoardFileMemento(mementoFile);

        localBoard.saveStatus(memento);

        // remove ship
        localBoard.removeShip(ship);

        // test if gone
        try {
            localBoard.getShip(0, 0);
            Assert.fail("no ship should be present on those coordiinates");
        }
        catch(SeaWarException swe) {
            // should reach this point - there is no ship
        }

        // re-read file - create new memento object with same file
        memento = new BoardFileMemento(mementoFile);

        // restore status
        localBoard.restoreStatus(memento);

        // ship should be present again
        Assert.assertNotNull(localBoard.getShip(0, 2));
    }

    @Test(expected = BoardException.class)
    public void rememberSet() throws BoardException, GameStatusException, IOException {
        // get a local board
        LocalBoardImpl localBoard = this.getLocalBoard();

        // get a ship length 5
        Ship ship = localBoard.getUnsetShip(5);

        // put in upper left corner - vertical
        localBoard.putShip(ship, 0, 0, false);

        // create file object
        File mementoFile = new File(LOCALBOARD_MEMENTO_FILENAME);
        BoardMemento memento = new BoardFileMemento(mementoFile);
        localBoard.saveStatus(memento);

        // re-read file - create new memento object with same file
        memento = new BoardFileMemento(mementoFile);

        // restore status
        localBoard.restoreStatus(memento);

        // there should not be a ship length 5 - already set
        localBoard.getUnsetShip(5);
    }
  
    @Test
    public void rememberHit() throws BoardException, GameStatusException, IOException {
        // get a local board
        LocalBoardImpl localBoard = this.getLocalBoard();

        // get a ship length 5
        Ship ship = localBoard.getUnsetShip(5);

        // put in upper left corner - vertical
        localBoard.putShip(ship, 0, 0, false);

        GameDebug gameDebug = (GameDebug) localBoard.getGame();
        gameDebug.setStatus(GameStatus.PLAY_PASSIVE);
        localBoard.shot(0, 0);
        
        FieldStatus fieldStatus = localBoard.getFieldStatus(0, 0);
        Assert.assertEquals(FieldStatus.HIT, fieldStatus);
        
        // create file object
        File mementoFile = new File(LOCALBOARD_MEMENTO_FILENAME);
        BoardMemento memento = new BoardFileMemento(mementoFile);
        localBoard.saveStatus(memento);

        // shot again but after saving status
        localBoard.shot(0, 1);
        fieldStatus = localBoard.getFieldStatus(0, 1);
        Assert.assertEquals(FieldStatus.HIT, fieldStatus);

        // re-read file - create new memento object with same file
        memento = new BoardFileMemento(mementoFile);

        // restore status
        localBoard.restoreStatus(memento);

        // ship was hit at 0,0
        fieldStatus = localBoard.getFieldStatus(0, 0);
        Assert.assertEquals(FieldStatus.HIT, fieldStatus);

        // don't remember hit at 0,1
        fieldStatus = localBoard.getFieldStatus(0, 1);
        Assert.assertEquals(FieldStatus.SHIP, fieldStatus);
    }
}
