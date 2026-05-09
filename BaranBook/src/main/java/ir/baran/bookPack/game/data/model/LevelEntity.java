package ir.baran.bookPack.game.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "Levels",
        foreignKeys = @ForeignKey(
                entity = CategoryEntity.class,
                parentColumns = "id",
                childColumns = "category_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index(value = "category_id")}
)
public class LevelEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "category_id")
    private int categoryId;

    @ColumnInfo(name = "level_number")
    private int levelNumber;

    @ColumnInfo(name = "grid_rows")
    private int gridRows;

    @ColumnInfo(name = "grid_cols")
    private int gridCols;

    // Kept as JSON string, parsed by ViewModel for runtime grid state.
    @ColumnInfo(name = "grid_data")
    private String gridData;

    @ColumnInfo(name = "clues_data")
    private String cluesData;

    @ColumnInfo(name = "is_completed")
    private int isCompleted;

    @ColumnInfo(name = "stars")
    private int stars;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
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

    public int getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(int isCompleted) {
        this.isCompleted = isCompleted;
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }
}
