package Visual;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

import Events.RefreshEvent;
import InterfaceLink.RefreshListner;
import Logic.BoardLogic;
import Logic.PlayerScore;

public class ScoreboardPanel extends JPanel implements RefreshListner {

    private JTable table;
    private DefaultTableModel tableModel;
    private ArrayList<PlayerScore> playerScores;

    public ScoreboardPanel(BoardLogic boardLogic) {
        setLayout(new BorderLayout());
        this.setBackground(Color.BLACK);

        // Create the table with default table model
        tableModel = new DefaultTableModel();
        tableModel.addColumn("Name");
        tableModel.addColumn("Score");
        table = new JTable(tableModel);
        this.playerScores = boardLogic.getPlayerScores();

        // Add the table to a scroll pane
        JScrollPane scrollPane = new JScrollPane(table);

        // Add the scroll pane to the panel
        add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    public void refresh(RefreshEvent evt) {
        tableModel.setRowCount(0);

        playerScores.sort(Collections.reverseOrder());

        int count = Math.min(playerScores.size(), 10);
        for (int i = 0; i < count; i++) {
            PlayerScore playerScore = playerScores.get(i);
            Object[] rowData = {playerScore.getPlayerName(), playerScore.getPlayerScore()};
            tableModel.addRow(rowData);
        }
    }
}
