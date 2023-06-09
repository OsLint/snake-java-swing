package Visual;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import Events.GameStateEvent;
import Events.RefreshEvent;
import InterfaceLink.BoardLink;
import InterfaceLink.GameStateListner;
import InterfaceLink.RefreshListner;
import Logic.GameState;

public class BoardVisual extends JPanel implements RefreshListner, GameStateListner {
    private final JPanel playerScore;
    private JDialog gameOverDialog;
    private final BoardLink boardLink;
    private final DefaultTableModel model;
    private final JButton stopButton;
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
    private final ArrayList<GameStateListner> gameStateListners = new ArrayList<>();

    public BoardVisual(BoardLink boardLink) {
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        String northSnakeHeadImagePath = "snakeHeadNorth.png";
        String westSnakeHeadImagePath = "snakeHeadWest.png";
        String eastSnakeHeadImagePath = "snakeHeadEast.png";
        String southSnakeHeadImagePath = "snakeHeadSouth.png";
        String snakeBodyImagePath ="snakeBody.png";
        String grassImagePath = "grass20x20.png";
        String appleImagePath = "apple20x20.png";
        String goldAppleImagePath ="goldapple20x20.png";
        String blackAppleImagePath= "blackapple20x20.png";
        String scissorsImagePath = "scissors20x20.png";
        northSnakeHeadIcon = new ImageIcon(northSnakeHeadImagePath);
        southSnakeHeadIcon = new ImageIcon(southSnakeHeadImagePath);
        westSnakeHeadIcon = new ImageIcon(westSnakeHeadImagePath);
        eastSnakeHeadIcon = new ImageIcon(eastSnakeHeadImagePath);
        snakeBodyIcon = new ImageIcon(snakeBodyImagePath);
        grassIcon = new ImageIcon(grassImagePath);
        appleIcon = new ImageIcon(appleImagePath);
        goldAppleIcon = new ImageIcon(goldAppleImagePath);
        blackAppleIcon = new ImageIcon(blackAppleImagePath);
        scissorsIcon = new ImageIcon(scissorsImagePath);
        this.boardLink = boardLink;
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
        stopButton = new JButton("Pause the Game");
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

        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                fireGameState(new GameStateEvent(this, GameState.PAUSED));
                boolean gamePaused = boardLink.getIspauseGame();
                if (gamePaused) {
                    int option = JOptionPane.showOptionDialog(
                            null,
                            "Score: " + boardLink.getPLayerScore(),
                            "Game Paused",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.INFORMATION_MESSAGE,
                            null,
                            new Object[]{"Resume"},
                            "Resume"
                    );

                    if (option == 0) {
                        fireGameState(new GameStateEvent(this, GameState.UNPAUSED));
                    }
                }
            }
        });

        playerScore.setBackground(Color.BLACK);
        setBackground(Color.BLACK);
        add(table,BorderLayout.CENTER);
        add(playerScore,BorderLayout.SOUTH);
        add(stopButton,BorderLayout.SOUTH);
        boardLink.initializeGameBoard();
        repaintTable();
    }

    @Override
    public void refresh(RefreshEvent evt) {
        playerScore.repaint();
        repaintTable();
    }

    private class CustomCellRenderer extends DefaultTableCellRenderer {
        int colorValue;
        @Override
        public void setValue(Object value) {
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
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
                case 4 -> g.drawImage(goldAppleIcon.getImage(),0,0,getWidth(),getHeight(),null);
                case 5 -> g.drawImage(scissorsIcon.getImage(),0,0,getWidth(),getHeight(),null);
                case 6 -> g.drawImage(blackAppleIcon.getImage(),0,0,getWidth(),getHeight(),null);
            }
        }
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
        ) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            colorValue = boardLink.getCellValue(row,column);
            switch (colorValue){
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

    @Override
    public void changeGameState(GameStateEvent gameStateEvent) {

        if(gameStateEvent.getGameState() == GameState.GAMEOVER){
            int score = boardLink.getPLayerScore();
            if (gameOverDialog == null) {
                gameOverDialog = new JDialog(
                        SwingUtilities.getWindowAncestor(this),
                        "Game Over",
                        Dialog.ModalityType.APPLICATION_MODAL
                );
                gameOverDialog.setLayout(new BorderLayout());
                gameOverDialog.setSize(200, 100);

                JLabel scoreLabel = new JLabel("Score: " + score);
                scoreLabel.setHorizontalAlignment(JLabel.CENTER);

                JButton newGameButton = new JButton("New Game");
                newGameButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        fireGameState(new GameStateEvent(this,GameState.NEWGAME));
                        System.out.println("start new game");
                        gameOverDialog.dispose();
                    }
                });

                gameOverDialog.add(scoreLabel, BorderLayout.CENTER);
                gameOverDialog.add(newGameButton, BorderLayout.SOUTH);
            }

            gameOverDialog.setVisible(true);
        }

    }


    private void repaintTable() {
        for (int row = 0; row < boardLink.getRows(); row++) {
            for (int col = 0; col < boardLink.getCols(); col++) {
                int segmentValue = boardLink.getCellValue(row,col);
                if (segmentValue == 2 && !boardLink.isRecentSegment(row, col)) {
                    segmentValue = 0;
                }
                model.setValueAt(segmentValue, row, col);
            }
        }
        revalidate();
        repaint();
    }

    private void fireGameState(GameStateEvent gameStateEvent){
        for (GameStateListner listener : gameStateListners) {
            listener.changeGameState(gameStateEvent);
        }
    }
    public void addGameStateListner(GameStateListner listner){
        this.gameStateListners.add(listner);
    }

    @Override
    public void addKeyListener(KeyListener keyListener) {
        super.addKeyListener(keyListener);
    }
}
