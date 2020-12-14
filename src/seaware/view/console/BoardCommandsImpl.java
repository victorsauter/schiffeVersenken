package seaware.view.console;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.StringTokenizer;
import seawar.Game;
import seawar.GameImpl;
import seawar.GameStatus;
import seawar.GameStatusException;
import seawar.board.Board;
import seawar.board.BoardException;
import seawar.board.BoardViewAccess;
import seawar.board.LocalBoard;
import seawar.board.RemoteBoard;
import seawar.board.Ship;
import seawar.board.ShotResults;
import seawar.board.persistency.BoardFileMemento;
import seawar.board.persistency.BoardMemento;

/**
  * commands
  * .. print local board
  * .. print remote board
  * .. shot x y
  * .. set ship
  * .. remove ship
  * .. start game
  * .. give up
  * .. talk
  * .. get status
  * 
  * @author thsc
  */
public class BoardCommandsImpl implements BoardCommands {
    public static final String LOCAL_MEMENTO_FILENAME = "mementoLocal";
    public static final String REMOTE_MEMENTO_FILENAME = "mementoRemote";
    
    private final Game game;
    
    private final PrintStream consoleOutput;
    private final BufferedReader userInput;
    
    private final BoardViewConsole view;

    private final RemoteBoard remoteBoard;
    private final BoardViewAccess remoteBoardAccess;

    private final LocalBoard localBoard;
    private final BoardViewAccess localBoardAccess;
            
    public BoardCommandsImpl(Game game, PrintStream os, InputStream is) {
        this.game = game;
        
        this.consoleOutput = os;
        this.userInput = new BufferedReader(new InputStreamReader(is));
        
        this.view = BoardViewConsole.getBoardViewConsole();
        
        this.remoteBoard = game.getRemoteBoard();
        this.remoteBoardAccess = game.getRemoteBoardAccessView();
        
        this.localBoard = game.getLocalBoard();
        this.localBoardAccess = game.getLocalBoardAccessView();
    }
    
    public static void main(String[] args) {
        Game game = GameImpl.createGame();
        PrintStream os = System.out;
        
        os.println("Welcome to Sea War Version 0.1");
        BoardCommandsImpl userCmd = new BoardCommandsImpl(
                game, os, System.in);
        
        userCmd.printUsage();
        userCmd.runGame();
    }

    @Override
    public void printUsage() {
        StringBuilder b = new StringBuilder();
        
        b.append("\n");
        b.append("\n");
        b.append("valid commands:");
        b.append("\n");
        b.append(PRINT_LOCAL);
        b.append(" .. print local board");
        b.append("\n");
        b.append(PRINT_REMOTE);
        b.append(" .. print remote board");
        b.append("\n");
        b.append(SHOT);
        b.append(" .. shot x y");
        b.append("\n");
        b.append(SET_SHIP);
        b.append(".. set ship");
        b.append("\n");
        b.append(REMOVE_SHIP);
        b.append(" .. remove ship");
        b.append("\n");
        b.append(START_GAME);
        b.append(".. start game");
        b.append("\n");
        b.append(GIVE_UP);
        b.append(" .. give up");
        b.append("\n");
        b.append(TALK);
        b.append(" .. talk");
        b.append("\n");
        b.append(GET_STATUS);
        b.append(".. get status");
        b.append("\n");
        b.append(SAVE);
        b.append(".. save current status");
        b.append("\n");
        b.append(RESTORE);
        b.append(".. restore status from file");
        b.append("\n");
        b.append(CONNECT);
        b.append(".. connect to partner");
        b.append("\n");
        b.append(FILL);
        b.append(".. fill local board with ships");
        b.append("\n");
        b.append(EXIT);
        b.append(".. exit");
        
        this.consoleOutput.println(b.toString());
    }
    
