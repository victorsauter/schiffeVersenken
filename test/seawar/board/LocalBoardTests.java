package seawar.board;

import org.junit.Assert;
import org.junit.Test;
import seawar.GameDebug;
import seawar.GameImpl;
import seawar.GameStatus;
import seawar.GameStatusException;
import seawar.SeaWarException;

/**
 *
 * @author thsc
 */
public class LocalBoardTests {
    
    final LocalBoard getLocalBoard() {
        return new LocalBoardImpl(GameImpl.createGame());
    }
    
    /**
     * Das sollten wir einmal im SU diskutieren. Wenn ich es vergessen,
     * dann sprechen Sie mich bitte darauf an!
     * 
     * @param board
     * @return 
     */
    final Shot getShot(LocalBoard board) {
        return (Shot) board;
    }

    
    /**
     * Test if all ten ships can be set but not more and board
     * notifies all ships set.
     * 
     * @throws BoardException 
     */
    @Test
    public void createLocalBoardCheck4Ships() throws BoardException {
        LocalBoard board = this.getLocalBoard();
         
        int length = LocalBoard.MAX_SHIP_LENGTH;
        
        int column = 0;
        int row = 0;

        do {
            // there must be 2 ship length 4, 3 of 3 and so forth
            try {
                // that loops goes one time for length MAX and MAX times for l=1
                for(int counter = 0; 
                        counter <= LocalBoard.MAX_SHIP_LENGTH-length; counter++) 
                {
                    Ship ship = board.getUnsetShip(length);
                    board.putShip(ship, column, row, true);
                    
                    // next ship goes two row lower
                    row += 2;
                    if(row > Board.MAX_ROW_INDEX) {
                        // reached last row
                        row = 0;
                        
                        // go right
                        column = 6;
                    }
                    
                }
            }
            catch(SeaWarException e) {
                // this must not fail
                e.printStackTrace(System.out);
                Assert.fail("ships could not be set, length == " + length);
            }         

            // this call must fail
            try {
                board.getUnsetShip(length);
                // should reach that point
                Assert.fail("this ship must not exists, length: " + length);
            }
            catch(BoardException e) {
                // ok
            }
            length--;
            
        } while(length > 1);
        
        // all ships are set
        Assert.assertTrue(board.allShipsSet());
     }
    
    @Test
    public void testShipCoordinates() throws BoardException, GameStatusException {
        LocalBoard board = this.getLocalBoard();
        
        Ship ship = board.getUnsetShip(5);
        
        board.putShip(ship, 0, 0, true);
        
        Shot shotBoard = this.getShot(board);
        
        // change status through debugging backdoor
        GameDebug.getGameDebug(board.getGame()).setStatus(GameStatus.PLAY_PASSIVE);
        
        ShotResults shot = shotBoard.shot(0, 0);
        Assert.assertEquals(ShotResults.HIT, shot);
        
        shot = shotBoard.shot(1, 0);
        Assert.assertEquals(ShotResults.HIT, shot);
        
        shot = shotBoard.shot(2, 0);
        Assert.assertEquals(ShotResults.HIT, shot);
        
        shot = shotBoard.shot(3, 0);
        Assert.assertEquals(ShotResults.HIT, shot);
        
        shot = shotBoard.shot(4, 0);
        Assert.assertEquals(ShotResults.HIT_DESTROYED, shot);
        
    }

    @Test(expected = BoardException.class)
    public void ships2close1() throws BoardException, GameStatusException {
        LocalBoard board = this.getLocalBoard();
    
        Ship shipL5 = board.getUnsetShip(5);
        Ship shipL4 = board.getUnsetShip(4);
        
        try {
            board.putShip(shipL5, 0, 0, true);
        }
        catch(BoardException e) {
            // should work
        }
        
        // too close - should throw BoardException
        board.putShip(shipL4, 0, 1, true);
    }
    
    @Test(expected = BoardException.class)
    public void ships2close2() throws BoardException, GameStatusException {
        LocalBoard board = this.getLocalBoard();
    
        Ship shipL5 = board.getUnsetShip(5);
        Ship shipL2 = board.getUnsetShip(2);
        
        try {
            // coordinates 0,0 1,0 2,0 3,0 4,0 
            board.putShip(shipL5, 0, 0, true);
        }
        catch(BoardException e) {
            // should work
        }
        
        // too close - should throw BoardException
        board.putShip(shipL2, 5, 0, true);
    }

    @Test(expected = BoardException.class)
    public void ships2close3() throws BoardException, GameStatusException {
        LocalBoard board = this.getLocalBoard();
    
        Ship shipL5 = board.getUnsetShip(5);
        Ship shipL2 = board.getUnsetShip(2);
        
        try {
            // coordinates 0,0 1,0 2,0 3,0 4,0 
            board.putShip(shipL5, 0, 0, true);
        }
        catch(BoardException e) {
            // should work
        }
        
        // too close - should throw BoardException
        board.putShip(shipL2, 1, 1, true);
    }
    
    @Test(expected = BoardException.class)
    public void ships2close4() throws BoardException, GameStatusException {
        LocalBoard board = this.getLocalBoard();
    
        Ship shipL5 = board.getUnsetShip(5);
        Ship shipL3 = board.getUnsetShip(3);
        
        try {
            // coordinates 0,0 1,0 2,0 3,0 4,0 
            board.putShip(shipL5, 0, 0, true);
        }
        catch(BoardException e) {
            // should work
        }
        
        // too close - should throw BoardException
        board.putShip(shipL3, 2, 1, false);
    }

