package seawar;

import java.io.IOException;
import java.io.PrintStream;
import seawar.board.Board;
import seawar.board.BoardException;
import seawar.board.BoardViewAccess;
import seawar.board.LocalBoard;
import seawar.board.LocalBoardImpl;
import seawar.board.RemoteBoard;
import seawar.board.RemoteBoardImpl;
import seawar.board.Ship;
import seawar.board.Shot;
import seawar.board.ShotResults;
import seawar.protocol.ConnectionMaker;
import seawar.protocol.SeaWarProtocolEngine;
import seawar.protocol.SeaWarProtocolEngineState;
import seawar.protocol.SeaWarProtocolException;

/**
 *
 * @author thsc
 */
public class GameImpl implements Game, GameDebug, Shot {
    private RemoteBoardImpl remoteBoard;
    private LocalBoardImpl localBoard;
    
    private ConnectionMaker connectionMaker = null;
    
    private GameStatus status;
    private boolean hasWon = false;
    private SeaWarProtocolEngine protocolEngine;
    
    private PrintStream userOutput;

    GameImpl() {
        this.status = GameStatus.PREPARE;
    }
    
    GameImpl(GameStatus status) {
        this.status = status;
    }
    
    public static Game createGame() {
        return new GameImpl();
    }

    public boolean isConnected() {
        return this.connectionMaker.isConnected();
    }
    
    @Override
    public RemoteBoard getRemoteBoard() {
        if(this.remoteBoard == null) {
            this.remoteBoard = new RemoteBoardImpl(this);
        }
        
        return this.remoteBoard;
    }
    
    @Override
    public BoardViewAccess getRemoteBoardAccessView() {
        // force object creation
        this.getRemoteBoard();
        return this.remoteBoard;
    }

    @Override
    public LocalBoard getLocalBoard() {
        if(this.localBoard == null) {
            this.localBoard = new LocalBoardImpl(this);
        }
        
        return this.localBoard;
    }

    @Override
    public BoardViewAccess getLocalBoardAccessView() {
        // force object creation
        this.getLocalBoard();
        return this.localBoard;
    }
    
    public static void fillLocalBoard(LocalBoard board) throws BoardException, GameStatusException {
        int length = LocalBoard.MAX_SHIP_LENGTH;
        
        int column = 0;
        int row = 0;

        do {
            // there must be 2 ship length 4, 3 of 3 and so forth
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
            length--;
            
        } while(length > 1);
    }

    @Override
    public GameStatus getStatus() {
        return this.status;
    }
    
    

    @Override
    public void startGame() throws GameStatusException {
        if(this.status != GameStatus.PREPARE) {
            throw new GameStatusException("game already started or even finished");
        }
        
        // check if all ships are set
        if(!this.localBoard.allShipsSet()) {
            throw new GameStatusException("all ships must be set to start a game.");
        }
        
        // connection established?
        if(this.connectionMaker == null) {
            throw new GameStatusException("connect before start playing");
        }
        
        if(!this.connectionMaker.isConnected()) {
            throw new GameStatusException("not yet connected to other host - try again later");
        }
        
        try {
            this.protocolEngine = new SeaWarProtocolEngine(
                    this.connectionMaker.getInputStream(),
                    this.connectionMaker.getOutputStream(),
                    this.localBoard,
                    this.userOutput
            );
            
            this.protocolEngine.doNegotiating();
            
            // instantiate remote board
            this.getRemoteBoard();
            
            SeaWarProtocolEngineState state = this.protocolEngine.getState();
            switch(state) {
                case ACTIVE:  this.status = GameStatus.PLAY_ACTIVE; break;
                case PASSIVE:  this.status = GameStatus.PLAY_PASSIVE; 
                                // accept shots
                                new AcceptShotingThread().start();
                                break;
                default: throw new GameStatusException("invalid protocol state");
            }
        }
        catch(IOException ioe) {
            this.protocolEngine = null;
            throw new GameStatusException("cannot get connection streams: " + ioe.getLocalizedMessage());
        } catch (SeaWarProtocolException ex) {
            throw new GameStatusException(ex.getLocalizedMessage());
        }
    }
    
