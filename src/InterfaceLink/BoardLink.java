package InterfaceLink;

import Logic.Direction;

public interface BoardLink {
    int [] [] getGameBoard();
    int getRows();
    int getCols();
    String getPlayerName();
    void initializeGameBoard();
    int getPLayerScore();
    boolean isRecentSegment(int row, int col);
    boolean getIsGameOngoing();
    Direction getCurrentDirection();
    int getCellValue(int row, int col);
    void newGame();
    boolean getIspauseGame();



}
