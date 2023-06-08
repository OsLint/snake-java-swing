package Events;

import Logic.Direction;

import java.util.EventObject;

public class ChangeDirectionEvent extends EventObject {
    private Direction direction;
    public ChangeDirectionEvent(Object source, Direction direction) {
        super(source);
        this.direction = direction;
    }

    public Direction getDirection() {
        return direction;
    }
}
