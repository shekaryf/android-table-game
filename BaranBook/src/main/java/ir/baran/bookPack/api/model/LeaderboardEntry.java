package ir.baran.bookPack.api.model;

public class LeaderboardEntry {
    public final String name;
    public final int level;
    public final int score;
    public final int rank;

    public LeaderboardEntry(String name, int level, int score, int rank) {
        this.name = name;
        this.level = level;
        this.score = score;
        this.rank = rank;
    }
}

