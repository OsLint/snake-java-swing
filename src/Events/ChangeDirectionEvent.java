package Events;

import InterfaceLink.ChangeDirection;
import Logic.Direction;

import java.util.EventObject;

public class ChangeDirectionEvent extends EventObject {

    Direction direction;

    public ChangeDirectionEvent(Object source,Direction changeDirection) {
        super(source);
        this.direction = changeDirection;
    }

    public Direction getChangeDirection() {
        return direction;
    }
}
