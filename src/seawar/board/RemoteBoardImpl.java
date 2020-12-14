package seawar.board;

import java.io.IOException;
import seawar.Game;
import seawar.GameStatus;
import seawar.GameStatusException;
import seawar.SeaWarException;
import seawar.protocol.SeaWarProtocolEngine;

/**
 *
 * @author thsc
 */
public class RemoteBoardImpl extends BoardImpl implements RemoteBoard {
    public RemoteBoardImpl(Game game) {
        super(game, FieldStatus.UNKNOWN);
    }
    
    @Override
    /**
     * @deprecated use implementation with protocol engine
     */
    public ShotResults shot(int column, int row) throws GameStatusException, 
            BoardException {
        
        if(this.game.getStatus() != GameStatus.PLAY_ACTIVE) {
            throw new GameStatusException("cannot fire - not active player now");
        }
        
        return super.shot(column, row);
    }

    public ShotResults shot(int column, int row, SeaWarProtocolEngine protocolEngine) throws GameStatusException, BoardException, IOException {
        try {
            ShotResults shotResult = protocolEngine.doShoting(column, row);
            switch(shotResult) {
                case HIT_DESTROYED: 
                case HIT:
                case HIT_FINISHED: 
                    this.setFieldStatus(column, row, FieldStatus.HIT); break;
                    
                case HIT_WATER: 
                    this.setFieldStatus(column, row, FieldStatus.SHOT_ON_WATER); break;
            }
            
            return shotResult;
        } catch (SeaWarException ex) {
            throw new GameStatusException(ex.getLocalizedMessage());
        }
    }
}
