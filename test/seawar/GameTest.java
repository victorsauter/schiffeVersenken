package seawar;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import seawar.board.BoardException;
import seawar.board.Shot;
import seawar.board.ShotResults;
import seaware.view.console.BoardCommands;
import seaware.view.console.BoardCommandsImpl;

/**
 *
 * @author thsc
 */
public class GameTest {
    public static final String BOB_HOST = "localhost";
    public static final String ALICE_HOST = "localhost";
    public static final int ALICE_PORT = 7070;
    public static final int BOB_PORT = 7071;
            
    
    public GameTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void scenario1() throws BoardException, GameStatusException, InterruptedException, IOException {
        Game alice = new GameImpl();

        Game bob = new GameImpl();

        // fill both boards
        GameImpl.fillLocalBoard(alice.getLocalBoard());
        GameImpl.fillLocalBoard(bob.getLocalBoard());
        
        // set connection parameter for both partners
        alice.setConnection(BOB_HOST, BOB_PORT, ALICE_PORT, System.out);
        Thread.sleep(1000);
        bob.setConnection(ALICE_HOST, ALICE_PORT, BOB_PORT, System.err);
        Thread.sleep(1000);

        while(!alice.isConnected() || !bob.isConnected()) {
            // wait a second
            Thread.sleep(1000);
        }
        
        // connected
        
        // start alice game in new thread
        new StartGameThread(alice).start();
        
        // start bob game in this thread
        bob.startGame();
        //new StartGameThread(bob).start();
        
        // wait for negotiation to end
        Thread.sleep(2000);
        
        // who is active
        GameStatus aliceStatus = alice.getStatus();
        GameStatus bobStatus = bob.getStatus();
        
        // one is active other passive
        Assert.assertTrue(aliceStatus != bobStatus);
        
        Assert.assertTrue(aliceStatus != GameStatus.PREPARE &&
                aliceStatus != GameStatus.END &&
                bobStatus != GameStatus.PREPARE &&
                bobStatus != GameStatus.END);
        
        Game activePlayer = aliceStatus == GameStatus.PLAY_ACTIVE ? alice : bob;
        activePlayer.shot(0, 0); // hit
        
        activePlayer.shot(0, 1); // miss
        
        // wait a second
        Thread.sleep(1000);
        
        // again with changed roles
        // who is active
        aliceStatus = alice.getStatus();
        bobStatus = bob.getStatus();
        
        // one is active other passive
        Assert.assertTrue(aliceStatus != bobStatus);
        
        Assert.assertTrue(aliceStatus != GameStatus.PREPARE &&
                aliceStatus != GameStatus.END &&
                bobStatus != GameStatus.PREPARE &&
                bobStatus != GameStatus.END);
        
        activePlayer = aliceStatus == GameStatus.PLAY_ACTIVE ? alice : bob;
        activePlayer.shot(0, 0); // hit
        Thread.sleep(1000);
        activePlayer.shot(0, 1); // miss
        
        // wait a second
        Thread.sleep(1000);
        
        // again with changed roles
        aliceStatus = alice.getStatus();
        activePlayer = aliceStatus == GameStatus.PLAY_ACTIVE ? alice : bob;

        // now finish it
        this.killRest((Shot) activePlayer);
        
        Thread.sleep(1000);
        Assert.assertEquals(GameStatus.END, activePlayer.getStatus());
        
        // print status
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeUTF(BoardCommands.GET_STATUS);
        byte[] getStatusBytes = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(getStatusBytes);

        // alice
        BoardCommands userConsole = new BoardCommandsImpl(alice, System.out, bais);
        userConsole.runGame();
        
        // bob
        bais = new ByteArrayInputStream(getStatusBytes);
        userConsole = new BoardCommandsImpl(bob, System.err, bais);
        userConsole.runGame();
        
        // tidy up
        try {
            alice.disconnect();
        }
        catch(IOException e) {
            // ignore
        }
        
        bob.disconnect();
        
        Thread.sleep(1000);
    
        // debug break
        int i = 42;
    }
     
