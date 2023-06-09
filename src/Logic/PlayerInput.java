package Logic;

import Events.ChangeDirectionEvent;
import Events.GameStateEvent;
import InterfaceLink.BoardLink;
import InterfaceLink.ChangeDirectionListner;
import InterfaceLink.GameStateListner;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

public class PlayerInput implements KeyListener, Runnable,GameStateListner {
    private final BoardLink boardLink;
    private Direction currentDirection = Direction.UP;
    private final ArrayList<ChangeDirectionListner> directionListners = new ArrayList<>();
    private boolean isGamePaused;


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
            currentDirection = Direction.UP;
        } else if (keyCode == KeyEvent.VK_DOWN && currentDirection != Direction.UP) {
            currentDirection = Direction.DOWN;
        } else if (keyCode == KeyEvent.VK_LEFT && currentDirection != Direction.RIGHT) {
            currentDirection = Direction.LEFT;
        } else if (keyCode == KeyEvent.VK_RIGHT && currentDirection != Direction.LEFT) {
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
            if (isGamePaused) {
                try {
                    synchronized (this) {
                        wait();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println("DEbug: działam player input...");
            if(currentDirection != null){
                ChangeDirectionEvent event = new ChangeDirectionEvent(this,this.currentDirection);
                fireChangeDirection(event);
            }
            try {
                synchronized (this) {
                    wait(100);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
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

    @Override
    public void changeGameState(GameStateEvent gameStateEvent) {
        if(gameStateEvent.getGameState() == GameState.PAUSED){
            isGamePaused = true;
        }else if(gameStateEvent.getGameState() == GameState.UNPAUSED){
            isGamePaused = false;
            synchronized (this) {
                notifyAll();
            }
        }
    }
}
