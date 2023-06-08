package Logic;

import Events.ChangeDirectionEvent;
import InterfaceLink.BoardLink;
import InterfaceLink.ChangeDirectionListner;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

public class PlayerInput implements KeyListener, Runnable {
    private final BoardLink boardLink;
    private Direction currentDirection = Direction.UP;
    private final ArrayList<ChangeDirectionListner> directionListners = new ArrayList<>();

    public PlayerInput(BoardLink boardLink) {
        this.boardLink = boardLink;

        Thread thread = new Thread(this);
        thread.start();
    }
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        currentDirection = boardLink.getCurrentDirection();
        if (keyCode == KeyEvent.VK_UP && currentDirection != Direction.DOWN) {
             //boardLink.setDirection(Direction.UP);
            currentDirection = Direction.UP;
        } else if (keyCode == KeyEvent.VK_DOWN && currentDirection != Direction.UP) {
            //boardLink.setDirection(Direction.DOWN);
            currentDirection = Direction.DOWN;
        } else if (keyCode == KeyEvent.VK_LEFT && currentDirection != Direction.RIGHT) {
            //boardLink.setDirection(Direction.LEFT);
            currentDirection = Direction.LEFT;
        } else if (keyCode == KeyEvent.VK_RIGHT && currentDirection != Direction.LEFT) {
            //boardLink.setDirection(Direction.RIGHT);
            currentDirection = Direction.RIGHT;
        }
    }
    @Override
    public void keyReleased(KeyEvent e) {
    }
    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void run() {
        while (boardLink.getIsGameOngoing()){
            if(currentDirection != null){
                ChangeDirectionEvent event = new ChangeDirectionEvent(this,this.currentDirection);
                fireChangeDirection(event);
            }
        }
    }
    private void fireChangeDirection(ChangeDirectionEvent event) {
        for (ChangeDirectionListner listener : directionListners) {
            listener.setDirection(event);
        }
    }
    public void addChangeDirectionListner (ChangeDirectionListner listner) {
        this.directionListners.add(listner);
    }
}
