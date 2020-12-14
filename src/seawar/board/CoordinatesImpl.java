package seawar.board;

/**
 *
 * @author thsc
 */
class CoordinatesImpl implements Coordinates {
    private final int column;
    private final int row;

    CoordinatesImpl(int column, int row) {
        this.column = column;
        this.row = row;
    }
    
    @Override
    public int getColumn() {
        return this.column;
    }

    @Override
    public int getRow() {
        return this.row;
    }
}