    @Test(expected = BoardException.class)
    public void ships2close5() throws BoardException, GameStatusException {
        LocalBoard board = this.getLocalBoard();
    
        Ship shipL5 = board.getUnsetShip(5);
        Ship shipL3 = board.getUnsetShip(3);
        
        try {
            // coordinates 3,1 3,2 3,3 3,4 3,5 
            board.putShip(shipL5, 3, 1, false);
        }
        catch(BoardException e) {
            // should work
        }
        
        // too close - should throw BoardException
        board.putShip(shipL3, 2, 6, true);
    }

    @Test(expected = BoardException.class)
    public void ships2close6() throws BoardException, GameStatusException {
        LocalBoard board = this.getLocalBoard();
    
        Ship shipL5 = board.getUnsetShip(5);
        Ship shipL3 = board.getUnsetShip(3);
        
        try {
            // coordinates 3,4 3,5 3,6 3,7 3,8 
            board.putShip(shipL5, 3, 4, false);
        }
        catch(BoardException e) {
            // should work
        }
        
        // too close - should throw BoardException
        board.putShip(shipL3, 2, 3, true); // 2,3 3,3 (!) 4,3
    }

    @Test(expected = BoardException.class)
    public void ships2close7() throws BoardException, GameStatusException {
        LocalBoard board = this.getLocalBoard();
    
        Ship shipL5 = board.getUnsetShip(5);
        Ship shipL3 = board.getUnsetShip(3);
        
        try {
            // coordinates 3,8 4,8 5,8 6,8 7,8 
            board.putShip(shipL5, 3, 8, true);
        }
        catch(BoardException e) {
            // should work
        }
        
        // too close - should throw BoardException
        board.putShip(shipL3, 5, 5, false);
    }

    @Test(expected = BoardException.class)
    public void ships2close8() throws BoardException, GameStatusException {
        LocalBoard board = this.getLocalBoard();
    
        Ship shipL2 = board.getUnsetShip(2);
        Ship shipL3 = board.getUnsetShip(3);
        
        try {
            // coordinates 3,1 3,2 
            board.putShip(shipL2, 3, 1, false);
        }
        catch(BoardException e) {
            // should work
        }
        
        // too close - should throw BoardException
        board.putShip(shipL3, 3, 3, false);
    }

    @Test
    public void shipsFit() throws BoardException, GameStatusException {
        LocalBoard board = this.getLocalBoard();
    
        Ship shipL5 = board.getUnsetShip(5);
        Ship shipL3 = board.getUnsetShip(3);
        
        try {
            // coordinates 3,4 3,5 3,6 3,7 3,8 
            board.putShip(shipL5, 3, 4, false);
        }
        catch(BoardException e) {
            // should work
        }
        
        // fits
        board.putShip(shipL3, 2, 1, true);
    }

    @Test
    public void shipsFit2() throws BoardException, GameStatusException {
        LocalBoard board = this.getLocalBoard();
    
        Ship shipL21 = board.getUnsetShip(2);
        
        try {
            // coordinates 5,5 6,5 
            board.putShip(shipL21, 5, 5, true);
        }
        catch(BoardException e) {
            // should work
        }
        
        // fits
        Ship shipL22 = board.getUnsetShip(2);
        board.putShip(shipL22, 7, 4, true);
        
        Ship shipL23 = board.getUnsetShip(2);
        board.putShip(shipL22, 3, 4, true);
        
        Ship shipL24 = board.getUnsetShip(2);
        board.putShip(shipL22, 3, 6, true);
        
        // no more ship length 2
        Ship shipL31 = board.getUnsetShip(3);
        board.putShip(shipL22, 7, 6, true);
    }

    @Test
    public void shipsFit3() throws BoardException, GameStatusException {
        LocalBoard board = this.getLocalBoard();
    
        Ship shipL21 = board.getUnsetShip(2);
        
        try {
            // coordinates 5,5 5,6 
            board.putShip(shipL21, 5, 5, false);
        }
        catch(BoardException e) {
            // should work
        }
        
        // fits
        Ship shipL22 = board.getUnsetShip(2);
        board.putShip(shipL22, 6, 3, false); // 6,3 6,4
        
        Ship shipL23 = board.getUnsetShip(2);
        board.putShip(shipL22, 4, 3, false); // 4,3 4,4 
        
        Ship shipL24 = board.getUnsetShip(2);
        board.putShip(shipL22, 4, 7, false); // 4,7 4,8 
        
        // no more ship length 2
        Ship shipL31 = board.getUnsetShip(3);
        board.putShip(shipL22, 6, 7, false); // 6,7 6,8 6,9
    }

    @Test(expected = BoardException.class)
    public void shipOnTop() throws BoardException, GameStatusException {
        LocalBoard board = this.getLocalBoard();
    
        Ship shipL5 = board.getUnsetShip(5);
        Ship shipL1 = board.getUnsetShip(1);
        
        try {
            // coordinates 0,0 1,0 2,0 3,0 4,0 
            board.putShip(shipL5, 0, 0, true);
        }
        catch(BoardException e) {
            // should work ship
        }
        
        // on top of other - should throw BoardException
        board.putShip(shipL1, 0, 1, true);
    }
}