    private void killRest(Shot activePlayer) throws GameStatusException, BoardException {
        // 5er
        activePlayer.shot(1, 0); // hit
        activePlayer.shot(2, 0); // hit
        activePlayer.shot(3, 0); // hit
        activePlayer.shot(4, 0); // hit
        
        // 4er A
        activePlayer.shot(0, 2); // hit
        activePlayer.shot(1, 2); // hit
        activePlayer.shot(2, 2); // hit
        activePlayer.shot(3, 2); // hit
        
        // 4er B
        activePlayer.shot(0, 4); // hit
        activePlayer.shot(1, 4); // hit
        activePlayer.shot(2, 4); // hit
        activePlayer.shot(3, 4); // hit
        
        // 3er A
        activePlayer.shot(0, 6); // hit
        activePlayer.shot(1, 6); // hit
        activePlayer.shot(2, 6); // hit

        // 3er B
        activePlayer.shot(0, 8); // hit
        activePlayer.shot(1, 8); // hit
        activePlayer.shot(2, 8); // hit
        
        // 3er C
        activePlayer.shot(6, 0); // hit
        activePlayer.shot(7, 0); // hit
        activePlayer.shot(8, 0); // hit
        
        // 2er A
        activePlayer.shot(6, 2); // hit
        activePlayer.shot(7, 2); // hit
        
        // 2er B
        activePlayer.shot(6, 4); // hit
        activePlayer.shot(7, 4); // hit
        
        // 2er C
        activePlayer.shot(6, 6); // hit
        activePlayer.shot(7, 6); // hit
        
        // 2er D
        activePlayer.shot(6, 8); // hit
        
        // final shot
        Assert.assertEquals(ShotResults.HIT_FINISHED, activePlayer.shot(7, 8)); // hit
    }
     
    @Test
    public void destroyAll() throws SeaWarException {
        GameImpl game = new GameImpl();
        GameImpl.fillLocalBoard(game.getLocalBoard());

        // set game passive
        game.setStatus(GameStatus.PLAY_PASSIVE);
        
        // get local board
        Shot shotLocal = (Shot) game.getLocalBoard();
        
        // first shot
        shotLocal.shot(0, 0);
        
        // rest
        this.killRest(shotLocal);
    }
    
    //@Test
    public void scenario2_giveup() throws BoardException, GameStatusException, InterruptedException, IOException {
        Game alice = new GameImpl();

        Game bob = new GameImpl();

        // fill both boards
        GameImpl.fillLocalBoard(alice.getLocalBoard());
        GameImpl.fillLocalBoard(bob.getLocalBoard());
        
        // set connection parameter for both partners
        alice.setConnection(BOB_HOST, BOB_PORT, ALICE_PORT, System.out);
        Thread.sleep(1000);
        bob.setConnection(ALICE_HOST, ALICE_PORT, BOB_PORT, System.err);
        Thread.sleep(1000);

        while(!alice.isConnected() || !bob.isConnected()) {
            // wait a second
            Thread.sleep(1000);
        }
        
        // connected
        
        // start alice game in new thread
        new StartGameThread(alice).start();
        
        // start bob game in this thread
        bob.startGame();
        
        // wait for negotiation to end
        Thread.sleep(2000);
        
        // who is active
        GameStatus aliceStatus = alice.getStatus();
        GameStatus bobStatus = bob.getStatus();
        
        // one is active other passive
        Assert.assertTrue(aliceStatus != bobStatus);
        
        Assert.assertTrue(aliceStatus != GameStatus.PREPARE &&
                aliceStatus != GameStatus.END &&
                bobStatus != GameStatus.PREPARE &&
                bobStatus != GameStatus.END);
        
        Game activePlayer = aliceStatus == GameStatus.PLAY_ACTIVE ? alice : bob;
        Game passivePlayer = aliceStatus == GameStatus.PLAY_PASSIVE ? alice : bob;
        
        passivePlayer.giveUp();
        
        ShotResults shotResult = activePlayer.shot(1, 1); // miss
        
        Assert.assertEquals(ShotResults.HIT_FINISHED, shotResult);
        
        // tidy up
        try {
            alice.disconnect();
        }
        catch(IOException e) {
            // ignore
        }
        
        bob.disconnect();
        
        Thread.sleep(1000);
    }
     
    private class StartGameThread extends Thread {

       private final Game game;
       StartGameThread(Game game) {
           this.game = game;
       }
       
       @Override
       public void run() {
           try {
               this.game.startGame();
           } catch (GameStatusException ex) {
               System.err.println(ex.getLocalizedMessage());
           }
       }
    }
}
