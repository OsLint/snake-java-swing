package Events;

import java.util.EventObject;

public class NameOfEvent extends EventObject {
    /**
     * Constructs a prototypical Event.
     *
     * @param source the object on which the Event initially occurred
     * @throws IllegalArgumentException if source is null
     */
    public NameOfEvent(Object source) {
        super(source);
    }
}
