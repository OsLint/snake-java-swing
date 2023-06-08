package InterfaceLink;

import Logic.Direction;

public interface BoardLink {
    int [] [] getGameBoard();
    int getRows();
    int getCols();
    void initializeGameBoard();
    int getPLayerScore();
    boolean isRecentSegment(int row, int col);
    Direction getCurrentDirection();
    void setDirection(Direction direction);
}
