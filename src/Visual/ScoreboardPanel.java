package Visual;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

import Events.RefreshEvent;
import InterfaceLink.BoardLink;
import InterfaceLink.RefreshListner;
import Logic.PlayerScore;


public class ScoreboardPanel extends JPanel implements RefreshListner {

    private final ScoreboardTableModel tableModel;
    private ArrayList<PlayerScore> playerScores;
    private final BoardLink boardLink;


    public ScoreboardPanel(BoardLink boardLink) {
        this.boardLink = boardLink;
        this.playerScores = boardLink.getPlayerScores();

        setLayout(new BorderLayout());
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


    @Override
    public void refresh(RefreshEvent evt) {
        playerScores = boardLink.getPlayerScores();
        playerScores.sort(Collections.reverseOrder());
        int count = Math.min(playerScores.size(), 10);
        for (int i = 0; i < count; i++) {
            PlayerScore playerScore = playerScores.get(i);
            tableModel.setValueAt(i + 1, i, 0);
            tableModel.setValueAt(playerScore.playerName(), i, 1);
            tableModel.setValueAt(playerScore.playerScore(), i, 2);
        }
    }


    private class ScoreboardTableModel extends DefaultTableModel {

        private final String[] columnNames = {"#", "Name", "Score"};
        private Object[][] data = new Object[10][3];


        public ScoreboardTableModel() {
            setData(playerScores, Math.min(10, playerScores.size()));
        }


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
