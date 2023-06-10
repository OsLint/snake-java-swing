package Logic;

import Events.*;
import InterfaceLink.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;


public class BoardLogic implements BoardLink, Runnable, ChangeDirectionListner {
    private final int[][] gameBoard;
    private static final int ROWS = 25;
    private static final int COLS = 16;
    private static int MAPINDEX = 0;
    private String playerName;
    private final Random random;
    private int deltaX = 0;
    private int deltaY = 0;
    private int snakeX = 8;
    private int snakeY = 12;
    private int snakeLenght = 1;
    private int playerScore = 0;
    private boolean gameOngoing;
    private boolean isGamePaused;
    private Direction DIRECTION;
    private final Map<Integer, int[]> segmentsMap;

    //ArrayListy Event Listnerów
    private final ArrayList<RefreshListner> refreshListners = new ArrayList<>();
    private final ArrayList<FoodEventListner> foodEventListners = new ArrayList<>();
    private final ArrayList<GameStateListner> gameStateListners = new ArrayList<>();
    private final ArrayList<PlayerScore> playerScores = new ArrayList<>();
    private final ArrayList<GenereteFoodListner> genereteFoodListners = new ArrayList<>();

    public BoardLogic() {
        //Inicjalizacja pól prywatnych
        gameBoard = new int[ROWS][COLS];
        gameOngoing = true;
        random = new Random();
        segmentsMap = new LinkedHashMap<>(2, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, int[]> eldest) {
                return size() == snakeLenght + 1;
            }
        };

        //Event Listnery
        GameStateListner gameStateListner = gameStateEvent -> {
            GameState gameState = gameStateEvent.getGameState();
            switch (gameState) {
                case PAUSED -> pauseGame();
                case UNPAUSED -> resumeGame();
                case GAMEOVER -> gameOver();
                case NEWGAME -> newGame();
            }
        };
        FoodEventListner foodEventListner = new FoodEventListner() {
            @Override
            public void consoooomeFood(FoodEatenEvent foodEatenEvent) {
                switch (foodEatenEvent.getFoodType()) {
                    case APPLE -> {
                        playerScore += 10;
                        snakeLenght++;
                    }
                    case SCISSORS -> {
                        if (snakeLenght > 2) {
                            segmentsMap.clear();
                            snakeLenght = 1;
                        }
                    }
                    case GOLDENAPPLE -> {
                        playerScore += 100;
                        snakeLenght++;
                    }
                    case BLACKAPPLE -> fireGameState(new GameStateEvent(this, GameState.GAMEOVER));
                }
            }
        };
        GenereteFoodListner genereteFoodListner = foodGeneratedEvent -> {
            FoodType foodType = foodGeneratedEvent.getFoodType();
            int foodRow = random.nextInt(ROWS);
            int foodCol = random.nextInt(COLS);
            if (gameBoard[foodRow][foodCol] == 0) {

                switch (foodType) {
                    case BLACKAPPLE -> gameBoard[foodRow][foodCol] = 6;
                    case GOLDENAPPLE -> gameBoard[foodRow][foodCol] = 4;
                    case APPLE -> gameBoard[foodRow][foodCol] = 3;
                    case SCISSORS -> gameBoard[foodRow][foodCol] = 5;
                }
            }
        };

        this.addGenerateFoodListner(genereteFoodListner);
        this.addFoodEventListner(foodEventListner);
        this.addGameStateListner(gameStateListner);

        //Tworzenie wątku odpowiadającego za logikę
        Thread thread = new Thread(this);
        thread.start();

        //Tworzenie wątku odpowiadającego za generacje jedzenia
        Thread foodThread = new Thread(this::generateFood);
        foodThread.start();

