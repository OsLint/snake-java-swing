package InterfaceLink;

public interface BoardLink {
    int [] [] getGameBoard();
    int getRows();
    int getCols();
    void initializeGameBoard();
    int getPLayerScore();
}
