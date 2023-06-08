package Events;

import java.util.EventObject;

public class GameOverEvent extends EventObject {
    public GameOverEvent(Object source) {
        super(source);
    }
}
