package Visual;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import Events.GameStateEvent;
import Events.RefreshEvent;
import InterfaceLink.BoardLink;
import InterfaceLink.GameStateListner;
import InterfaceLink.RefreshListner;
import Enums.GameState;

/**

 Klasa BoardPanel reprezentuje panel planszy gry.
 Panel ten dziedziczy po klasie JPanel i implementuje interfejsy RefreshListner oraz GameStateListner.
 Służy do wyświetlania planszy gry oraz aktualizacji jej stanu.
 */

public class BoardPanel extends JPanel implements RefreshListner, GameStateListner {
    private JDialog gameOverDialog;
    private final BoardLink boardLink;
    private final DefaultTableModel model;
    private int playerScoreValue;
    private final JLabel scoreLabel;
    private final PlayerScorePanel playerScore;
    /**
     Konstruktor klasy BoardPanel.
     @param boardLink Obiekt typu BoardLink, który umożliwia połączenie z logiką gry.
     */
    public BoardPanel(BoardLink boardLink) {
        //Inicjalizacja komponentów
        this.boardLink = boardLink;
        playerScoreValue = boardLink.getPLayerScore();
        scoreLabel = new JLabel("Score: " + playerScoreValue);
        model = new DefaultTableModel(boardLink.getRows(), boardLink.getCols());
        playerScore = new PlayerScorePanel(boardLink);
        JButton stopButton = new JButton("Pause the Game");
        BoardVisual boardVisual = new BoardVisual(boardLink,model);
        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                boardLink.fireGameState(new GameStateEvent(this, GameState.PAUSED));
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
                        boardLink.fireGameState(new GameStateEvent(this, GameState.UNPAUSED));
                    }
                }
            }
        });
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        setBackground(Color.GRAY);
        add(Box.createVerticalGlue());
        add(playerScore);
        add(boardVisual);
        add(stopButton);
        repaintTable();
        add(Box.createVerticalGlue());
    }
    /**
     Metoda changeGameState obsługuje zmianę stanu gry.
     Wywoływana, gdy następuje zdarzenie GameStateEvent.
     Jeśli stan gry jest GAMEOVER, tworzy dialog z informacją o końcu gry oraz opcją rozpoczęcia nowej gry.
     @param gameStateEvent Obiekt GameStateEvent reprezentujący zdarzenie zmiany stanu gry.
     */
    @Override
    public void changeGameState(GameStateEvent gameStateEvent) {

        if (gameStateEvent.getGameState() == GameState.GAMEOVER) {
            //int score = boardLink.getPLayerScore();
            if (gameOverDialog == null) {
                gameOverDialog = new JDialog(
                        SwingUtilities.getWindowAncestor(this),
                        "Game Over",
                        Dialog.ModalityType.APPLICATION_MODAL
                );
                gameOverDialog.setLayout(new BorderLayout());
                gameOverDialog.setSize(200, 120);

                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                int dialogWidth = gameOverDialog.getSize().width;
                int dialogHeight = gameOverDialog.getSize().height;
                int dialogX = (screenSize.width - dialogWidth) / 2;
                int dialogY = (screenSize.height - dialogHeight) / 2;
                gameOverDialog.setLocation(dialogX, dialogY);
                scoreLabel.setHorizontalAlignment(JLabel.CENTER);

                JPanel namePanel = new JPanel(new BorderLayout());
                JLabel nameLabel = new JLabel("Your Name:");
                namePanel.add(nameLabel, BorderLayout.NORTH);

                JTextField nameField = new JTextField();
                nameField.setText(boardLink.getPlayerName());
                namePanel.add(nameField, BorderLayout.CENTER);

                JButton newGameButton = new JButton("New Game");
                newGameButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String newName = nameField.getText();
                        boardLink.setPlayerName(newName);
                        boardLink.fireGameState(new GameStateEvent(this, GameState.NEWGAME));
                        System.out.println("start new game");
                        gameOverDialog.dispose();
                    }
                });
                gameOverDialog.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                });

                gameOverDialog.add(scoreLabel, BorderLayout.NORTH);
                gameOverDialog.add(newGameButton, BorderLayout.SOUTH);
                gameOverDialog.add(namePanel,BorderLayout.CENTER);
            }

            gameOverDialog.setVisible(true);
        }

    }
    /**
     Metoda repaintTable odświeża tabelę planszy gry.
     Aktualizuje wartości komórek tabeli na podstawie stanu planszy z obiektu boardLink.
     Wywoływana po zmianie stanu planszy.
     */
    private void repaintTable() {
        for (int row = 0; row < boardLink.getRows(); row++) {
            for (int col = 0; col < boardLink.getCols(); col++) {
                int segmentValue = boardLink.getCellValue(row, col);
                if (segmentValue == 2 && !boardLink.recentSegment(row, col)) {
                    segmentValue = 0;
                }
                model.setValueAt(segmentValue, row, col);
            }
        }
        revalidate();
        repaint();
    }
    /**
     Metoda refresh odświeża panel z wynikiem gracza.
     Aktualizuje wyświetlane wartości punktacji oraz odświeża tabelę planszy.
     Wywoływana po zdarzeniu RefreshEvent.
     @param evt Obiekt RefreshEvent reprezentujący zdarzenie odświeżenia.
     */
    @Override
    public void refresh(RefreshEvent evt) {
        playerScore.repaint();
        playerScoreValue = boardLink.getPLayerScore();
        scoreLabel.setText("Score: " + playerScoreValue);
        repaintTable();
    }
}
