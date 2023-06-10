package Logic;

import Enums.Direction;
import Enums.FoodType;
import Enums.GameState;
import Events.*;
import InterfaceLink.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/**
 *  Klasa BoardLogic reprezentuje logikę gry w węża.
 *  Zawiera metody i pola odpowiedzialne za obsługę planszy, sterowanie wężem,
 *  generację jedzenia oraz zarządzanie stanem gry.
 */
public class BoardLogic implements BoardLink, Runnable, ChangeDirectionListner {
    private final int[][] gameBoard; // Tablica reprezentująca planszę gry
    private static final int ROWS = 25; // Liczba wierszy planszy
    private static final int COLS = 16; // Liczba kolumn planszy
    private static int MAPINDEX = 0; // Indeks mapy segmentów węża
    private String playerName; // Nazwa gracza
    private final Random random; // Generator liczb losowych
    private int deltaX = 0; // Przesunięcie w osi X
    private int deltaY = 0; // Przesunięcie w osi Y
    private int snakeX = 8; // Pozycja X węża
    private int snakeY = 12; // Pozycja Y węża
    private int snakeLenght = 1; // Długość węża
    private int playerScore = 0; // Wynik gracza
    private boolean gameOngoing; // Flaga określająca, czy gra jest w toku
    private boolean isGamePaused; // Flaga określająca, czy gra jest zatrzymana
    private Direction DIRECTION; // Obecny kierunek ruchu węża
    private final Map<Integer, int[]> segmentsMap; // Mapa segmentów węża
    private final ArrayList<PlayerScore> playerScores = new ArrayList<>(); // Lista wyników graczy

    private final ArrayList<RefreshListner> refreshListners = new ArrayList<>(); // Lista obiektów nasłuchujących
    // zdarzenia odświeżenia planszy
    private final ArrayList<FoodEventListner> foodEventListners = new ArrayList<>(); // Lista obiektów nasłuchujących
    // zdarzenia zjedzenia jedzenia
    private final ArrayList<GameStateListner> gameStateListners = new ArrayList<>(); // Lista obiektów nasłuchujących
    // zmiany stanu gry
    private final ArrayList<GenereteFoodListner> genereteFoodListners = new ArrayList<>(); // Lista obiektów
    // nasłuchujących zdarzenia generacji jedzenia

    /**
     * Konstruktor klasy BoardLogic.
     * Inicjalizuje pola prywatne, tworzy mapę segmentów węża, dodaje obiekty nasłuchujące zdarzeń,
     * tworzy i uruchamia wątki odpowiedzialne za logikę gry, generację jedzenia oraz odświeżanie planszy.
     */
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
        Thread foodThread = new Thread(this::generateFoodLoop);
        foodThread.start();

