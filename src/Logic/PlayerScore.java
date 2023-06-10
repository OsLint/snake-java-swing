package Logic;

public class PlayerScore implements Comparable<PlayerScore>{
    private String playerName;
    private int playerScore;

    public PlayerScore(String playerName, int playerScore) {
        this.playerName = playerName;
        this.playerScore = playerScore;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getPlayerScore() {
        return playerScore;
    }

    @Override
    public int compareTo(PlayerScore o) {
        return Integer.compare(this.playerScore,o.getPlayerScore());
    }
}
