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

        try (FileOutputStream fos = new FileOutputStream("playerScores.bin", false)) {


            int numRecords = Math.min(playerScores.size(), 10);

            for (int i = 0; i < numRecords; i++) {

                fos.write((byte) playerScores.get(i).playerName().length());


                fos.write(playerScores.get(i).playerName().getBytes());


                fos.write(intToByteArray(playerScores.get(i).playerScore()));
            }
            System.out.println("zapisaliśmy plik");
        } catch (IOException e) {
            System.out.println("nie udało się zapisać pliku");
        }
    }


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

                int nameLength = fis.read();
                if (nameLength < 0) {
                    throw new IOException("Failed to read the name length.");
                }

                byte[] nameBytes = new byte[nameLength];
                if (fis.read(nameBytes) != nameLength) {
                    throw new IOException("Failed to read the player name.");
                }
                String name = new String(nameBytes);


                byte[] pointsBytes = new byte[4];
                if (fis.read(pointsBytes) != 4) {
                    throw new IOException("Failed to read the player score.");
                }
                int points = byteArrayToInt(pointsBytes);

                PlayerScore newPlayerScore = new PlayerScore(name, points);
                loadedPlayerScoresList.add(newPlayerScore);


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
            generateRandomScores(10, 500);
            boardLink.setPlayerScores(loadedPlayerScoresList);
        }

    }


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
            int playerScore = (int) (Math.random() * Math.min(maxPointsValue, 1000));
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
