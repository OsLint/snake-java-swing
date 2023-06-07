package InterfaceLink;

import java.util.Map;

public interface BoardLink {
    int [] [] getGameBoard();
    int getRows();
    int getCols();
    void initializeGameBoard();
    int getPLayerScore();
    Map<Integer, int[]> getSegmentsMap();
}
