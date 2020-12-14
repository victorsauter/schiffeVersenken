package seawar.board;

/**
 *
 * @author thsc
 */
class ShipImpl implements Ship {
    private final int length;
    private Coordinates[] coordinates = null;
    private boolean[] hits = null;

    ShipImpl(int length) {
        this.length = length;
        this.hits = new boolean[length];
        for(int i = 0; i < length; i++) {
            this.hits[i] = false;
        }
    }

    @Override
    public ShipStatus getStatus() {
        boolean isOK = true; // assume the best
        boolean isPartIntact = false; // assume worst
        
        // check the facts
        for(boolean hit : hits) {
            if(hit) {
                isOK = false; // some part was hit :(
            } else {
                isPartIntact = true; // at least one part working :)
            }
        }
        
        // no hit at all :)
        if(isOK) return ShipStatus.OK;
        
        // we reach this point if that vessel was hit at least once
        if(isPartIntact) {
            // hit but at least one part works
            return ShipStatus.HIT;
        }
        
        // no intact part found.
        return ShipStatus.SUNK;
    }

    @Override
    public boolean isSet() {
        return this.coordinates != null;
    }

    @Override
    public void putShip(int column, int row, boolean horizontal) 
            throws BoardException {
        // assumed that coordindates are correct
        
        // create coordinates list
        this.coordinates = new CoordinatesImpl[length];
        
        this.coordinates[0] = new CoordinatesImpl(column, row);
        Coordinates currentCoo = this.coordinates[0];

        for(int i = 1; i < this.length; i++) {
            try {
                this.coordinates[i] = 
                        BoardImpl.getNextCoordinate(currentCoo, horizontal);
                
                currentCoo = this.coordinates[i];
            }
            catch(BoardException e) {
                // remove ship from board
                this.remove();
                throw e;
            }
        }
    }

    @Override
    public void remove() {
        this.coordinates = null;
    }

    @Override
    public Coordinates[] getCoordinates() throws BoardException {
        if(this.coordinates == null) 
            throw new BoardException("vessel not on board");
        
        return this.coordinates;
    }

    @Override
    public ShotResults shot(int column, int row)  throws BoardException {
        // find index of coordinate
        Coordinates[] coordinates = this.getCoordinates();
        
        for(int index = 0; index < coordinates.length; index++) {
            Coordinates coo = coordinates[index];
            if(coo.getColumn() == column && coo.getRow() == row) {
                this.hits[index] = true;
                
                // sunk?
                for(int i = 0; i < this.hits.length; i++) {
                    // there is at least one intact vessel part
                    if(!this.hits[i]) return ShotResults.HIT;
                }
                
                // no intact part found
                return ShotResults.HIT_DESTROYED;
            }
        }
        
        throw new BoardException("serious error: shot on ship but coordinates"
                + "not found");
    }

    @Override
    public int getLength() {
        return this.length;
    }
}