    private void checkProtocolEngine() throws GameStatusException {
        if(this.protocolEngine == null) {
            throw new GameStatusException("protocol engine not yet established - connect to partner player first");
        }
    }

    @Override
    public void giveUp() throws GameStatusException {
        if(this.status == GameStatus.END) {
            throw new GameStatusException("game already ended");
        }
        
        this.hasWon = false;
        this.status = GameStatus.END;
        
        this.protocolEngine.doGiveUp();
    }

    @Override
    public boolean won() throws GameStatusException {
        if(this.status != GameStatus.END) {
            throw new GameStatusException("game not yet ended");
        }
        
        return this.hasWon;
    }

    @Override
    public ShotResults shot(int column, int row) throws GameStatusException, BoardException {
        ShotResults shotResult;
        try {
            shotResult = this.remoteBoard.shot(column, row, this.protocolEngine);
            
            switch(shotResult) {
                case HIT_DESTROYED: // as next line
                case HIT: this.status = GameStatus.PLAY_ACTIVE; break;
                case HIT_FINISHED: 
                    this.status = GameStatus.END; this.hasWon = true; break;
                case HIT_WATER: this.status = GameStatus.PLAY_PASSIVE; break;
            }

            if(shotResult == ShotResults.HIT_WATER) {
                // accept shoting
                this.userOutput.println("you missed and have to wait for enemy fire now :(");
                
                new AcceptShotingThread().start();
            }
        } catch (IOException ex) {
            throw new GameStatusException("IOException: " + ex.getLocalizedMessage());
        }
        
        if(shotResult == ShotResults.HIT_FINISHED) {
            
        }
                
        return shotResult;
    }

    @Override
    public void setStatus(GameStatus status) {
        this.status = status;
    }

    @Override
    public void setConnection(String remoteHost, int remotePort, int localPort, PrintStream userOutput) {
        this.connectionMaker = new ConnectionMaker(remoteHost, remotePort, localPort, userOutput);
        
        this.connectionMaker.start(); // try to find client
        
        this.userOutput = userOutput;
    }
    
    @Override
    public void disconnect() throws IOException {
        this.protocolEngine.disconnect();
        this.protocolEngine = null;
        
        this.connectionMaker.close();
    }

    private class AcceptShotingThread extends Thread {
        
        @Override
        public void run() {
            userOutput.println("waiting for game partner to fire.. ");
            while(status == GameStatus.PLAY_PASSIVE) {
                try {
                    ShotResults result = protocolEngine.readShot(localBoard);
                    int column = protocolEngine.getLastShotColumn();
                    int row = protocolEngine.getLastShotRow();

                    userOutput.println("your game partner fired on column/row: ");
                    userOutput.println(column + " / " + row);
                    userOutput.print("and ");

                    switch(result) {
                        case HIT: 
                            userOutput.println("hit :( "); break;

                        case HIT_DESTROYED:
                            userOutput.println("hit and destroyed ship:(( "); break;

                        case HIT_FINISHED:
                            userOutput.println("hit and you lost :((( "); 
                            status = GameStatus.END;
                            hasWon = false;
                            break;

                        case HIT_WATER:
                            userOutput.println("missed :) your turn (again)"); 
                            status = GameStatus.PLAY_ACTIVE; break;
                    }
                }
                catch(IOException ioEx) {
                    userOutput.println("I/O problems: " + ioEx.getLocalizedMessage());
                    return;
                }
                catch(SeaWarProtocolException | GameStatusException statusEx) {
                    userOutput.println("status exception: " + statusEx.getLocalizedMessage());
                    return;
                }
                catch(BoardException bEx) {
                    userOutput.println("board exception (failure in remote implementation? : " + bEx.getLocalizedMessage());
                    return;
                }
            }
        }
    }
}
