package Logic;

import Events.RefreshEvent;
import InterfaceLink.BoardLink;
import InterfaceLink.RefreshListner;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;


public class BoardLogic implements BoardLink, Runnable {
    private final int[][] gameBoard;
    private static final int ROWS = 25;
    private static final int COLS = 16;
    private static int MAPINDEX = 0;
    private int deltaX = 0;
    private int deltaY = 0;
    private int snakeX = 8;
    private int snakeY = 12;
    private int snakeLenght = 2;
    private int playerScore = 0;
    private boolean gameOngoing;
    public  Direction DIRECTION;
    private final Map<Integer, int[]> segmentsMap;
    private final ArrayList<RefreshListner> listners = new ArrayList<>();


    public BoardLogic() {
        gameBoard = new int[ROWS][COLS];
        gameOngoing = true;
        segmentsMap = new LinkedHashMap<>(2, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, int[]> eldest) {
                return size() == snakeLenght;
            }
        };
        Thread thread = new Thread(this);
        thread.start();
        // Tworzenie i uruchamianie wątku odświerzającego
        Thread refreshThread = new Thread(this::refreshThreadLoop);
        refreshThread.start();
    }

    @Override
    public void initializeGameBoard() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                gameBoard[row][col] = 0;
            }
        }
        gameBoard[12][13] = 3;
        gameBoard[12][14] = 3;
        gameBoard[12][15] = 3;
        gameBoard[12][12] = 3;

    }



    @Override
    public void run() {
        while (gameOngoing) {
            gameBoard[snakeY][snakeX] = 2;
            snakeX += deltaX;
            snakeY += deltaY;
            if(gameBoard[snakeY][snakeX] == 3) {
                snakeLenght++;
                playerScore++;
            }
            gameBoard[snakeY][snakeX] = 1;
            segmentsMap.put(MAPINDEX,new int[]{snakeX,snakeY});
            MAPINDEX++;
            for (int row = 0; row < ROWS; row++) {
                for (int col = 0; col < COLS; col++) {
                    if(!(gameBoard[row][col] == 3)){
                        if(!isRecentSegment(row,col))
                            gameBoard[row][col] = 0;
                    }
                }
            }
            try {
                synchronized (this){
                    wait(300);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void refreshThreadLoop() {
        while(gameOngoing) {
            fireRefresh(new RefreshEvent(this, this));
            try {
                synchronized (this){
                   wait(100);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    @Override
    public void setDirection(Direction direction) {
        this.DIRECTION = direction;
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

    @Override
    public Direction getCurrentDirection() {
        return DIRECTION;
    }

    private void fireRefresh(RefreshEvent refreshEvent) {
        for (RefreshListner listener : listners) {
            listener.refresh(refreshEvent);
        }
    }
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
