package Logic;

import Events.ChangeDirectionEvent;
import InterfaceLink.ChangeDirection;
import InterfaceLink.RefreshListner;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

public class PlayerInput implements KeyListener {

    private Direction direction;
    private final ArrayList<ChangeDirection> listners = new ArrayList<>();

    public Direction getDirection() {
        return direction;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_UP) {
            direction = Direction.UP;
            fireRefresh(new ChangeDirectionEvent(this,direction));
            System.out.println("Up arrow key pressed");
        } else if (keyCode == KeyEvent.VK_DOWN) {
            direction = Direction.DOWN;
            fireRefresh(new ChangeDirectionEvent(this,direction));
            System.out.println("Down arrow key pressed");
        } else if (keyCode == KeyEvent.VK_LEFT) {
            direction = Direction.LEFT;
            fireRefresh(new ChangeDirectionEvent(this,direction));
            System.out.println("Left arrow key pressed");
        } else if (keyCode == KeyEvent.VK_RIGHT) {
            direction = Direction.RIGHT;
            fireRefresh(new ChangeDirectionEvent(this,direction));
            System.out.println("Right arrow key pressed");
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    private void fireRefresh(ChangeDirectionEvent changeDirectionEvent) {
        for (ChangeDirection listener : listners) {
            listener.changeDirection(changeDirectionEvent);
        }
    }

    public void addChangeDirectionListner(ChangeDirection listner) {
        this.listners.add(listner);
    }
}
