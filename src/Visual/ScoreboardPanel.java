package Visual;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

import Events.RefreshEvent;
import InterfaceLink.RefreshListner;
import Logic.BoardLogic;
import Logic.PlayerScore;

public class ScoreboardPanel extends JPanel implements RefreshListner {

    private JTable table;
    private ScoreboardTableModel tableModel;
    protected ArrayList<PlayerScore> playerScores;

    public ScoreboardPanel(BoardLogic boardLogic) {
        setLayout(new BorderLayout());
        this.setBackground(Color.BLACK);

        this.playerScores = boardLogic.getPlayerScores();

        if(playerScores.isEmpty()){
            for (int i = 0; i < 10; i++) {
                String playerName = "Player " + (i + 1);
                int playerScore = (int) (Math.random() * 1000); // Generate a random score
                playerScores.add(new PlayerScore(playerName, playerScore));
            }
        }

        tableModel = new ScoreboardTableModel();
        table = new JTable(tableModel);


        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(30);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(200,200));
        scrollPane.setMaximumSize(new Dimension(200,200));
        this.setPreferredSize(new Dimension(200,200));
        setBackground(Color.GRAY);
        add(scrollPane, BorderLayout.CENTER);
    }



    @Override
    public void refresh(RefreshEvent evt) {
        playerScores.sort(Collections.reverseOrder());
        int count = Math.min(playerScores.size(), 10);
        for (int i = 0; i < count; i++) {
            PlayerScore playerScore = playerScores.get(i);
            tableModel.setValueAt(i + 1, i, 0);
            tableModel.setValueAt(playerScore.getPlayerName(), i, 1);
            tableModel.setValueAt(playerScore.getPlayerScore(), i, 2);
        }
    }

    private  class ScoreboardTableModel extends DefaultTableModel {

        private final String[] columnNames = {"#", "Name", "Score"};
        private Object[][] data = new Object[10][3];


        public ScoreboardTableModel() {
            setData(playerScores,10);
        }

        public void setData(ArrayList<PlayerScore> playerScores, int count) {
            data = new Object[count][3];
            for (int i = 0; i < count; i++) {
                PlayerScore playerScore = playerScores.get(i);
                data[i][0] = i + 1;
                data[i][1] = playerScore.getPlayerName();
                data[i][2] = playerScore.getPlayerScore();
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
