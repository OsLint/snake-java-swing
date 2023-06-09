package Logic;

import Events.ChangeDirectionEvent;
import Events.FoodEatenEvent;
import Events.GameStateEvent;
import Events.RefreshEvent;
import InterfaceLink.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;



public class BoardLogic implements BoardLink, Runnable, ChangeDirectionListner, FoodEventListner, GameStateListner {
    private final int[][] gameBoard;
    private static final int ROWS = 25;
    private static final int COLS = 16;
    private static int MAPINDEX = 0;
    private String playerName;
    private int deltaX = 0;
    private int deltaY = 0;
    private int snakeX = 8;
    private int snakeY = 12;
    private int snakeLenght = 1;
    private int playerScore = 0;
    private boolean gameOngoing;
    private boolean isGamePaused;
    private  Direction DIRECTION;
    private final Map<Integer, int[]> segmentsMap;
    private final ArrayList<RefreshListner> refreshListners = new ArrayList<>();
    private final ArrayList<FoodEventListner> foodEventListners = new ArrayList<>();
    private final ArrayList<GameStateListner> gameStateListners = new ArrayList<>();
    private final Thread thread;
    private final Thread foodThread;
    private final Thread refreshThread;

    public BoardLogic() {
        //Inicjalizacja pól prywatnych
        gameBoard = new int[ROWS][COLS];
        gameOngoing = true;
        segmentsMap = new LinkedHashMap<>(2, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, int[]> eldest) {
                return size() == snakeLenght+1;
            }
        };

        //Tworzenie wątku odpowiadającego za logikę
        thread = new Thread(this);
        thread.start();

        //Tworzenie wątku odpowiadającego za generacje jedzenia
        foodThread = new Thread(this::generateFood);
        foodThread.start();

