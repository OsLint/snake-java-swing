package Logic;

import InterfaceLink.BoardLink;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class PlayerInput implements KeyListener {
    private final BoardLink boardLink;
    public PlayerInput(BoardLink boardLink) {
        this.boardLink = boardLink;
    }
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        Direction currentDirection = boardLink.getCurrentDirection();
        if (keyCode == KeyEvent.VK_UP && currentDirection != Direction.DOWN) {
             boardLink.setDirection(Direction.UP);
        } else if (keyCode == KeyEvent.VK_DOWN && currentDirection != Direction.UP) {
            boardLink.setDirection(Direction.DOWN);
        } else if (keyCode == KeyEvent.VK_LEFT && currentDirection != Direction.RIGHT) {
            boardLink.setDirection(Direction.LEFT);
        } else if (keyCode == KeyEvent.VK_RIGHT && currentDirection != Direction.LEFT) {
            boardLink.setDirection(Direction.RIGHT);
        }
    }
    @Override
    public void keyReleased(KeyEvent e) {
    }
    @Override
    public void keyTyped(KeyEvent e) {

    }
}
