package InterfaceLink;

import Events.ChangeDirectionEvent;
import Logic.Direction;

public interface ChangeDirectionListner {
    void setDirection(ChangeDirectionEvent changeDirectionEvent);
}
