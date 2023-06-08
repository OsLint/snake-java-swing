package Logic;

import Events.FoodEatenEvent;
import Events.RefreshEvent;
import InterfaceLink.BoardLink;
import InterfaceLink.ChangeDirectionListner;
import InterfaceLink.FoodEventListner;
import InterfaceLink.RefreshListner;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import static java.lang.Math.round;


public class BoardLogic implements BoardLink, Runnable, ChangeDirectionListner, FoodEventListner {
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
    public  Direction DIRECTION;
    private final Map<Integer, int[]> segmentsMap;
    private final ArrayList<RefreshListner> refreshListners = new ArrayList<>();
    private final ArrayList<FoodEventListner> foodEventListners = new ArrayList<>();

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
        Thread thread = new Thread(this);
        thread.start();

        //Tworzenie wątku odpowiadającego za generacje jedzenia
        Thread foodThread = new Thread(this::generateFood);
        foodThread.start();

        // Tworzenie i uruchamianie wątku odświeżającego część graficzną
        Thread refreshThread = new Thread(this::refreshThreadLoop);
        refreshThread.start();


    }

    public void setPlayerName (String playerName) {
        this.playerName = playerName;
    }
    @Override
    public void run() {
        while (gameOngoing) {
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
                notifyFoodEventListners(foodEatenEvent);
            }  //Sprawdzamy, czy w tym ruchu snake zjadł złote jabłko
            else if (cellValue == 4) {
                FoodEatenEvent foodEatenEvent = new FoodEatenEvent(this, FoodType.GOLDENAPPLE);
                notifyFoodEventListners(foodEatenEvent);
            } else if(cellValue == 5){
                FoodEatenEvent foodEatenEvent = new FoodEatenEvent(this, FoodType.SCISSORS);
                notifyFoodEventListners(foodEatenEvent);
            } //Sprawdzamy, czy w tym ruchu snake zjadł swój ogon
            else if(snakeLenght >= 2 && cellValue == 2) {
                System.out.println("Sam się zjadłeś koleś");
                gameOngoing = false;
            }
            }catch (ArrayIndexOutOfBoundsException ignored) {
                //Ignorujemy wyjątek
            }

            //Jeśli gracz wyjdzie poza plansze następuje koniec gry
            try{
                gameBoard[snakeY][snakeX] = 1;
            }catch (ArrayIndexOutOfBoundsException e){
                System.out.println("koniec gry");
                gameOngoing = false;
            }

            //Dodajemy nowy segment do mapy segmentów
            segmentsMap.put(MAPINDEX,new int[]{snakeX,snakeY});
            MAPINDEX++;

            //Usuwanie segmentów niebędących częścią mapy
            for (int row = 0; row < ROWS; row++) {
                for (int col = 0; col < COLS; col++) {
                    if(!(gameBoard[row][col] == 3)){
                        if(!isRecentSegment(row,col))
                            gameBoard[row][col] = 0;
                    }
                }
            }

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
            Random random = new Random();
            int foodRow = random.nextInt(ROWS);
            int foodCol = random.nextInt(COLS);
            int baseWaitTime = 2;
            int minWaitTime = 1;
            int waitTime = Math.max(minWaitTime, baseWaitTime / snakeLenght);
            //Zabezpieczenie by jedzenie nie pojawiło się w wężu
            if(!isRecentSegment(foodRow,foodCol)){
                gameBoard[foodRow][foodCol] = 3;
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
    public boolean getIsGameOngoing() {
        return gameOngoing;
    }
    @Override
    public void consoooomeFood(FoodType foodType) {
        switch (foodType){
            case APPLE -> {
                playerScore +=10;
                snakeLenght++;
            }
            case SCISSORS -> {
                if(snakeLenght >2){
                    snakeLenght = round(((float)snakeLenght/2));
                }
            }
            case GOLDENAPPLE -> {
                playerScore +=100;
                snakeLenght++;
            }
        }
    }
    private void notifyFoodEventListners(FoodEatenEvent foodEatenEvent) {
        for (FoodEventListner listener : foodEventListners) {
            listener.consoooomeFood(foodEatenEvent.getFoodType());
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
    @Override
    public Direction getCurrentDirection() {
        return DIRECTION;
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


}
