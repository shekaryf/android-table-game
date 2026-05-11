package ir.baran.bookPack.game.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "DailyPuzzles")
/**
 * موجودیت چالش روزانه مطابق جدول DailyPuzzles.
 */
public class DailyPuzzleEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private Integer id;

    @ColumnInfo(name = "puzzle_date")
    @NonNull
    private String puzzleDate;

    @ColumnInfo(name = "grid_data")
    @NonNull
    private String gridData;

    @ColumnInfo(name = "clues_data")
    @NonNull
    private String cluesData;

    @ColumnInfo(name = "reward_coins", defaultValue = "50")
    private Integer rewardCoins;

    @ColumnInfo(name = "status", defaultValue = "0")
    private Integer status;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public Integer getRewardCoins() {
        return rewardCoins;
    }

    public void setRewardCoins(Integer rewardCoins) {
        this.rewardCoins = rewardCoins;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
