package seawar.protocol;

import helper.tcp.TCPChannel;
import java.io.IOException;
import helper.tcp.ConnectedListener;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * @author thsc
 */
public class ConnectionMaker extends Thread implements ConnectedListener {
    private TCPChannel serverChannel;
    private TCPChannel clientChannel;
    private TCPChannel channel = null;
    
    private final PrintStream logOut;
    
    public ConnectionMaker(String remoteHost, int remotePort, int localPort) {
        this(remoteHost, remotePort, localPort, null);
    }
    
    public ConnectionMaker(String remoteHost, int remotePort, int localPort,
            PrintStream logOut) {
        
        // create two attempts of connections
        this.serverChannel = new TCPChannel(remoteHost, remotePort, this);
        this.clientChannel = new TCPChannel(localPort, this);
        
        this.logOut = logOut;
        
        this.serverChannel.setLogOut(logOut);
        this.clientChannel.setLogOut(logOut);
    }
    
    private void log(String logString) {
        if(this.logOut != null) {
            this.logOut.println(logString);
        }
    }
    
    public void close() throws IOException {
        this.channel.close();
    }
    
    @Override
    public void run() {
        // try to establish at least one connection
        
        // start client first - maybe there is already a server waiting
        this.clientChannel.start();
        this.log("started client");
        
        try {
            // give it a sec
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            // ignore
        }
        
        if(!this.isConnected()) {
            // if not already found a client - start server
            this.log("start server");
            this.serverChannel.start();
        }
    }
    
    public boolean isConnected() {
        return this.channel != null;
    }
    
    public InputStream getInputStream() throws IOException {
        if(!this.isConnected()){
            throw new IOException("not yet connected");
        }
        
        return this.channel.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        if(!this.isConnected()){
            throw new IOException("not yet connected");
        }
        
        return this.channel.getOutputStream();
    }

    @Override
    synchronized public void connectionEstablished(TCPChannel channel) {
        if(this.isConnected()) {
            // nothing todo we are already connected
            if(this.channel == this.serverChannel) {
                this.log("connection established called from server channel" +
                      "..we are already connected..close connection");
            } else {
                this.log("connection established called from client channel" +
                      "..we are already connected..close connection");
            }
            
            try {
                channel.close();
            } catch (IOException ex) {
                // TODO?
            }
            return;
        }
        
        this.channel = channel;
        TCPChannel toCloseChannel;
        
        if(this.channel == this.serverChannel) {
            this.log("connection established called from server channel");
            // close client attempt
            toCloseChannel = this.clientChannel;
            this.clientChannel = null; // free resources
            this.log("stop client");
        } else {
            this.log("connection established called from client channel");
            // close server attempt
            toCloseChannel = this.serverChannel;
            this.serverChannel = null; // free resources
            this.log("stop server");
        }
        
        // close unnecessary channel
        try {
            toCloseChannel.close();
        } catch (IOException ex) {
            // ignore
        }
    }
}