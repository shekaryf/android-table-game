package ir.baran.bookPack.game.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "Achievements")
/**
 * موجودیت دستاوردها مطابق جدول Achievements.
 */
public class AchievementEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private Integer id;

    @ColumnInfo(name = "title")
    @NonNull
    private String title;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "target_value")
    private int targetValue;

    @ColumnInfo(name = "current_value", defaultValue = "0")
    private Integer currentValue;

    @ColumnInfo(name = "reward_coins")
    private int rewardCoins;

    @ColumnInfo(name = "is_claimed", defaultValue = "0")
    private Integer isClaimed;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getTargetValue() { return targetValue; }
    public void setTargetValue(int targetValue) { this.targetValue = targetValue; }
    public Integer getCurrentValue() { return currentValue; }
    public void setCurrentValue(Integer currentValue) { this.currentValue = currentValue; }
    public int getRewardCoins() { return rewardCoins; }
    public void setRewardCoins(int rewardCoins) { this.rewardCoins = rewardCoins; }
    public Integer getIsClaimed() { return isClaimed; }
    public void setIsClaimed(Integer isClaimed) { this.isClaimed = isClaimed; }
}
