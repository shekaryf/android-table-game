package ir.baran.bookPack.game.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import ir.baran.bookPack.game.data.model.LevelEntity;

@Dao
/**
 * دسترسی به داده‌های جدول مراحل (Levels).
 */
public interface LevelDao {

    @Query("SELECT * FROM Levels WHERE level_number = :levelId LIMIT 1")
    LiveData<LevelEntity> observeLevelById(int levelId);

    @Query("SELECT * FROM Levels WHERE level_number = :levelId LIMIT 1")
    LevelEntity getLevelByIdSync(int levelId);

    @Query("UPDATE Levels SET is_completed = 1 WHERE id = :levelId")
    void markLevelCompleted(int levelId);

    @Query("SELECT * FROM Levels ORDER BY level_number ASC")
    List<LevelEntity> getAllLevelsSync();

    @Query("SELECT COUNT(*) FROM Levels WHERE level_number = :levelId")
    int countById(int levelId);

    @Query("SELECT is_completed FROM Levels WHERE level_number = :levelId LIMIT 1")
    Integer getIsCompletedById(int levelId);

    @Query("SELECT COUNT(*) FROM Levels")
    int getLevelsCount();
}
