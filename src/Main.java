

import Logic.BoardLogic;
import Logic.PlayerInput;
import Visual.BoardVisual;

import javax.swing.*;

class Main extends JFrame  {

    public Main () {
        super("Snake");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
        setLocationRelativeTo(null);
        BoardLogic boardLogic = new BoardLogic();
        BoardVisual boardVisual = new BoardVisual(boardLogic);
        PlayerInput playerInput = new PlayerInput(boardLogic);
        addKeyListener(playerInput);
        boardLogic.addRefreshListner(boardVisual);
        getContentPane().add(boardVisual);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}
