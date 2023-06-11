package Visual;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

import Events.RefreshEvent;
import InterfaceLink.BoardLink;
import InterfaceLink.FileHandler;
import InterfaceLink.RefreshListner;
import Logic.PlayerScore;

/**
 * Klasa ScoreboardPanel reprezentuje panel wyników graczy.
 * Wyświetla tabelę z najlepszymi wynikami graczy.
 */
public class ScoreboardPanel extends JPanel implements RefreshListner {

    private final ScoreboardTableModel tableModel;
    protected ArrayList<PlayerScore> playerScores;

    /**
     * Konstruktor klasy ScoreboardPanel.
     *
     * @param fileHandler Obiekt typu filehandler, zapisujący i odczytujący wyniki graczy.
     */
    public ScoreboardPanel(BoardLink boardLink, FileHandler fileHandler) {
        setLayout(new BorderLayout());

        this.playerScores = boardLink.getPlayerScores();
        if(playerScores == null || playerScores.isEmpty()) {
            playerScores = fileHandler.getPlayerScores();
            if (playerScores == null || playerScores.isEmpty()) {
                playerScores = new ArrayList<>();
                for (int i = 0; i < 10; i++) {
                    String playerName = "Player " + (i + 1);
                    int playerScore = (int) (Math.random() * 1000); // Generacja losowych graczy
                    playerScores.add(new PlayerScore(playerName, playerScore));
                }
            }
        }

        tableModel = new ScoreboardTableModel();
        JTable table = new JTable(tableModel);


        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(30);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(this.getWidth(), this.getHeight() / 5));
        scrollPane.setMaximumSize(new Dimension(this.getWidth(), this.getHeight() / 5));
        scrollPane.setBackground(Color.GRAY);
        setBackground(Color.GRAY);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Metoda refresh wywoływana w momencie odświeżenia danych panelu.
     * Sortuje wyniki graczy malejąco i wyświetla maksymalnie 10 najlepszych wyników w tabeli.
     *
     * @param evt Obiekt typu RefreshEvent, informujący o zdarzeniu odświeżenia.
     */
    @Override
    public void refresh(RefreshEvent evt) {
        playerScores.sort(Collections.reverseOrder());
        int count = Math.min(playerScores.size(), 10);
        for (int i = 0; i < count; i++) {
            PlayerScore playerScore = playerScores.get(i);
            tableModel.setValueAt(i + 1, i, 0);
            tableModel.setValueAt(playerScore.playerName(), i, 1);
            tableModel.setValueAt(playerScore.playerScore(), i, 2);
        }
    }

    /**
     * Wewnętrzna klasa ScoreboardTableModel rozszerzająca DefaultTableModel.
     * Reprezentuje model tabeli wyników.
     */
    private class ScoreboardTableModel extends DefaultTableModel {

        private final String[] columnNames = {"#", "Name", "Score"};
        private Object[][] data = new Object[10][3];
        /**
         * Konstruktor klasy ScoreboardTableModel.
         * Inicjalizuje dane tabeli na podstawie listy wyników graczy.
         */
        public ScoreboardTableModel() {
            setData(playerScores, 10);
        }
        /**
         Ustawia dane tabeli na podstawie listy wyników graczy.
         @param playerScores Lista wyników graczy.
         @param count Maksymalna liczba wierszy w tabeli.
         */
        public void setData(ArrayList<PlayerScore> playerScores, int count) {
            data = new Object[count][3];
            for (int i = 0; i < count; i++) {
                PlayerScore playerScore = playerScores.get(i);
                data[i][0] = i + 1;
                data[i][1] = playerScore.playerName();
                data[i][2] = playerScore.playerScore();
            }
        }
        @Override
        public int getRowCount() {
            return 10;
        }
        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return data[rowIndex][columnIndex];
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            data[rowIndex][columnIndex] = aValue;
            fireTableCellUpdated(rowIndex, columnIndex);
        }
        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }
    }
}
