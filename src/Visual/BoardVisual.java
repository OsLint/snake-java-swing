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

    ImageIcon snakeHeadIcon = new ImageIcon("snakeheadtest.png");
    ImageIcon grassIcon = new ImageIcon("pixelartgrass.png");
    ImageIcon appleIcon = new ImageIcon("apple1.png");



    public BoardVisual(BoardLink boardLink) {
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        this.boardLink = boardLink;
        this.gameBoard = boardLink.getGameBoard();
        this.playerScore = new JLabel("Score: " +
               boardLink.getPLayerScore());
        model = new DefaultTableModel(boardLink.getRows(),boardLink.getCols());
        table = new JTable(model);
        int cellSize = 20;
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
        playerScore.setText("Score: " +
                boardLink.getPLayerScore());
        this.repaintTable();
    }

    private class CustomCellRenderer extends DefaultTableCellRenderer {
        int colorValue;
        @Override
        public void setValue(Object value) {
            // Do nothing to hide the value
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            setBackground(Color.BLACK);
            switch (colorValue){
                case 0 -> g.drawImage(grassIcon.getImage(),0,0,getWidth(),getHeight(),null);
                case 1 ->  g.drawImage(snakeHeadIcon.getImage(), 0, 0, getWidth(), getHeight(), null);
                case 2 -> System.out.println("bug");
                case 3 -> g.drawImage(appleIcon.getImage(),0,0,getWidth(),getHeight(),null);
            }
        }
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
        ) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            colorValue = gameBoard[row][column];
            component.setBackground(new Color(60,200,60));
            /*switch (colorValue){
                case 0 -> component.setBackground(Color.GREEN);
                case 1 -> component.setBackground(Color.GREEN);
                case 2 -> component.setBackground(Color.GREEN);
                case 3 -> component.setBackground(Color.GREEN);
            }*/
            return component;
        }
    }

    private void repaintTable() {
        for (int row = 0; row < boardLink.getRows(); row++) {
            for (int col = 0; col < boardLink.getCols(); col++) {
                int segmentValue = gameBoard[row][col];
                if (segmentValue == 2 && !boardLink.isRecentSegment(row, col)) {
                    segmentValue = 0;
                }
                model.setValueAt(segmentValue, row, col);
            }
        }
        revalidate();
        repaint();
    }
}
