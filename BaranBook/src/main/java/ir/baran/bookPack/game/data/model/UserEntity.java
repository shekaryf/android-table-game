package ir.baran.bookPack.game.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "User")
/**
 * موجودیت کاربر محلی (تنها یک رکورد با id=1).
 */
public class UserEntity {

    @PrimaryKey
    @ColumnInfo(name = "id")
    private Integer id;

    @ColumnInfo(name = "username", defaultValue = "'بازیکن'")
    private String username;

    @ColumnInfo(name = "coins", defaultValue = "100")
    private Integer coins;

    @ColumnInfo(name = "total_score", defaultValue = "0")
    private Integer totalScore;

    @ColumnInfo(name = "current_category_id", defaultValue = "1")
    private Integer currentCategoryId;

    @ColumnInfo(name = "current_level_id", defaultValue = "1")
    private Integer currentLevelId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getCoins() {
        return coins;
    }

    public void setCoins(Integer coins) {
        this.coins = coins;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public Integer getCurrentCategoryId() {
        return currentCategoryId;
    }

    public void setCurrentCategoryId(Integer currentCategoryId) {
        this.currentCategoryId = currentCategoryId;
    }

    public Integer getCurrentLevelId() {
        return currentLevelId;
    }

    public void setCurrentLevelId(Integer currentLevelId) {
        this.currentLevelId = currentLevelId;
    }
}
