package helper.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author thsc
 */
public class TCPChannel extends Thread {
    private final int port;
    private final boolean asServer;
    private final String hostname;
    private final ConnectedListener listener;
    
    private Socket socket = null;
    private boolean closed = false;
    
    private boolean fatalError = false;
    private TCPChannel channelThread = null;
    
    public int waitingTimeForNextConnectionTryInMillis = DEFAULT_WAITINGTIME;
    public static final int DEFAULT_WAITINGTIME = 10000;
    private PrintStream logOut;
    
    /**
     * Create a TCP channel
     * @param port port to connected to or to offer to clients
     * @param asServer run as server
     */
    private TCPChannel(String hostname, int port, 
            ConnectedListener listener, boolean asServer) {
        this.hostname = hostname;
        this.port = port;
        this.listener = listener;
        this.asServer = asServer;
    }
    
    /**
     * Create a TCP channel as client
     * @param port
     * @param listener
     */
    public TCPChannel(int port, ConnectedListener listener) {
        this("localhost", port, listener, false);
    }
    
    public TCPChannel(int port, ConnectedListener listener, 
            int waitingTimeForNextConnectionTryInMillis) {
        
        this("localhost", port, listener, false);
        this.waitingTimeForNextConnectionTryInMillis = 
                waitingTimeForNextConnectionTryInMillis;
    }
    
    /**
     * Create a TCP channel as server
     * @param hostname
     * @param port
     * @param listener
     */
    public TCPChannel(String hostname, int port, 
            ConnectedListener listener) {
        this(hostname, port, listener, true);
    }
    
    public void setLogOut(PrintStream logOut) {
        this.logOut = logOut;
    }
    
    private void log(String logString) {
        if(this.logOut != null) {
            this.logOut.println(logString);
        }
    }
    
    @Override
    public void run() {
        this.channelThread = this;
        
        try {
            // create sockets in either way
            if(this.asServer) {
                this.log("start tcp channel thread as server");
                this.socket = new TCPChannel.TCPServer().getSocket();
                this.log("got socket as server");
                this.listener.connectionEstablished(this);
            } else {
                this.log("start tcp channel thread as client");
                this.socket = new TCPChannel.TCPClient().getSocket();
                this.log("got socket as client");
                this.listener.connectionEstablished(this);
            }
        } catch (IOException ex) {
            System.err.println("couldn't esatblish connection");
            this.fatalError = true;
        }
    }
    
    public void close() throws IOException {
        if(this.asServer) {
            this.log("TCPChannel: close called as server");
        } else {
            this.log("TCPChannel: close called as client");
        }
        
        this.closed = true;
        
        if(this.socket != null) {
            this.socket.close();
        }
        
        // kill thread
        if(this.channelThread != null) {
            this.channelThread.interrupt();
        }
    }
    
    private boolean threadRunning() {
        return this.channelThread != null;
    }
    
    /**
     * holds thread until a connection is established
     * @throws java.io.IOException
     */
    public void waitForConnection() throws IOException {
        if(!this.threadRunning()) {
            /* in unit tests there is a race condition between the test
            thread and those newly created tests to establish a connection.
            
            Thus, this call could be in the right order - give it a
            second chance
            */
            
            try {
                Thread.sleep(waitingTimeForNextConnectionTryInMillis);
            } catch (InterruptedException ex) {
                // ignore
            }

            if(!this.threadRunning()) {
                // that's probably wrong usage:
                throw new IOException("must start TCPChannel thread first by calling start()");
            }
        }
        
        
        while(!this.fatalError && this.socket == null) {
            try {
                Thread.sleep(waitingTimeForNextConnectionTryInMillis);
            } catch (InterruptedException ex) {
                // ignore
            }
        }
    }
    
    public void checkConnected() throws IOException {
        if(this.socket == null) {
            throw new IOException("no socket yet - should call connect first");
        }
    }
    
    public InputStream getInputStream() throws IOException {
        this.checkConnected();
        return this.socket.getInputStream();
    }
    
    public OutputStream getOutputStream() throws IOException {
        this.checkConnected();
        return this.socket.getOutputStream();
    }
    
    private class TCPServer {
        Socket getSocket() throws IOException {
            ServerSocket srvSocket = new ServerSocket(port);
            log("server socket created - step into accept");
            Socket socket = srvSocket.accept();
            log("returned from accept");
            return socket;
        }
    }

    private class TCPClient {
        Socket getSocket() throws IOException {
            while(!closed) {
                try {
                    log("try to create client socket");
                    Socket socket = new Socket(hostname, port);
                    log("client socket created");
                    return socket;
                }
                catch(IOException ioe) {
                    try {
                        log("no client socket created - sleep and try again later");
                        Thread.sleep(waitingTimeForNextConnectionTryInMillis);
                    } catch (InterruptedException ex) {
                        // ignore
                    }
                }
            }
            return null;
        }
    }
}
