package seawar.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.PrimitiveIterator;
import java.util.Random;
import java.util.stream.IntStream;
import seawar.GameStatusException;
import seawar.board.Board;
import seawar.board.BoardException;
import seawar.board.LocalBoardImpl;
import seawar.board.ShotResults;
import static seawar.protocol.SeaWarProtocolEngineState.*;

/**
 * Protocol has three mandantory commands:
 * 1st:
 * ready := "ready" intValue 
 * Negotiation what peer is going to start game
 * Peer with highest number starts. Negotiation is
 * repeated until a different number are exchanged
 * 
 * 2nd.
 * shot := "shot" intValue intValue
 * intValues are in [0,9] and defines a field on the board.
 * first values depicts coloum, second row
 * 
 * 3rd:
 * status := "status" ["WATER" | "HIT" | "SUNK" | "LOST"]
 * issues status after shoting. WATER means no hit
 * HIT - ship hit not sunk. SUNK - ship hit and sunk
 * LOST - final ship was sunk - passive player lost game
 * 
 * @author thsc
 */
public class SeaWarProtocolEngine {
    private SeaWarProtocolEngineState status = READY;
    private final DataInputStream dis;
    private final DataOutputStream dos;
    private final Board localBoard;
    private PrintStream logOut;
    
    public SeaWarProtocolEngine(InputStream is, OutputStream os, 
            Board localBoard, PrintStream userOutput) {
        
        this.dis = new DataInputStream(is);
        this.dos = new DataOutputStream(os);
        this.localBoard = localBoard;
        this.logOut = userOutput;
    }
    
    private void log(String s) {
        if(this.logOut != null) {
            this.logOut.println(s);
        }
    }
    
    private PrimitiveIterator.OfInt intIterator = null;

    public SeaWarProtocolEngine(InputStream inputStream, OutputStream outputStream, LocalBoardImpl localBoard) {
        this(inputStream, outputStream, localBoard, null);
    }
    
    private int getRandomNumber() {
        if(this.intIterator == null) {
            Random random = new Random();
            IntStream ints = random.ints();
            this.intIterator = ints.iterator();
        }
        
        return this.intIterator.nextInt();
    }
    
    public SeaWarProtocolEngineState getState() {
        return this.status;
    }
    
    /**
     * exchange random numbers. 
     * @throws IOException
     * @throws SeaWarProtocolException 
     */
    public void doNegotiating() throws IOException, SeaWarProtocolException {
        if(this.status != READY) {
            throw new SeaWarProtocolException("negotiating must take place in status ready");
        }
        
        // create random number
        int localIntValue = this.getRandomNumber();

        this.log("created random number: " + localIntValue);
        
        // send
        this.log("send..");
        this.sendReady(localIntValue);
        this.log("..sent");

        // receive random number
        this.log("read number..");
        int remoteIntValue = this.readReady();
        this.log("..read: " + remoteIntValue);
        
        if(remoteIntValue == localIntValue) {
            this.log("same do again");
            this.doNegotiating(); // again
        }
        
        if(remoteIntValue > localIntValue) {
            this.log("enter passive status");
            this.status = PASSIVE;
        } else {
            this.log("enter active status");
            this.status = ACTIVE;
        }
    }
    
    /**
     * Shot on remote board
     * @param column
     * @param row
     * @return true if can shot again, false otherwise
     * @throws seawar.protocol.SeaWarProtocolException
     * @throws java.io.IOException
     */
    public ShotResults doShoting(int column, int row) throws SeaWarProtocolException, IOException {
        if(this.status != ACTIVE) {
            throw new SeaWarProtocolException("must not shot if not active player");
        }
        
        // issue shot command
        this.log("send shot to " + column + " / " + row);
        this.sendShot(column, row);
        
        // read status
        ShotResults shotResult = this.readStatus();
        switch(shotResult) {
            case HIT_WATER: this.log("you missed"); this.status = PASSIVE; break;
            case HIT: this.log("you hit"); break;
            case HIT_DESTROYED: this.log("you hit and destroyed a ship"); break;
            case HIT_FINISHED: this.log("won!"); this.status = WON; break;
        }
        
        return shotResult;
    }

