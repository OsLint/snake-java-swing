package Logic;

public record PlayerScore(String playerName, int playerScore) implements Comparable<PlayerScore> {
    @Override
    public int compareTo(PlayerScore o) {
        return Integer.compare(this.playerScore, o.playerScore());
    }

    @Override
    public String playerName() {
        return playerName;
    }

    @Override
    public int playerScore() {
        return playerScore;
    }
}
