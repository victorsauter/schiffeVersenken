package seawar.board.persistency;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import seawar.board.FieldStatus;

/**
 *
 * @author local
 */
public class BoardFileMemento extends BoardStringMemento {
    private final File file;
    
    public BoardFileMemento(File mementoFile) {
        this.file = mementoFile;
    }
    
    @Override
    public void saveBoard(FieldStatus[][] status) throws 
            FileNotFoundException, IOException {
        
        String boardString = this.board2String(status);
        
        FileOutputStream fos = new FileOutputStream(this.file);
        
        DataOutputStream dos = new DataOutputStream(fos);
        
        dos.writeUTF(boardString);
        
        dos.close();
    }

    @Override
    public FieldStatus[][] restoreBoard() 
            throws FileNotFoundException, IOException {
        
        FileInputStream fis = new FileInputStream(this.file);
        DataInputStream dis = new DataInputStream(fis);
        
        String boardString = dis.readUTF();
        
        return this.string2Board(boardString);
    }    
}