    @Override
    public void printUsage(String cmdString, String comment) {
        PrintStream out = this.consoleOutput;
        
        if(comment == null) comment = " ";
        out.println("malformed command: " + comment);
        out.println("use:");
        switch(cmdString) {
            case SET_SHIP: 
                out.println(SET_SHIP + " ship_length column (A..J) row (0..9) [h|v]");
                out.println("example: " + SET_SHIP + " 5 B 2 h");
                out.println("puts a ship with length 5 at field B2 horizontal");
                break;
                
            case REMOVE_SHIP:
                out.println(REMOVE_SHIP + " column (A..J) row (0..9) ");
                out.println("example: " + REMOVE_SHIP + " B 2 ");
                out.println("removes ship on field B2");
                break;
                
            case SHOT:
                out.println(SHOT + " column (A..J) row (0..9) ");
                out.println("example: " + SHOT + " B 2 ");
                out.println("fires on field B2");
                break;

            case CONNECT:
                out.println(CONNECT + " IP/DNS-Name_remoteHost remotePort localPort");
                out.println("example: " + CONNECT + "  localhost 7070 7071 ");
                out.println("opens a server socket #7071 and tries to connect to localhost:7070");
                break;
        }
        
        out.println("unknown command: " + cmdString);
    }    

    @Override
    public void runGame() {
        
        boolean again = true;
        while(again) {
            try {
                // read user input
                String cmdLineString = userInput.readLine();
                
                // finish that loop if less than nothing came in
                if(cmdLineString == null) break;
                
                // trim whitespaces on both sides
                cmdLineString = cmdLineString.trim();
                
                // extract command
                int spaceIndex = cmdLineString.indexOf(' ');
                spaceIndex = spaceIndex != -1 ? spaceIndex : cmdLineString.length();

                // got command string
                String commandString = cmdLineString.substring(0, spaceIndex);
                
                // extract parameters string - can be empty
                String parameterString = cmdLineString.substring(spaceIndex);
                parameterString = parameterString.trim();

                // start command loop
                switch(commandString) {
                    case PRINT_LOCAL: 
                        this.doPrintLocalBoard(); break;
                    case PRINT_REMOTE:
                        this.doPrintRemoteBoard(); break;
                    case SHOT:
                        this.doShot(parameterString); break;
                    case SET_SHIP:
                        this.doSetShip(parameterString); break;
                    case REMOVE_SHIP:
                        this.doRemoveShip(parameterString); break;
                    case START_GAME:
                        this.doStartGame(); break;
                    case GIVE_UP:
                        this.doGiveUp(); break;
                    case TALK:
                        this.doTalk(parameterString); break;
                    case GET_STATUS:
                        this.doGetStatus(); break;
                    case SAVE:
                        this.doSave(); break;
                    case RESTORE:
                        this.doRestore(); break;
                    case CONNECT:
                        this.doConnect(parameterString); break;
                    case FILL:
                        this.doFill();
                        this.doPrintLocalBoard(); break;
                    case "q": // convenience
                    case EXIT:
                        again = false; break; // end loop 

                    default: this.consoleOutput.println("unknown command:" +
                            cmdLineString);
                            this.printUsage();
                            break;
                }
            }
            catch(BoardException be) {
                this.consoleOutput.println("failure on board operation: " 
                        + be.getMessage());
            }
            catch(GameStatusException ge) {
                this.consoleOutput.println("operation not allowed in that status: " 
                        + ge.getMessage());
            } catch (IOException ex) {
                this.consoleOutput.println("cannot read from input stream");
                System.exit(0);
            }
        }
    }
    
    /////////////////////////////////////////////////////////////////////////
    //                        command loop methods                         //
    /////////////////////////////////////////////////////////////////////////
    
