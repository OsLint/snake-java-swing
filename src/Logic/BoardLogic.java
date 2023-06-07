package Logic;

import Events.ChangeDirectionEvent;
import Events.RefreshEvent;
import InterfaceLink.BoardLink;
import InterfaceLink.ChangeDirection;
import InterfaceLink.RefreshListner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.lang.Thread.sleep;


public class BoardLogic implements BoardLink, Runnable, ChangeDirection {
    private final int[][] gameBoard;
    private static final int ROWS = 25;
    private static final int COLS = 16;
    private int deltaX = 0;
    private int deltaY = 0;
    private int snakeX = 10;
    private int snakeY = 10;
    private static int mapIndex = 0;
    Map<Integer, int[]> segmentsMap = new LinkedHashMap<Integer, int[]>(2, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Integer, int[]> eldest) {
            return size() >= snakeLenght;
        }
    };
    private int snakeLenght = 2;
    private int playerScore = 0;
    private boolean gameOngoing;
    public  Direction DIRECTION;
    private final ArrayList<RefreshListner> listners = new ArrayList<>();


    public BoardLogic() {
        gameBoard = new int[ROWS][COLS];
        gameOngoing = true;
        Thread thread = new Thread(this);
        thread.start();
        // Tworzenie i uruchamianie wątku odświerzającego
        Thread refreshThread = new Thread(this::refreshThreadLoop);
        refreshThread.start();



    }

    @Override
    public void initializeGameBoard() {
        // Initialize the game board with 0s
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                //gameBoard[row][col] = (int)Math.round(Math.random() * 10);
                gameBoard[row][col] = 0;
            }
        }

        gameBoard[7][8]= 2;
        gameBoard[2][9]= 2;
        gameBoard[5][10]= 2;
        gameBoard[1][11]= 2;
        gameBoard[11][8]= 2;
        gameBoard[22][9]= 2;
        gameBoard[15][10]= 2;
        gameBoard[10][11]= 2;
        gameBoard[17][8]= 2;
        gameBoard[12][9]= 2;
        gameBoard[16][10]= 2;
        gameBoard[11][11]= 2;
    }

    @Override
    public Map<Integer, int[]> getSegmentsMap() {
        return segmentsMap;
    }

    @Override
    public void run() {
        while (gameOngoing) {
            snakeX += deltaX;
            snakeY += deltaY;
            if(gameBoard[snakeY][snakeX] == 2) {
                snakeLenght++;
                playerScore++;
            }
            gameBoard[snakeY][snakeX] = 1;
            segmentsMap.put(mapIndex,new int[]{snakeX,snakeY});
            mapIndex++;
            for (int row = 0; row < ROWS; row++) {
                for (int col = 0; col < COLS; col++) {
                    if(!(gameBoard[row][col] == 2)){
                        if(!isRecentSegment(row,col))
                            gameBoard[row][col] = 0;
                    }
                }
            }
            //gameBoard[snakeY-deltaY][snakeX-deltaX] = 0;


            System.out.println(snakeLenght);

            //System.out.println("Snake is on position: " + snakeX + " " + snakeY);
            try {
               sleep(300);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void refreshThreadLoop() {
        while(gameOngoing) {
            fireRefresh(new RefreshEvent(this, this));
            try {
                sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void changeDirection(ChangeDirectionEvent e) {
        this.DIRECTION = e.getChangeDirection();
        switch (DIRECTION) {
            case UP -> {
                deltaY = -1;
                deltaX = 0;
            }
            case DOWN -> {
                deltaY = 1;
                deltaX = 0;
            }
            case LEFT -> {
                deltaX = -1;
                deltaY = 0;
            }
            case RIGHT -> {
                deltaX = 1;
                deltaY = 0;
            }
        }
        System.out.println("Direction is: " + DIRECTION.toString());
    }
    @Override
    public boolean isRecentSegment(int row, int col) {
        for (int[] segment : segmentsMap.values()) {
            int segmentRow = segment[1];
            int segmentCol = segment[0];
            if (segmentRow == row && segmentCol == col) {
                return true;
            }
        }
        return false;
    }

    /**
     * Metoda wywołująca zdarzenie odświeżenia.
     *
     * @param refreshEvent zdarzenie odświeżenia
     */
    private void fireRefresh(RefreshEvent refreshEvent) {
        for (RefreshListner listener : listners) {
            listener.refresh(refreshEvent);
        }
    }

    /**
     * Metoda dodająca słuchacza odświeżenia.
     *
     * @param listner słuchacz odświeżenia
     */
    public void addRefreshListner(RefreshListner listner) {
        this.listners.add(listner);
    }


    @Override
    public int getPLayerScore() {
        return playerScore;
    }

    @Override
    public int[][] getGameBoard() {
        return gameBoard;
    }

    @Override
    public int getRows() {
        return ROWS;
    }

    @Override
    public int getCols() {
        return COLS;
    }


}
