package Logic;

import Events.ChangeDirectionEvent;
import Events.GameStateEvent;
import InterfaceLink.BoardLink;
import InterfaceLink.ChangeDirectionListner;
import InterfaceLink.GameStateListner;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

public class PlayerInput implements KeyListener, Runnable, GameStateListner {
    private final BoardLink boardLink;
    private Direction currentDirection = Direction.UP;
    private final ArrayList<ChangeDirectionListner> directionListners = new ArrayList<>();
    private boolean isGamePaused;
    private Thread thread;


    public PlayerInput(BoardLink boardLink) {
        this.boardLink = boardLink;
        thread = new Thread(this);
        thread.start();
    }
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        currentDirection = boardLink.getCurrentDirection();
        if (keyCode == KeyEvent.VK_UP && currentDirection != Direction.DOWN) {
            currentDirection = Direction.UP;
            System.out.println("zmiana kierunku góra");
        } else if (keyCode == KeyEvent.VK_DOWN && currentDirection != Direction.UP) {
            currentDirection = Direction.DOWN;
            System.out.println("zmiana kierunku dol");
        } else if (keyCode == KeyEvent.VK_LEFT && currentDirection != Direction.RIGHT) {
            currentDirection = Direction.LEFT;
            System.out.println("zmiana kierunku lewy");
        } else if (keyCode == KeyEvent.VK_RIGHT && currentDirection != Direction.LEFT) {
            currentDirection = Direction.RIGHT;
            System.out.println("zmiana kierunku prawy");
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
    }public void addChangeDirectionListner (ChangeDirectionListner listner) {
        this.directionListners.add(listner);
    }

    @Override
    public void changeGameState(GameStateEvent gameStateEvent) {
        GameState gameState = gameStateEvent.getGameState();
        if(gameState == GameState.PAUSED){
            isGamePaused = true;
            System.out.println("Player input dostaje info by się zatrzymać");
        }else if(gameState == GameState.UNPAUSED){
            System.out.println("Player input dostaje info by wznowić");
            isGamePaused = false;
            synchronized (this) {
                notify();
            }
        }
    }
}