        // Tworzenie i uruchamianie wątku odświeżającego część graficzną
        refreshThread = new Thread(this::refreshThreadLoop);
        refreshThread.start();


    }

    @Override
    public void setPlayerName (String playerName) {
        this.playerName = playerName;
    }
    @Override
    public void run() {
        while (gameOngoing) {
            System.out.println("main thread is working");
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

            try{
                //Sprawdzamy, czy w tym ruchu snake zjadł jabłko
                int cellValue = gameBoard[snakeY][snakeX];
            if(cellValue == 3) {
                FoodEatenEvent foodEatenEvent = new FoodEatenEvent(this, FoodType.APPLE);
                fireFoodEvent(foodEatenEvent);
            }  //Sprawdzamy, czy w tym ruchu snake zjadł złote jabłko
            else if (cellValue == 4) {
                FoodEatenEvent foodEatenEvent = new FoodEatenEvent(this, FoodType.GOLDENAPPLE);
                fireFoodEvent(foodEatenEvent);
            } else if(cellValue == 5){
                FoodEatenEvent foodEatenEvent = new FoodEatenEvent(this, FoodType.SCISSORS);
                fireFoodEvent(foodEatenEvent);
            } else if (cellValue == 6) {
                FoodEatenEvent foodEatenEvent = new FoodEatenEvent(this, FoodType.BLACKAPPLE);
                fireFoodEvent(foodEatenEvent);
            }  //Sprawdzamy, czy w tym ruchu snake zjadł swój ogon
            else if(snakeLenght >= 2 && cellValue == 2) {
                fireGameState(new GameStateEvent(this,GameState.GAMEOVER));
            }
            }catch (ArrayIndexOutOfBoundsException ignored) {
                //Ignorujemy wyjątek
            }

            //Jeśli gracz wyjdzie poza plansze następuje koniec gry
            try{
                gameBoard[snakeY][snakeX] = 1;
            }catch (ArrayIndexOutOfBoundsException e){
                fireGameState(new GameStateEvent(this,GameState.GAMEOVER));
            }

            //Dodajemy nowy segment do mapy segmentów
            segmentsMap.put(MAPINDEX,new int[]{snakeX,snakeY});
            MAPINDEX++;

            //Usuwanie segmentów niebędących częścią mapy
            cleanNonRecentSegments();

            //Oczekiwanie na wykonanie następnego ruchu
            try {
                synchronized (this){
                    wait(300);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private void generateFood() {
        while (gameOngoing) {
            System.out.println("food gen is working");
            if (isGamePaused) {
                try {
                    synchronized (this) {
                        wait();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            Random random = new Random();
            int foodRow = random.nextInt(ROWS);
            int foodCol = random.nextInt(COLS);
            int baseWaitTime = 2;
            int minWaitTime = 1;
            int waitTime = Math.max(minWaitTime, baseWaitTime / snakeLenght);
            //Zabezpieczenie by jedzenie nie pojawiło się w wężu
            if(gameBoard[foodRow][foodCol] == 0){
                double randomValue = random.nextDouble();

                if (randomValue < 0.01) {
                    gameBoard[foodRow][foodCol] = 5; // 1% szansy na pojawienie się nożyc
                }else if (randomValue < 0.06) {
                    gameBoard[foodRow][foodCol] = 6;
                }else if (randomValue < 0.11) {
                    gameBoard[foodRow][foodCol] = 4; // 10% szansy na pojawienie się złotego jabłka
                } else {
                    gameBoard[foodRow][foodCol] = 3; // 89% szansy na pojawienie się zwykłego jabłka
                }

            }
            try {
                synchronized (this) {
                    wait(waitTime * 1000);
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
    public void cleanNonRecentSegments(){
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int cellValue  = gameBoard[row][col];
                if(cellValue == 2){
                    if(!isRecentSegment(row,col))
                        gameBoard[row][col] = 0;
                }
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
    public boolean getIsGameOngoing() {
        return gameOngoing;
    }
    @Override
    public void consoooomeFood(FoodEatenEvent foodEatenEvent) {
        switch (foodEatenEvent.getFoodType()){
            case APPLE -> {
                playerScore +=10;
                snakeLenght++;
            }
            case SCISSORS -> {
                if(snakeLenght > 2){
                        segmentsMap.clear();
                        snakeLenght = 1;
                }
            }
            case GOLDENAPPLE -> {
                playerScore +=100;
                snakeLenght++;
            }
            case BLACKAPPLE -> fireGameState(new GameStateEvent(this,GameState.GAMEOVER));
        }
    }
    private void fireFoodEvent(FoodEatenEvent foodEatenEvent) {
        for (FoodEventListner listener : foodEventListners) {
            listener.consoooomeFood(foodEatenEvent);
        }
    }
    private void fireGameState(GameStateEvent gameStateEvent){
        for (GameStateListner listener : gameStateListners) {
            listener.changeGameState(gameStateEvent);
        }
    }

    public void refreshThreadLoop() {
        while(gameOngoing) {
            System.out.println("refresh is working");
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
                synchronized (this){
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
    public void addFoodEventListner(FoodEventListner listner){
        this.foodEventListners.add(listner);
    }
    public void addGameStateListner(GameStateListner listner){
        this.gameStateListners.add(listner);
    }

    @Override
    public Direction getCurrentDirection() {
        return DIRECTION;
    }

    @Override
    public int getCellValue(int row,int col) {
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
        System.out.println("game gets message to do  new game");
        snakeX = 8;
        snakeY = 12;
        snakeLenght = 1;
        playerScore = 0;
        isGamePaused = false;
        notifyAll();

    }

    @Override
    public boolean getIspauseGame() {
        return isGamePaused;
    }



    @Override
    public void changeGameState(GameStateEvent gameStateEvent) {
        GameState gameState = gameStateEvent.getGameState();
        synchronized (this){
            switch (gameState){
                case PAUSED -> {
                    isGamePaused = true;
                    fireExcludedGameState(new GameStateEvent(this,GameState.PAUSED));
                }
                case UNPAUSED -> {
                    isGamePaused = false;
                    notifyAll();
                    fireExcludedGameState(new GameStateEvent(this,GameState.UNPAUSED));
                }
                case GAMEOVER -> {
                    isGamePaused = true;
                    fireExcludedGameState(new GameStateEvent(this,GameState.GAMEOVER));

                }
                case NEWGAME -> {
                    newGame();
                    fireExcludedGameState(new GameStateEvent(this,GameState.NEWGAME));
                }

            }
        }

    }

    private void fireExcludedGameState(GameStateEvent gameStateEvent){
        for (GameStateListner listener : gameStateListners) {
            if(listener != this){
                listener.changeGameState(gameStateEvent);
            }
        }
    }


}
