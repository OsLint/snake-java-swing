package Visual;

import InterfaceLink.BoardLink;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;

public class BoardVisual extends JPanel {
    private final ImageIcon northSnakeHeadIcon;
    private final ImageIcon southSnakeHeadIcon;
    private final ImageIcon westSnakeHeadIcon;
    private final ImageIcon eastSnakeHeadIcon;
    private final ImageIcon snakeBodyIcon;
    private final ImageIcon grassIcon;
    private final ImageIcon appleIcon;
    private final ImageIcon goldAppleIcon;
    private final ImageIcon blackAppleIcon;
    private final ImageIcon scissorsIcon;
    private final BoardLink boardLink;


    public BoardVisual(BoardLink boardLink, DefaultTableModel model) {
        //Inicjalizacja pól prywatnych
        this.boardLink = boardLink;
        northSnakeHeadIcon = new ImageIcon("snakeHeadNorth.png");
        southSnakeHeadIcon = new ImageIcon("snakeHeadSouth.png");
        westSnakeHeadIcon = new ImageIcon("snakeHeadWest.png");
        eastSnakeHeadIcon = new ImageIcon("snakeHeadEast.png");
        snakeBodyIcon = new ImageIcon("snakeBody.png");
        grassIcon = new ImageIcon("grass20x20.png");
        appleIcon = new ImageIcon("apple20x20.png");
        goldAppleIcon = new ImageIcon("goldapple20x20.png");
        blackAppleIcon = new ImageIcon("blackapple20x20.png");
        scissorsIcon = new ImageIcon("scissors20x20.png");

        JTable table = new JTable(model);
        //Ustawiamy wielkość komórek planszy
        for (int i = 0; i < boardLink.getCols(); i++) {
            TableColumn column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth(20);
            column.setMaxWidth(20);
            column.setMinWidth(20);
            column.setCellRenderer(new BoardVisual.CustomCellRenderer());
        }

        Border tableBorder = BorderFactory.createDashedBorder(
                Color.BLACK, 5, 2, 2, false
        );
        table.setBorder(tableBorder);
        table.setRowHeight(20);
        table.setTableHeader(null);
        table.setCellSelectionEnabled(false);
        table.setDefaultEditor(Object.class, null);
        table.setDefaultRenderer(Object.class, new BoardVisual.CustomCellRenderer());
        table.setPreferredSize(new Dimension(20 * 16, 20 * 25));
        table.setMaximumSize(new Dimension(20 * 16, 20 * 25));

        setBackground(Color.GRAY);
        add(table);
    }

    class CustomCellRenderer extends DefaultTableCellRenderer {
        int colorValue;

        @Override
        public void setValue(Object value) {
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            switch (colorValue) {
                case 0 -> g.drawImage(grassIcon.getImage(), 0, 0, getWidth(), getHeight(), null);
                case 1 -> {
                    if (boardLink.getCurrentDirection() != null) {
                        switch (boardLink.getCurrentDirection()) {
                            case UP -> g.drawImage(
                                    northSnakeHeadIcon.getImage(), 0, 0, getWidth(), getHeight(), null);
                            case DOWN -> g.drawImage(
                                    southSnakeHeadIcon.getImage(), 0, 0, getWidth(), getHeight(), null);
                            case LEFT -> g.drawImage(
                                    westSnakeHeadIcon.getImage(), 0, 0, getWidth(), getHeight(), null);
                            case RIGHT -> g.drawImage(
                                    eastSnakeHeadIcon.getImage(), 0, 0, getWidth(), getHeight(), null);
                        }
                    } else {
                        g.drawImage(
                                northSnakeHeadIcon.getImage(), 0, 0, getWidth(), getHeight(), null);
                    }
                }
                case 2 -> g.drawImage(snakeBodyIcon.getImage(), 0, 0, getWidth(), getHeight(), null);
                case 3 -> g.drawImage(appleIcon.getImage(), 0, 0, getWidth(), getHeight(), null);
                case 4 -> g.drawImage(goldAppleIcon.getImage(), 0, 0, getWidth(), getHeight(), null);
                case 5 -> g.drawImage(scissorsIcon.getImage(), 0, 0, getWidth(), getHeight(), null);
                case 6 -> g.drawImage(blackAppleIcon.getImage(), 0, 0, getWidth(), getHeight(), null);
            }
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
        ) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            colorValue = boardLink.getCellValue(row, column);
            switch (colorValue) {
                case 0 -> component.setBackground(Color.MAGENTA);
                case 1 -> component.setBackground(Color.ORANGE);
                case 2 -> component.setBackground(Color.GREEN);
                case 3 -> component.setBackground(Color.RED);
                case 4 -> component.setBackground(Color.YELLOW);
                case 5 -> component.setBackground(Color.BLUE);
                case 6 -> component.setBackground(Color.BLACK);
            }
            return component;
        }
    }



}