        // Tworzenie i uruchamianie wątku odświeżającego część graficzną
        Thread refreshThread = new Thread(this::refreshThreadLoop);
        refreshThread.start();
    }





    @Override
    public void run() {
        while (gameOngoing) {
            if (isGamePaused) {
                try {
                    synchronized (this) {
                        wait();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            playerScore++;
            //Segmenty, które w poprzednim ruchu miały wartość 1 mają zmienianą wartość na 2
            gameBoard[snakeY][snakeX] = 2;

            //Wykonanie ruchu
            snakeX += deltaX;
            snakeY += deltaY;

            try {
                //Sprawdzamy, czy w tym ruchu snake zjadł jabłko
                int cellValue = gameBoard[snakeY][snakeX];
                if (cellValue == 3) {
                    FoodEatenEvent foodEatenEvent = new FoodEatenEvent(this, FoodType.APPLE);
                    fireFoodEvent(foodEatenEvent);
                }  //Sprawdzamy, czy w tym ruchu snake zjadł złote jabłko
                else if (cellValue == 4) {
                    FoodEatenEvent foodEatenEvent = new FoodEatenEvent(this, FoodType.GOLDENAPPLE);
                    fireFoodEvent(foodEatenEvent);
                } else if (cellValue == 5) {
                    FoodEatenEvent foodEatenEvent = new FoodEatenEvent(this, FoodType.SCISSORS);
                    fireFoodEvent(foodEatenEvent);
                } else if (cellValue == 6) {
                    FoodEatenEvent foodEatenEvent = new FoodEatenEvent(this, FoodType.BLACKAPPLE);
                    fireFoodEvent(foodEatenEvent);
                }  //Sprawdzamy, czy w tym ruchu snake zjadł swój ogon
                else if (snakeLenght >= 2 && cellValue == 2) {
                    fireGameState(new GameStateEvent(this, GameState.GAMEOVER));
                }
            } catch (ArrayIndexOutOfBoundsException ignored) {
                //Ignorujemy wyjątek
            }

            //Jeśli gracz wyjdzie poza plansze następuje koniec gry
            try {
                gameBoard[snakeY][snakeX] = 1;
            } catch (ArrayIndexOutOfBoundsException e) {
                fireGameState(new GameStateEvent(this, GameState.GAMEOVER));
            }

            //Dodajemy nowy segment do mapy segmentów
            synchronized (this){
                segmentsMap.put(MAPINDEX, new int[]{snakeX, snakeY});
            }

            MAPINDEX++;

            //Usuwanie segmentów niebędących częścią mapy
            cleanNonRecentSegments();

            //Oczekiwanie na wykonanie następnego ruchu
            try {
                synchronized (this) {
                    int baseWaitTime = 400;
                    int minWaitTime = 200;
                    int waitTime = Math.max(minWaitTime, baseWaitTime - (snakeLenght*10));
                    wait(waitTime);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void generateFood() {
        while (gameOngoing) {
            if (isGamePaused) {
                try {
                    synchronized (this) {
                        wait();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            int baseWaitTime = 2000;
            int minWaitTime = 400;
            int waitTime = Math.max(minWaitTime, baseWaitTime - (snakeLenght*10));
                double randomValue = random.nextDouble();
                if (randomValue < 0.01) {
                    fireFoodGeneratedEvent(
                            new FoodGeneratedEvent(this,FoodType.SCISSORS)
                    ); // 1% szansy na pojawienie się nożyc
                } else if (randomValue < 0.06) {
                    fireFoodGeneratedEvent(
                            new FoodGeneratedEvent(this,FoodType.BLACKAPPLE)
                    );
                    //5% szansy na pojawienie się czarnego jabłka
                } else if (randomValue < 0.11) {
                    fireFoodGeneratedEvent(
                            new FoodGeneratedEvent(this,FoodType.GOLDENAPPLE)
                    ); // 10% szansy na pojawienie się złotego jabłka
                } else {
                    fireFoodGeneratedEvent(
                            new FoodGeneratedEvent(this,FoodType.APPLE)
                    ); // 89% szansy na pojawienie się zwykłego jabłka
                }
            try {
                synchronized (this) {
                    wait(waitTime);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void initializeGameBoard() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                gameBoard[row][col] = 0;
            }
        }
    }

    @Override
    public void setDirection(ChangeDirectionEvent changeDirectionEvent) {
        this.DIRECTION = changeDirectionEvent.getDirection();
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

    public void cleanNonRecentSegments() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int cellValue = gameBoard[row][col];
                if (cellValue == 2) {
                    if (!isRecentSegment(row, col))
                        gameBoard[row][col] = 0;
                }
            }
        }
    }

    @Override
    public boolean isRecentSegment(int row, int col) {
        synchronized (this){
            for (int[] segment : segmentsMap.values()) {
                int segmentRow = segment[1];
                int segmentCol = segment[0];
                if (segmentRow == row && segmentCol == col) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public boolean getIsGameOngoing() {
        return gameOngoing;
    }

    private void fireFoodEvent(FoodEatenEvent foodEatenEvent) {
        for (FoodEventListner listener : foodEventListners) {
            listener.consoooomeFood(foodEatenEvent);
        }
    }
    private void fireFoodGeneratedEvent(FoodGeneratedEvent foodGeneratedEvent) {
        for (GenereteFoodListner listener : genereteFoodListners) {
            listener.generateFood(foodGeneratedEvent);
        }
    }

    @Override
    public void fireGameState(GameStateEvent gameStateEvent) {
        for (GameStateListner listener : gameStateListners) {
            listener.changeGameState(gameStateEvent);
        }
    }

    public void refreshThreadLoop() {
        while (gameOngoing) {
            if (isGamePaused) {
                try {
                    synchronized (this) {
                        wait();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            fireRefresh(new RefreshEvent(this, this));
            try {
                synchronized (this) {
                    wait(100);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void fireRefresh(RefreshEvent refreshEvent) {
        for (RefreshListner listener : refreshListners) {
            listener.refresh(refreshEvent);
        }
    }

    public void addRefreshListner(RefreshListner listner) {
        this.refreshListners.add(listner);
    }

    public void addFoodEventListner(FoodEventListner listner) {
        this.foodEventListners.add(listner);
    }

    public void addGameStateListner(GameStateListner listner) {
        this.gameStateListners.add(listner);
    }
    private void addGenerateFoodListner(GenereteFoodListner genereteFoodListner) {
        this.genereteFoodListners.add(genereteFoodListner);
    }

    @Override
    public Direction getCurrentDirection() {
        return DIRECTION;
    }

    @Override
    public int getCellValue(int row, int col) {
        if (row >= 0 && row < ROWS && col >= 0 && col < COLS) {
            return gameBoard[row][col];
        } else {
            throw new IllegalArgumentException("Invalid row or column value");
        }
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

    @Override
    public String getPlayerName() {
        return playerName;
    }

    @Override
    public void newGame() {
        segmentsMap.clear();
        initializeGameBoard();
        cleanNonRecentSegments();
        snakeX = 8;
        snakeY = 12;
        snakeLenght = 1;
        playerScore = 0;
        isGamePaused = false;
        gameOngoing = true;
        synchronized (this){
            notifyAll();
        }
    }

    @Override
    public void gameOver() {
        //gameOngoing = false;
        isGamePaused = true;
        PlayerScore playerScore = new PlayerScore(playerName,this.playerScore);
        playerScores.add(playerScore);
    }

    @Override
    public void pauseGame() {
        isGamePaused = true;
    }

    @Override
    public void resumeGame() {
        isGamePaused = false;
        synchronized (this) {
            notifyAll();
        }
    }

    @Override
    public ArrayList<PlayerScore> getPlayerScores() {
        return playerScores;
    }

    @Override
    public boolean getIspauseGame() {
        return isGamePaused;
    }

    @Override
    public String getPLayerName() {
        return playerName;
    }

    @Override
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}