    ///////////////////////////////////////////////////////////////////////
    //                        protocol methods                           //
    ///////////////////////////////////////////////////////////////////////
    
    public static final String READY_CMD = "ready";
    public static final String SHOT_CMD = "shot";
    public static final String STATUS_CMD = "status";
    
    private void sendReady(int intValue) throws IOException {
        // check status
        this.dos.writeUTF(READY_CMD);
        this.dos.writeInt(intValue);
    }
    
    private void checkCommand(String expectedCmd) throws IOException, SeaWarProtocolException {
        String cmd = this.dis.readUTF();
        if(cmd != null) {
            cmd = cmd.trim();
            if(!cmd.equalsIgnoreCase(expectedCmd)) {
                throw new SeaWarProtocolException("expected: " + expectedCmd + " got: " + cmd);
            }
        }
    }
    
    private int readReady() throws IOException, SeaWarProtocolException {
        this.checkCommand(READY_CMD);

        return this.dis.readInt();
    }
    
    public static final String STATUS_WATER = "WATER";
    public static final String STATUS_HIT = "HIT";
    public static final String STATUS_SUNK = "SUNK";
    public static final String STATUS_LOST = "LOST";

    /**
     * Read status of game partner from stream
     * @return
     * @throws IOException
     * @throws SeaWarProtocolException 
     */
    private ShotResults readStatus() throws IOException, SeaWarProtocolException {
        this.checkCommand(STATUS_CMD);

        String hitStatusString = this.dis.readUTF();
        switch(hitStatusString) {
            case STATUS_WATER: this.status = PASSIVE; return ShotResults.HIT_WATER;
            case STATUS_HIT: this.status = ACTIVE; return ShotResults.HIT;
            case STATUS_SUNK: this.status = ACTIVE; return ShotResults.HIT_DESTROYED;
            case STATUS_LOST: this.status = WON; return ShotResults.HIT_FINISHED;
        }
        
        throw new SeaWarProtocolException("received unvalid status: " + hitStatusString);
    }
    
    private void sendStatus(ShotResults status) throws IOException, SeaWarProtocolException {
        this.dos.writeUTF(STATUS_CMD);
        
        // gave up - whatever really happend - we notify that we are gone.
        if(this.gaveUp) {
            this.dos.writeUTF(STATUS_LOST);
        }
        
        switch(status) {
            case HIT_WATER: this.dos.writeUTF(STATUS_WATER); break;
            case HIT: this.dos.writeUTF(STATUS_HIT); break;
            case HIT_DESTROYED: this.dos.writeUTF(STATUS_SUNK); break;
            case HIT_FINISHED: this.dos.writeUTF(STATUS_LOST); break;
        }
    }
    
    private void sendShot(int column, int row) throws IOException {
        this.dos.writeUTF(SHOT_CMD);
        this.dos.writeInt(column);
        this.dos.writeInt(row);
    }
    
    private int lastShotColumn = 0;
    private int lastShotRow = 0;
    
    public int getLastShotColumn() {
        return this.lastShotColumn;
    }

    public int getLastShotRow() {
        return this.lastShotRow;
    }

    public ShotResults readShot(LocalBoardImpl localBoard) throws 
            IOException, SeaWarProtocolException, GameStatusException, BoardException {
        
        this.checkCommand(SHOT_CMD);
        lastShotColumn = this.dis.readInt();
        lastShotRow = this.dis.readInt();
        
        ShotResults result = localBoard.shot(lastShotColumn, lastShotRow);
        
        // adjust protocol engine status
        switch(result) {
            case HIT_DESTROYED: // enemy fire hit
            case HIT: this.status = PASSIVE; break;
            case HIT_FINISHED: this.status = LOST; break; // We have lost
            case HIT_WATER: this.status = ACTIVE; break; // enemy hit water!
        }
        
        // notify game partner
        this.sendStatus(result);
        
        return result;
    }
    
    private boolean gaveUp = false;

    public void doGiveUp() {
        this.gaveUp = true;
    }

    public void disconnect() throws IOException {
        this.dis.close();
        this.dos.close();
    }
}
