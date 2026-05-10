package ir.baran.bookPack.game.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(
        tableName = "Levels",
        foreignKeys = @ForeignKey(
                entity = CategoryEntity.class,
                parentColumns = "id",
                childColumns = "category_id"
        )
)
public class LevelEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private Integer id;

    @ColumnInfo(name = "category_id")
    private Integer categoryId;

    @ColumnInfo(name = "level_number")
    private int levelNumber;

    @ColumnInfo(name = "grid_rows")
    private int gridRows;

    @ColumnInfo(name = "grid_cols")
    private int gridCols;

    // Kept as JSON string, parsed by ViewModel for runtime grid state.
    @ColumnInfo(name = "grid_data")
    @NonNull
    private String gridData;

    @ColumnInfo(name = "clues_data")
    @NonNull
    private String cluesData;

    @ColumnInfo(name = "is_completed", defaultValue = "0")
    private Integer isCompleted;

    @ColumnInfo(name = "stars", defaultValue = "0")
    private Integer stars;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public void setLevelNumber(int levelNumber) {
        this.levelNumber = levelNumber;
    }

    public int getGridRows() {
        return gridRows;
    }

    public void setGridRows(int gridRows) {
        this.gridRows = gridRows;
    }

    public int getGridCols() {
        return gridCols;
    }

    public void setGridCols(int gridCols) {
        this.gridCols = gridCols;
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

    public Integer getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(Integer isCompleted) {
        this.isCompleted = isCompleted;
    }

    public Integer getStars() {
        return stars;
    }

    public void setStars(Integer stars) {
        this.stars = stars;
    }
}
