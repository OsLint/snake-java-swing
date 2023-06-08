package Visual;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;

import Events.RefreshEvent;
import InterfaceLink.BoardLink;
import InterfaceLink.RefreshListner;

public class BoardVisual extends JPanel implements RefreshListner {
    private final JPanel playerScore;
    private final BoardLink boardLink;
    private final DefaultTableModel model;
    private int [] [] gameBoard;
    private final ImageIcon northSnakeHeadIcon;
    private final ImageIcon southSnakeHeadIcon;
    private final ImageIcon westSnakeHeadIcon;
    private final ImageIcon eastSnakeHeadIcon;
    private final ImageIcon snakeBodyIcon;
    private final ImageIcon grassIcon;
    private final ImageIcon appleIcon;

    public BoardVisual(BoardLink boardLink) {
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        String northSnakeHeadImagePath = "snakeHeadNorth.png";
        String westSnakeHeadImagePath = "snakeHeadWest.png";
        String eastSnakeHeadImagePath = "snakeHeadEast.png";
        String southSnakeHeadImagePath = "snakeHeadSouth.png";
        String snakeBodyImagePath ="snakeBody.png";
        String grassImagePath = "grass20x20.png";
        String appleImagePath = "apple20x20.png";
        northSnakeHeadIcon = new ImageIcon(northSnakeHeadImagePath);
        southSnakeHeadIcon = new ImageIcon(southSnakeHeadImagePath);
        westSnakeHeadIcon = new ImageIcon(westSnakeHeadImagePath);
        eastSnakeHeadIcon = new ImageIcon(eastSnakeHeadImagePath);
        snakeBodyIcon = new ImageIcon(snakeBodyImagePath);
        grassIcon = new ImageIcon(grassImagePath);
        appleIcon = new ImageIcon(appleImagePath);
        this.boardLink = boardLink;
        gameBoard = boardLink.getGameBoard();
        model = new DefaultTableModel(boardLink.getRows(),boardLink.getCols());
        playerScore = new JPanel(){
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.PLAIN, 20));
                g.drawString("Score: " + boardLink.getPLayerScore(), (this.getWidth()/2)-50, 21);
            }
        };
        int cellSize = 20;
        JTable table = new JTable(model);
        Border tableBorder = BorderFactory.createDashedBorder(Color.BLACK, 5, 2, 2, false);
        table.setBorder(tableBorder);
        table.setRowHeight(cellSize);
        table.setTableHeader(null);
        table.setBorder(tableBorder);
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

        playerScore.setBackground(Color.BLACK);
        setBackground(Color.BLACK);
        add(table,BorderLayout.CENTER);
        add(playerScore,BorderLayout.SOUTH);
        boardLink.initializeGameBoard();
        repaintTable();


    }

    @Override
    public void refresh(RefreshEvent evt) {
        this.gameBoard = boardLink.getGameBoard();
        playerScore.repaint();
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
                case 1 ->  {
                    if(boardLink.getCurrentDirection() != null){
                        switch (boardLink.getCurrentDirection()){
                            case UP -> g.drawImage(
                                    northSnakeHeadIcon.getImage(), 0, 0, getWidth(), getHeight(), null);
                            case DOWN -> g.drawImage(
                                    southSnakeHeadIcon.getImage(), 0, 0, getWidth(), getHeight(), null);
                            case LEFT -> g.drawImage(
                                    westSnakeHeadIcon.getImage(), 0, 0, getWidth(), getHeight(), null);
                            case RIGHT -> g.drawImage(
                                    eastSnakeHeadIcon.getImage(), 0, 0, getWidth(), getHeight(), null);
                        }
                    }else {
                        g.drawImage(
                                northSnakeHeadIcon.getImage(), 0, 0, getWidth(), getHeight(), null);
                    }
                }
                case 2 -> g.drawImage(snakeBodyIcon.getImage(),0,0,getWidth(),getHeight(),null);
                case 3 -> g.drawImage(appleIcon.getImage(),0,0,getWidth(),getHeight(),null);
            }
        }
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
        ) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            colorValue = gameBoard[row][column];
            switch (colorValue){
                case 0 -> component.setBackground(Color.BLACK);
                case 1 -> component.setBackground(Color.ORANGE);
                case 2 -> component.setBackground(Color.GREEN);
                case 3 -> component.setBackground(Color.RED);
            }
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
