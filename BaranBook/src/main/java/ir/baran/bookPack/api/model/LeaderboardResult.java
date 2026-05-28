package ir.baran.bookPack.api.model;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardResult {
    public final List<LeaderboardEntry> topEntries = new ArrayList<>();
    public LeaderboardEntry selfEntry;
}

