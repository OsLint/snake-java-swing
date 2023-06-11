
import Events.GameStateEvent;
import Logic.BoardLogic;
import Enums.GameState;
import Logic.PlayerInput;
import Visual.BoardPanel;
import Logic.FileHandler;
import Visual.ScoreboardPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Klasa Main reprezentuje główną klasę programu.
 * Uruchamia grę Snake.
 */
class Main extends JFrame {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }

    private final BoardLogic boardLogic;
    private String playerName;

    /**
     * Konstruktor klasy Main.
     * Initialize interfejs użytkownika, logikę gry i panele.
     */
    public Main() {
        super("Snake");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        boardLogic = new BoardLogic();
        FileHandler fileHandler = new FileHandler(boardLogic);
        BoardPanel boardPanel = new BoardPanel(boardLogic);
        PlayerInput playerInput = new PlayerInput(boardLogic);

        fileHandler.loadPoints();

        playerName = showPlayerNameDialog();
        if (playerName == null) {
            // Gracz wybrał opcję wyjścia
            System.exit(0);
        }
        boardLogic.setPlayerName(playerName);


        boardLogic.addGameStateListner(event -> {
            if (
                    event.getGameState() == GameState.UNPAUSED
                            || event.getGameState() == GameState.NEWGAME
            ) {
                Main.this.requestFocusInWindow();
            }
        });

        ScoreboardPanel scoreboardPanel = new ScoreboardPanel(boardLogic);
        JPanel emptyPanel = new JPanel();

        int screenWidth = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();

        emptyPanel.setPreferredSize(new Dimension(screenWidth / 5, JFrame.MAXIMIZED_VERT));
        emptyPanel.setMaximumSize(new Dimension(screenWidth / 5, JFrame.MAXIMIZED_VERT));
        emptyPanel.setBackground(Color.GRAY);
        scoreboardPanel.setPreferredSize(new Dimension(screenWidth / 5, JFrame.MAXIMIZED_VERT));
        scoreboardPanel.setMaximumSize(new Dimension(screenWidth / 5, JFrame.MAXIMIZED_VERT));


        addKeyListener(playerInput);
        boardLogic.addRefreshListner(boardPanel);
        boardLogic.addRefreshListner(scoreboardPanel);
        boardLogic.addGameStateListner(playerInput);
        boardLogic.addGameStateListner(boardPanel);
        boardLogic.addGameStateListner(fileHandler);
        playerInput.addChangeDirectionListner(boardLogic);


        getContentPane().add(boardPanel, BorderLayout.CENTER);
        getContentPane().add(scoreboardPanel, BorderLayout.EAST);
        getContentPane().add(emptyPanel, BorderLayout.WEST);
    }

    /**
     * Metoda showplayernamedialog wyświetla dialog z prośbą o podanie nazwy gracza.
     * @return Nazwa gracza.
     */
    private String showPlayerNameDialog() {
        JTextField playerNameField = new JTextField();
        Object[] message = {"Podaj nazwę gracza:", playerNameField};
        int option = JOptionPane.showOptionDialog(this, message, "Wprowadź nazwę",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);

        if (option == JOptionPane.OK_OPTION) {
            playerName = playerNameField.getText().trim();

            if (!playerName.isEmpty()) {
                boardLogic.setPlayerName(playerName);
                boardLogic.fireGameState(new GameStateEvent(this, GameState.NEWGAME));
                return playerName;
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Nazwa gracza nie może być pusta.",
                        "Błąd",
                        JOptionPane.ERROR_MESSAGE
                );
                return showPlayerNameDialog();
            }
        } else {
            return null;
        }
    }
}
