package Visual;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.KeyListener;

import Events.RefreshEvent;
import InterfaceLink.BoardLink;
import InterfaceLink.RefreshListner;

public class BoardVisual extends JPanel implements RefreshListner {
    private JLabel playerScore;
    private BoardLink boardLink;
    private JTable table;
    private DefaultTableModel model;
    private int [] [] gameBoard;
    private int cellSize = 20;



    public BoardVisual(BoardLink boardLink) {
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        this.boardLink = boardLink;
        this.gameBoard = boardLink.getGameBoard();
        this.playerScore = new JLabel("Score: " +
               boardLink.getPLayerScore());
        model = new DefaultTableModel(boardLink.getRows(),boardLink.getCols());
        table = new JTable(model);
        table.setRowHeight(cellSize);
        table.setTableHeader(null);
        // Set column widths
        for (int i = 0; i < boardLink.getCols(); i++) {
            TableColumn column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth(cellSize);
            column.setMaxWidth(cellSize);
            column.setMinWidth(cellSize);
            column.setCellRenderer(new CustomCellRenderer());
        }
        table.setCellSelectionEnabled(false);
        table.setDefaultEditor(Object.class,null);
        table.setFocusable(false);
        table.setDefaultRenderer(Object.class, new CustomCellRenderer());

        setBackground(Color.GRAY);

        playerScore.setForeground(Color.WHITE);
        playerScore.setFont(new Font("Arial",Font.PLAIN,20));
        add(playerScore);
        add(table);
        boardLink.initializeGameBoard();
        repaintTable();


    }

    @Override
    public void refresh(RefreshEvent evt) {
        this.gameBoard = boardLink.getGameBoard();
        this.repaintTable();
    }

    private class CustomCellRenderer extends DefaultTableCellRenderer {
        @Override
        public void setValue(Object value) {
            // Do nothing to hide the value
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            setBackground(Color.BLACK);

            if (getBackground().equals(Color.RED)) {
                g.setColor(Color.RED);
                int ovalWidth = Math.min(getWidth(), getHeight()) - 4;
                int ovalHeight = ovalWidth;
                int ovalX = (getWidth() - ovalWidth) / 2;
                int ovalY = (getHeight() - ovalHeight) / 2;
                g.fillOval(ovalX, ovalY, ovalWidth, ovalHeight);
            }
        }
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
        ) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            int colorValue = gameBoard[row][column];
            if (colorValue == 1) {
                component.setBackground(new Color(0,100,0));
            } else if (colorValue == 2) {
                component.setBackground(Color.RED);
                if (isSelected) {
                    component.setForeground(Color.WHITE);
                } else {
                    component.setForeground(Color.GREEN);
                }
            } else {
                component.setBackground(Color.GREEN);
            }
            return component;
        }
    }

    private void repaintTable() {
        for (int row = 0; row < boardLink.getRows(); row++) {
            for (int col = 0; col < boardLink.getCols(); col++) {
                model.setValueAt(gameBoard[row][col], row, col);
            }
        }
        revalidate();
        repaint();
    }
}
