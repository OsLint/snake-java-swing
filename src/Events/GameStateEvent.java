package Events;

import Logic.GameState;

import java.util.EventObject;

public class GameStateEvent extends EventObject {
    private final GameState gameState;



    public GameStateEvent(Object source,GameState gameState) {
        super(source);
        this.gameState = gameState;
    }


    public GameState getGameState() {
        return gameState;
    }
}
