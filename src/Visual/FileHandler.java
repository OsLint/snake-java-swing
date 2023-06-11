package Visual;

import Enums.GameState;
import Events.GameStateEvent;
import InterfaceLink.BoardLink;
import InterfaceLink.GameStateListner;
import Logic.PlayerScore;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class FileHandler implements GameStateListner, InterfaceLink.FileHandler {
    private ArrayList<PlayerScore> playerScores;
    private final BoardLink boardLink;
    public FileHandler(BoardLink boardLink){
        this.boardLink = boardLink;
    }

    public void writePoints(){
        playerScores = boardLink.getPlayerScores();
        playerScores.sort(Collections.reverseOrder());
        try (FileOutputStream fos = new FileOutputStream("playerScores.bin")) {
            // Ograniczenie listy do 10 największych wartości
            int numRecords = Math.min(playerScores.size(), 10);

            // Zapis ilości rekordów jako 4-bajtowa liczba całkowita
            fos.write(intToByteArray(numRecords));

            for (PlayerScore playerPoints : playerScores) {
                // Zapis pola LEN (ilości znaków opisujących nazwę gracza) jako 1-bajtowa liczba całkowita
                fos.write((byte) playerPoints.playerName().length());

                // Zapis sekwencji LEN bajtów zawierających znaki składające się na nazwę gracza
                fos.write(playerPoints.playerName().getBytes());

                // Zapis 4-bajtowej liczby całkowitej opisującej ilość zdobytych punktów
                fos.write(intToByteArray(playerPoints.playerScore()));
                System.out.println("zapisaliśmy plik");
            }
        }catch (IOException e){
            System.out.println("nie udało się odczytać pliku");
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

    public ArrayList<PlayerScore> loadPoints() {
        ArrayList<PlayerScore> loadedPlayerScoresList = null;
        try (FileInputStream fis = new FileInputStream("playerScores.bin")) {
            loadedPlayerScoresList = new ArrayList<>();

            // Odczyt ilości rekordów jako 4-bajtowa liczba całkowita
            byte[] numRecordsBytes = new byte[4];
            if (fis.read(numRecordsBytes) != 4) {
                throw new IOException("Failed to read the number of records.");
            }
            int numRecords = byteArrayToInt(numRecordsBytes);

            for (int i = 0; i < numRecords; i++) {
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
                loadedPlayerScoresList.add(new PlayerScore(name, points));
                System.out.println("odczytaliśmy plik");
            }
        } catch (IOException e) {
            System.out.println("nie odnaleźliśmy pliku");
        }

        return loadedPlayerScoresList;
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

    @Override
    public void changeGameState(GameStateEvent gameStateEvent) {
        GameState gameState = gameStateEvent.getGameState();
        if(gameState == GameState.GAMEOVER){
            playerScores = boardLink.getPlayerScores();
            writePoints();
        } else if (gameState == GameState.NEWGAME) {
            playerScores = loadPoints();
        }
    }

    @Override
    public ArrayList<PlayerScore> getPlayerScores() {
        return playerScores;
    }
}