    private void doShot(String parameterString) throws BoardException {
                StringTokenizer st = new StringTokenizer(parameterString);
        
        try {
            String columnString = st.nextToken();
            String rowString = st.nextToken();
            
            int columnInt = this.parseColumnInt(columnString);
            int rowInt = this.parseRowInt(rowString);
            
            ShotResults shot = this.game.shot(columnInt, rowInt);

            switch(shot) {
                case HIT: 
                    this.consoleOutput.println(HIT_STRING); break;
                    
                case HIT_DESTROYED: 
                    this.consoleOutput.println(HIT_STRING); break;
                    
                case HIT_FINISHED: 
                    this.consoleOutput.println(HIT_STRING); break;
                    
                case HIT_WATER:
                    this.consoleOutput.println(HIT_WATER_STRING); break;
            }
            
            // show result
            this.doPrintRemoteBoard();
        }
        catch(RuntimeException re) {
            // nullpointer or not enough parameters
            this.printUsage(SHOT, re.getMessage());
        } catch (BoardException ex) {
            this.consoleOutput.println("couldn't set ship: " + ex.getMessage());
        } catch (GameStatusException ex) {
            this.consoleOutput.println("not allowed setting ship: " + ex.getMessage());
        }
    }
    /**
     * expected parameters: length column row orientation
     * @param cmdLineString 
     */
    private void doSetShip(String parameterString) {
        StringTokenizer st = new StringTokenizer(parameterString);
        
        try {
            String lengthString = st.nextToken();
            String columnString = st.nextToken();
            String rowString = st.nextToken();
            String horizontalString = st.nextToken();
            
            int shipLength = Integer.parseInt(lengthString);
            
            int columnInt = this.parseColumnInt(columnString);
            
            int rowInt = this.parseRowInt(rowString);
            
            boolean horizontal = this.parseHorizontal(horizontalString);
            
            Ship ship = this.localBoard.getUnsetShip(shipLength);
            
            this.localBoard.putShip(ship, columnInt, rowInt, horizontal);
            
            // show result
            this.doPrintLocalBoard();
            
            // show number available ship
            this.printAvailableShips(this.consoleOutput, this.localBoard);
        }
        catch(RuntimeException re) {
            // nullpointer or not enough parameters or similar stuff
            this.printUsage(SET_SHIP, re.getMessage());
        } catch (BoardException ex) {
            this.consoleOutput.println("couldn't set ship: " + ex.getMessage());
        } catch (GameStatusException ex) {
            this.consoleOutput.println("not allowed setting ship: " + ex.getMessage());
        }
    }

    private void doRemoveShip(String parameterString) {
        StringTokenizer st = new StringTokenizer(parameterString);
        
        try {
            String columnString = st.nextToken();
            String rowString = st.nextToken();
            
            int columnInt = this.parseColumnInt(columnString);
            int rowInt = this.parseRowInt(rowString);
            
            this.localBoard.removeShip(
                    this.localBoard.getShip(columnInt, rowInt));
            
            // show result
            this.doPrintLocalBoard();
            
            // show number available ship
            this.printAvailableShips(this.consoleOutput, this.localBoard);
        }
        catch(RuntimeException re) {
            // nullpointer or not enough parameters
            this.printUsage(REMOVE_SHIP, re.getMessage());
        } catch (BoardException ex) {
            this.consoleOutput.println("couldn't remove ship: " + ex.getMessage());
        } catch (GameStatusException ex) {
            this.consoleOutput.println("was not allowed to remove ship: " + ex.getMessage());
        }
    }
    
    private void doConnect(String parameterString) {
        StringTokenizer st = new StringTokenizer(parameterString);
        
        try {
            String remoteHost = st.nextToken();
            String remotePortString = st.nextToken();
            String localPortString = st.nextToken();
            
            int remotePort = Integer.parseInt(remotePortString);
            int localPort = Integer.parseInt(localPortString);
            
            this.game.setConnection(remoteHost, remotePort, localPort, this.consoleOutput);
        }
        catch(RuntimeException re) {
            // nullpointer or not enough parameters
            this.printUsage(CONNECT, re.getMessage());
        }
    }

    private void doTalk(String parameterString) {
        this.consoleOutput.println("will be implemented later .. allows talking to your opponent");
    }
    
    private void doPrintLocalBoard() {
        this.consoleOutput.println("\nLocal Board:");
        view.printBoard(localBoardAccess, this.consoleOutput);
    }
    
    private void doPrintRemoteBoard() {
        this.consoleOutput.println("\nRemote Board:");
        view.printBoard(remoteBoardAccess, this.consoleOutput);
    }
    
    private void doStartGame() throws GameStatusException {
        this.game.startGame(); 
        this.consoleOutput.println("started");
    }
    
    private void doGiveUp() throws GameStatusException {
        game.giveUp(); 
        this.consoleOutput.print("you gave up. ");
        if(this.game.won()) {
            this.consoleOutput.println(WON_STRING);
        } else {
            this.consoleOutput.println(LOST_STRING);
        }
    }
    
