package InterfaceLink;

import Events.GameStateEvent;
import Logic.Direction;
import Logic.PlayerScore;

import java.util.ArrayList;

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

    boolean getIspauseGame();
    void setPlayerName (String playerName);
    void fireGameState(GameStateEvent gameStateEvent);
    void newGame();
    void gameOver();
    void pauseGame();
    void resumeGame();
    ArrayList<PlayerScore> getPlayerScores();

    String getPLayerName();


}
