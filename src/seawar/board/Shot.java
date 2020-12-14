package seawar.board;

import seawar.GameStatusException;

/**
 *
 * @author thsc
 */
public interface Shot {
    /**
     * A shot was issued on that board.
     * @param column coloum index of the shot 
     * @param row row index of that shot
     * @return result of that shot (note: a subsequent shot on a field that was
     * already HIT results in a HIT_WATER)
     * @throws GameStatusException if not in play status
     * @throws BoardException if shot in not within the board
     */
    ShotResults shot(int column, int row) throws GameStatusException, BoardException;
}