        // Tworzenie i uruchamianie wątku odświeżającego część graficzną
        Thread refreshThread = new Thread(this::refreshThreadLoop);
        refreshThread.start();
    }

    /**
     * Metoda odpowiedzialna za uruchomienie gry.
     */
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
            synchronized (this) {
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
                    int waitTime = Math.max(minWaitTime, baseWaitTime - (snakeLenght * 10));
                    wait(waitTime);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    /**
     * Metoda odpowiedzialna za generowanie położenia jedzenia.
     */
    private void generateFoodLoop() {
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
            int waitTime = Math.max(minWaitTime, baseWaitTime - (snakeLenght * 10));
            double randomValue = random.nextDouble();
            if (randomValue < 0.01) {
                fireFoodGeneratedEvent(
                        new FoodGeneratedEvent(this, FoodType.SCISSORS)
                ); // 1% szansy na pojawienie się nożyc
            } else if (randomValue < 0.06) {
                fireFoodGeneratedEvent(
                        new FoodGeneratedEvent(this, FoodType.BLACKAPPLE)
                );
                //5% szansy na pojawienie się czarnego jabłka
            } else if (randomValue < 0.11) {
                fireFoodGeneratedEvent(
                        new FoodGeneratedEvent(this, FoodType.GOLDENAPPLE)
                ); // 10% szansy na pojawienie się złotego jabłka
            } else {
                fireFoodGeneratedEvent(
                        new FoodGeneratedEvent(this, FoodType.APPLE)
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

    /**
     * Metoda inicjalizująca planszę gry.
     */
    @Override
    public void initializeGameBoard() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                gameBoard[row][col] = 0;
            }
        }
    }

    /**
     * Metoda ustawiająca kierunek poruszania się gracza.
     *
     * @param changeDirectionEvent Obiekt z informacją o zmianie kierunku.
     */
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

    /**
     * Metoda usuwająca segmenty, które nie są częścią mapy segmentów.
     */
    public void cleanNonRecentSegments() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int cellValue = gameBoard[row][col];
                if (cellValue == 2) {
                    if (!recentSegment(row, col))
                        gameBoard[row][col] = 0;
                }
            }
        }
    }
    /**
     * Metoda sprawdzająca, czy dany segment jest częścią mapy segmentów.
     *
     * @param row Wiersz segmentu.
     * @param col Kolumna segmentu.
     * @return Wartość logiczna określająca, czy segment jest częścią mapy.
     */
    @Override
    public boolean recentSegment(int row, int col) {
        synchronized (this) {
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
    /**
     * Metoda odpowiedzialna za odświeżanie części wizualnej gry.
     */
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
            fireRefresh(new RefreshEvent(this));
            try {
                synchronized (this) {
                    wait(100);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    /**
     * Metoda rozpoczynająca nową grę.
     */
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
        synchronized (this) {
            notifyAll();
        }
    }
    /**
     * Metoda wywoływana po zakończeniu gry.
     */
    @Override
    public void gameOver() {
        isGamePaused = true;
        PlayerScore playerScore = new PlayerScore(playerName, this.playerScore);
        playerScores.add(playerScore);
    }
    /**
     * Metoda pozwalająca na zatrzymanie gry.
     */
    @Override
    public void pauseGame() {
        isGamePaused = true;
    }
    /**
     * Metoda pozwalająca na wznowienie gry.
     */
    @Override
    public void resumeGame() {
        isGamePaused = false;
        synchronized (this) {
            notifyAll();
        }
    }
    /**
     * Metoda wywołująca zdarzenie zmiany stanu gry.
     *
     * @param gameStateEvent Obiekt z informacją o zmianie stanu gry.
     */
    @Override
    public void fireGameState(GameStateEvent gameStateEvent) {
        for (GameStateListner listener : gameStateListners) {
            listener.changeGameState(gameStateEvent);
        }
    }
    /**
     * Metoda wywołująca zdarzenie odświeżenia ekranu gry.
     *
     * @param refreshEvent Obiekt z informacją o odświeżeniu.
     */
    private void fireRefresh(RefreshEvent refreshEvent) {
        for (RefreshListner listener : refreshListners) {
            listener.refresh(refreshEvent);
        }
    }
    /**
     * Metoda wywołująca zdarzenie zjedzenia jedzenia przez węża.
     *
     * @param foodEatenEvent Obiekt z informacją o zjedzonym jedzeniu.
     */
    private void fireFoodEvent(FoodEatenEvent foodEatenEvent) {
        for (FoodEventListner listener : foodEventListners) {
            listener.consoooomeFood(foodEatenEvent);
        }
    }
    /**
     * Metoda wywołująca zdarzenie wygenerowania jedzenia.
     *
     * @param foodGeneratedEvent Obiekt z informacją o wygenerowanym jedzeniu.
     */
    private void fireFoodGeneratedEvent(FoodGeneratedEvent foodGeneratedEvent) {
        for (GenereteFoodListner listener : genereteFoodListners) {
            listener.generateFood(foodGeneratedEvent);
        }
    }
    /**
     * Metoda dodająca słuchacza zdarzeń odświeżenia ekranu gry.
     * @param listner Obiekt słuchacza zdarzeń odświeżenia.
     */
    public void addRefreshListner(RefreshListner listner) {
        this.refreshListners.add(listner);
    }
    /**
     * Metoda dodająca słuchacza zdarzeń zjedzenia jedzenia przez węża.
     *
     * @param listner Obiekt słuchacza zdarzeń zjedzenia jedzenia.
     */
    public void addFoodEventListner(FoodEventListner listner) {
        this.foodEventListners.add(listner);
    }
    /**
     * Metoda dodająca słuchacza zdarzeń zmiany stanu gry.
     *
     * @param listner Obiekt słuchacza zdarzeń zmiany stanu gry.
     */
    public void addGameStateListner(GameStateListner listner) {
        this.gameStateListners.add(listner);
    }
    /**
     * Metoda dodająca słuchacza zdarzeń generowania jedzenia.
     *
     * @param genereteFoodListner Obiekt słuchacza zdarzeń generowania jedzenia.
     */
    private void addGenerateFoodListner(GenereteFoodListner genereteFoodListner) {
        this.genereteFoodListners.add(genereteFoodListner);
    }
    /**
     * Metoda ustawiająca nazwę gracza.
     *
     * @param playerName Nazwa gracza.
     */
    @Override
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    /**
     * Metoda zwracająca nazwę gracza.
     *
     * @return Nazwa gracza.
     */
    @Override
    public String getPlayerName() {
        return playerName;
    }
    /**
     * Metoda zwracająca listę wyników graczy.
     *
     * @return Lista wyników graczy.
     */
    @Override
    public ArrayList<PlayerScore> getPlayerScores() {
        return playerScores;
    }
    /**
     * Metoda zwracająca informację o zatrzymaniu gry.
     *
     * @return Wartość logiczna określająca, czy gra jest zatrzymana.
     */
    @Override
    public boolean getIspauseGame() {
        return isGamePaused;
    }
    /**
     * Metoda zwracająca nazwę gracza.
     *
     * @return Nazwa gracza.
     */
    @Override
    public String getPLayerName() {
        return playerName;
    }
    /**
     * Metoda zwracająca aktualny kierunek poruszania się gracza.
     *
     * @return Aktualny kierunek poruszania się gracza.
     */
    @Override
    public Direction getCurrentDirection() {
        return DIRECTION;
    }
    /**
     * Metoda zwracająca wartość komórki na planszy gry.
     *
     * @param row Wiersz komórki.
     * @param col Kolumna komórki.
     * @return Wartość komórki.
     * @throws IllegalArgumentException Jeżeli wiersz lub kolumna mają nieprawidłową wartość.
     */
    @Override
    public int getCellValue(int row, int col) {
        if (row >= 0 && row < ROWS && col >= 0 && col < COLS) {
            return gameBoard[row][col];
        } else {
            throw new IllegalArgumentException("Invalid row or column value");
        }
    }
    /**
     * Metoda zwracająca aktualny wynik gracza.
     *
     * @return Aktualny wynik gracza.
     */
    @Override
    public int getPLayerScore() {
        return playerScore;
    }
    /**
     * Metoda zwracająca planszę gry.
     *
     * @return Dwuwymiarowa tablica reprezentująca planszę gry.
     */
    @Override
    public int[][] getGameBoard() {
        return gameBoard;
    }
    /**
     * Metoda zwracająca liczbę wierszy planszy gry.
     *
     * @return Liczba wierszy planszy gry.
     */
    @Override
    public int getRows() {
        return ROWS;
    }
    /**
     * Metoda zwracająca liczbę kolumn planszy gry.
     *
     * @return Liczba kolumn planszy gry.
     */
    @Override
    public int getCols() {
        return COLS;
    }
    /**
     * Metoda zwracająca informację o trwaniu gry.
     *
     * @return Wartość logiczna określająca, czy gra trwa.
     */
    @Override
    public boolean getIsGameOngoing() {
        return gameOngoing;
    }
}
