package Logic;

import Enums.GameState;
import Events.GameStateEvent;
import InterfaceLink.BoardLink;
import InterfaceLink.GameStateListner;

import java.io.*;
import java.util.ArrayList;
public class FileHandler implements GameStateListner, InterfaceLink.FileHandler {
    private ArrayList<PlayerScore> playerScores;
    private ArrayList<PlayerScore> loadedPlayerScoresList;
    private final BoardLink boardLink;

    public FileHandler(BoardLink boardLink) {

        this.boardLink = boardLink;
    }

    public void writePoints() {
        playerScores = boardLink.getPlayerScores();



        try (FileOutputStream fos = new FileOutputStream("playerScores.bin",false)) {
            // Sprawdzenie istnienia pliku
            /*File file = new File("playerScores.bin");
            boolean fileExists = file.exists();*/

            // Wyczyszczenie zawartości pliku, jeśli już istnieje
           /* if (fileExists) {
                if (file.delete()) {
                    System.out.println("Previous file deleted successfully.");
                } else {
                    System.out.println("Failed to delete previous file.");
                }

                if (file.createNewFile()) {
                    System.out.println("New file created successfully.");
                } else {
                    System.out.println("Failed to create new file.");
                }
            }*/

            for (PlayerScore playerscore:
                 playerScores) {
                System.out.println("Zapisujemy: " + playerscore.toString());
            }


            // Ograniczenie listy do 10 największych wartości
            int numRecords = Math.min(playerScores.size(), 10);


            for (int i = 0; i < numRecords; i++) {
                // Zapis pola LEN (ilości znaków opisujących nazwę gracza) jako 1-bajtowa liczba całkowita
                fos.write((byte) playerScores.get(i).playerName().length());

                // Zapis sekwencji LEN bajtów zawierających znaki składające się na nazwę gracza
                fos.write(playerScores.get(i).playerName().getBytes());

                // Zapis 4-bajtowej liczby całkowitej opisującej ilość zdobytych punktów
                fos.write(intToByteArray(playerScores.get(i).playerScore()));
            }
            System.out.println("zapisaliśmy plik");
        } catch (IOException e) {
            System.out.println("nie udało się zapisać pliku");
        }
    }

    // Konwersja liczby całkowitej na tablicę 4 bajtów
    private static byte[] intToByteArray(int value) {
        byte[] byteArray = new byte[4];
        byteArray[0] = (byte) (value >> 24);
        byteArray[1] = (byte) (value >> 16);
        byteArray[2] = (byte) (value >> 8);
        byteArray[3] = (byte) value;
        return byteArray;
    }

    public void loadPoints() {
        loadedPlayerScoresList = null;
        try (FileInputStream fis = new FileInputStream("playerScores.bin")) {
            loadedPlayerScoresList = new ArrayList<>();
            for (int i = 0; i < fis.available(); i++) {
                // Odczyt pola LEN (ilości znaków opisujących nazwę gracza) jako 1-bajtowa liczba całkowita
                int nameLength = fis.read();
                if (nameLength < 0) {
                    throw new IOException("Failed to read the name length.");
                }

                // Odczyt sekwencji LEN bajtów zawierających znaki składające się na nazwę gracza
                byte[] nameBytes = new byte[nameLength];
                if (fis.read(nameBytes) != nameLength) {
                    throw new IOException("Failed to read the player name.");
                }
                String name = new String(nameBytes);

                // Odczyt 4-bajtowej liczby całkowitej opisującej ilość zdobytych punktów
                byte[] pointsBytes = new byte[4];
                if (fis.read(pointsBytes) != 4) {
                    throw new IOException("Failed to read the player score.");
                }
                int points = byteArrayToInt(pointsBytes);

                // Dodanie odczytanych danych do listy
                PlayerScore newPlayerScore = new PlayerScore(name,points);
                System.out.println("Odczytujemy: " + newPlayerScore);
                loadedPlayerScoresList.add(newPlayerScore);

                //Wygenerowanie brakujących wyników punktowych
                if (loadedPlayerScoresList.size() < 10) {
                    int missingValues = 10 - loadedPlayerScoresList.size();
                    int maxPointsValue = findMinScore();
                    generateRandomScores(missingValues, maxPointsValue);
                }


                boardLink.setPlayerScores(loadedPlayerScoresList);

            }
            System.out.println("odczytaliśmy plik");
        } catch (IOException e) {
            System.out.println("nie odnaleźliśmy pliku");
            loadedPlayerScoresList = new ArrayList<>();
            generateRandomScores(10, 1000);
            boardLink.setPlayerScores(loadedPlayerScoresList);
        }

    }

    // Konwersja tablicy 4 bajtów na liczbę całkowitą
    private static int byteArrayToInt(byte[] byteArray) {
        int value = 0;
        value |= byteArray[0] & 0xFF;
        value <<= 8;
        value |= byteArray[1] & 0xFF;
        value <<= 8;
        value |= byteArray[2] & 0xFF;
        value <<= 8;
        value |= byteArray[3] & 0xFF;
        return value;
    }

    private void generateRandomScores(int amount, int maxPointsValue) {

        for (int i = 0; i < amount; i++) {
            String playerName = "Player " + (i + 1);
            int playerScore = (int) (Math.random() * Math.min(maxPointsValue, 1000)); // Generacja losowych graczy
            loadedPlayerScoresList.add(new PlayerScore(playerName, playerScore));
        }
    }

    private int findMinScore() {
        if (loadedPlayerScoresList.isEmpty()) {
            throw new IllegalArgumentException("Lista wyników jest pusta.");
        }

        int minScore = loadedPlayerScoresList.get(0).playerScore();

        for (int i = 1; i < loadedPlayerScoresList.size(); i++) {
            int currentScore = loadedPlayerScoresList.get(i).playerScore();
            if (currentScore < minScore) {
                minScore = currentScore;
            }
        }

        return minScore;

    }
    @Override
    public void changeGameState(GameStateEvent gameStateEvent) {
        GameState gameState = gameStateEvent.getGameState();
        if (gameState == GameState.GAMEOVER) {
            playerScores = boardLink.getPlayerScores();
            loadedPlayerScoresList = playerScores;
            writePoints();
        } else if (gameState == GameState.NEWGAME) {
            playerScores = boardLink.getPlayerScores();
            if (playerScores == null || playerScores.isEmpty()) {
                loadPoints();
            }
        }
    }

    @Override
    public ArrayList<PlayerScore> getPlayerScores() {
        return playerScores;
    }

}
