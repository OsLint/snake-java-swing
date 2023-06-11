package InterfaceLink;

import Events.GameStateEvent;
import Enums.Direction;
import Logic.PlayerScore;

import java.util.ArrayList;

public interface BoardLink {
    void setPlayerScores(ArrayList<PlayerScore> playerScores);
    void setPlayerName (String playerName);
    void fireGameState(GameStateEvent gameStateEvent);
    void newGame();
    void gameOver();
    void pauseGame();
    void resumeGame();
    void initializeGameBoard();
    int [] [] getGameBoard();
    int getRows();
    int getCols();
    int getPLayerScore();
    int getCellValue(int row, int col);
    boolean recentSegment(int row, int col);
    boolean getIsGameOngoing();
    boolean getIspauseGame();
    Direction getCurrentDirection();
    ArrayList<PlayerScore> getPlayerScores();
    String getPLayerName();
    String getPlayerName();



}
