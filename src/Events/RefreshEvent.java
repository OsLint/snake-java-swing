package Events;

import Logic.BoardLogic;
import Logic.Direction;

import java.util.EventObject;

public class RefreshEvent extends EventObject {
    private final BoardLogic boardLogic;
    public RefreshEvent(Object source, BoardLogic boardLogic) {
        super(source);
        this.boardLogic = boardLogic;
    }

    public BoardLogic getBoardLogic() {
        return boardLogic;
    }
}
