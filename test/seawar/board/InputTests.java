package seawar.board;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.junit.Test;
import seawar.Game;
import seawar.GameImpl;
import seaware.view.console.BoardCommands;
import seaware.view.console.BoardCommandsImpl;

/**
 * @author thsc
 */
public class InputTests extends Root {
    
    public InputTests() {
    }
    
    private void runTest(String commands) throws IOException {
        byte[] buffer = new byte[1000];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        DataOutputStream dos = new DataOutputStream(baos);
        
        dos.writeUTF(commands);
        
        // commands in byte array
        byte[] byteCommands = baos.toByteArray();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(byteCommands);
        
        Game game = this.getGame();
        
        BoardCommands commandInput = new BoardCommandsImpl(
                game, System.out, bais);
        
        commandInput.runGame();
        
    }
    
    
    @Test
    public void basicVisualTests() throws IOException {
        Game game = this.getGame();
        BoardCommands cmd = new BoardCommandsImpl(game, System.out, System.in);
        
        cmd.printUsage();
        
    }
    
    @Test
    public void basicVisualTests2() throws IOException {
        // expected: printboard
        StringBuilder b = new StringBuilder();
        b.append(BoardCommands.PRINT_LOCAL);
        b.append("\n");
        
        b.append(BoardCommands.PRINT_REMOTE);
        b.append("\n");
        
        String inputString = b.toString();
        
        this.runTest(inputString);
    }
    
    @Test
    public void inputTestsDebug() throws IOException {
        // expected: printboard
        StringBuilder b = new StringBuilder();
        b.append(BoardCommands.REMOVE_SHIP);
        b.append(" B 4"); // failure
        b.append("\n");
        
        String inputString = b.toString();
        
        this.runTest(inputString);
    }
    
    @Test
    public void inputTestsDebug2() throws IOException {
        // expected: printboard
        StringBuilder b = new StringBuilder();
        b.append(BoardCommands.GET_STATUS);
        b.append("\n");
        
        String inputString = b.toString();
        
        this.runTest(inputString);
    }
}
