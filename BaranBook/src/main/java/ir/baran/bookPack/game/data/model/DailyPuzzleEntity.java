package ir.baran.bookPack.game.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "DailyPuzzles")
public class DailyPuzzleEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "puzzle_date")
    private String puzzleDate;

    @ColumnInfo(name = "grid_data")
    private String gridData;

    @ColumnInfo(name = "clues_data")
    private String cluesData;

    @ColumnInfo(name = "reward_coins")
    private int rewardCoins;

    @ColumnInfo(name = "status")
    private int status;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPuzzleDate() {
        return puzzleDate;
    }

    public void setPuzzleDate(String puzzleDate) {
        this.puzzleDate = puzzleDate;
    }

    public String getGridData() {
        return gridData;
    }

    public void setGridData(String gridData) {
        this.gridData = gridData;
    }

    public String getCluesData() {
        return cluesData;
    }

    public void setCluesData(String cluesData) {
        this.cluesData = cluesData;
    }

    public int getRewardCoins() {
        return rewardCoins;
    }

    public void setRewardCoins(int rewardCoins) {
        this.rewardCoins = rewardCoins;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
