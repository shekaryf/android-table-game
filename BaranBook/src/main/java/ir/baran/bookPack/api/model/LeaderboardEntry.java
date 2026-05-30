package ir.baran.bookPack.api.model;

public class LeaderboardEntry {
    public final String id;
    public final String name;
    public final int level;
    public final int score;
    public final int rank;

    public LeaderboardEntry(String id, String name, int level, int score, int rank) {
        this.id = id;
        this.name = name;
        this.level = level;
        this.score = score;
        this.rank = rank;
    }
}
