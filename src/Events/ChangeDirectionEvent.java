package Events;

import Enums.Direction;

import java.util.EventObject;

public class ChangeDirectionEvent extends EventObject {
    private final Direction direction;
    public ChangeDirectionEvent(Object source, Direction direction) {
        super(source);
        this.direction = direction;
    }

    public Direction getDirection() {
        return direction;
    }
}