    private void doGetStatus() throws GameStatusException {
        GameStatus status = game.getStatus();
        switch(status) {
            case PREPARE:
                this.consoleOutput.println("PREPARE: set your ships");
                break;
            case PLAY_ACTIVE:
                this.consoleOutput.println("PLAY_ACTIVE: you can shot");
                break;
            case PLAY_PASSIVE:
                this.consoleOutput.println("PLAY_PASSIV: wait for be shoting");
                break;
            case END:
                this.consoleOutput.print("game finished - ");
                if(game.won()) {
                    this.consoleOutput.println(WON_STRING);
                } else {
                    this.consoleOutput.println(LOST_STRING);
                }
                break;
            default: 
                this.consoleOutput.println("serious error: unknown status");
        }
    }
    
    /////////////////////////////////////////////////////////////////////
    //                       private helper methods                    //
    /////////////////////////////////////////////////////////////////////
    
    private void printAvailableShips(PrintStream os, LocalBoard b) throws BoardException {
        int[] free = new int[Board.MAX_SHIP_LENGTH - Board.MIN_SHIP_LENGTH + 1];
        // count all free ships
        int totalFree = 0;
        
        for(int l = Board.MIN_SHIP_LENGTH; l <= Board.MAX_SHIP_LENGTH; l++) {
            int freeShips = b.getNumberShipsNotSet(l);
            free[l-Board.MIN_SHIP_LENGTH] = freeShips;
            totalFree += freeShips;
        }
        
        if(totalFree == 0) {
            os.println("no ships to set - we could start");
        } else {
            os.println(totalFree + " ships to set:");
            for(int l = Board.MIN_SHIP_LENGTH; l <= Board.MAX_SHIP_LENGTH; l++) {
                if(free[l-Board.MIN_SHIP_LENGTH] != 0) {
                    os.print("length "  + l + ": ");
                    os.println(b.getNumberShipsNotSet(l) + " ship(s) to set");
                }
            }
        }
    }
    
    private int parseColumnInt(String columnString) throws 
            NumberFormatException {
        
            if(columnString.length() != 1) {
                throw new NumberFormatException("column must be single "
                        + "alphanumeric letter");
            }
            
            columnString = columnString.toUpperCase();
            char columnChar = columnString.charAt(0);
            
            if(columnChar >= Board.MIN_COLUMN_CHAR 
                    && columnChar <= Board.MAX_COLUMN_CHAR) {
                return columnChar - 'A';
            }
            
            throw new NumberFormatException("column must be single "
                        + "within " + Board.MIN_COLUMN_CHAR + 
                    " and " + Board.MAX_COLUMN_CHAR);
    }

    private int parseRowInt(String rowString) throws 
            NumberFormatException {
        
            if(rowString.length() != 1) {
                throw new NumberFormatException("row must be single "
                        + "numeric letter");
            }
            
            return Integer.parseInt(rowString);
    }
    
    private boolean parseHorizontal(String horizontalString) throws 
            NumberFormatException {
        
        
            char hChar = horizontalString.toUpperCase().charAt(0);
            if(hChar == 'H') return true;
            
            if(hChar == 'V') return false;
            
            throw new NumberFormatException("must be h(orizontal) or v(ertical)");
    }

    private void doSave() throws IOException {
        // local board
        File mementoFile = new File(BoardCommandsImpl.LOCAL_MEMENTO_FILENAME);
        BoardMemento memento = new BoardFileMemento(mementoFile);
        this.localBoard.saveStatus(memento);

        // remote board
        mementoFile = new File(BoardCommandsImpl.REMOTE_MEMENTO_FILENAME);
        memento = new BoardFileMemento(mementoFile);
        this.remoteBoard.saveStatus(memento);

        this.consoleOutput.println(BoardCommands.SAVED_STRING);
    }

    private void doRestore() throws IOException {
        File mementoFile = new File(BoardCommandsImpl.LOCAL_MEMENTO_FILENAME);
        BoardMemento memento = new BoardFileMemento(mementoFile);
        this.localBoard.restoreStatus(memento);

        mementoFile = new File(BoardCommandsImpl.REMOTE_MEMENTO_FILENAME);
        memento = new BoardFileMemento(mementoFile);
        this.remoteBoard.restoreStatus(memento);

        this.consoleOutput.println(BoardCommands.RESTORED_STRING);
    }

    private void doFill() throws BoardException, GameStatusException {
        GameImpl.fillLocalBoard(this.localBoard);
    }
}
