package ir.baran.bookPack.game.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import ir.baran.bookPack.game.data.model.LevelEntity;

@Dao
public interface LevelDao {

    @Query("SELECT * FROM Levels WHERE id = :levelId LIMIT 1")
    LiveData<LevelEntity> observeLevelById(int levelId);

    @Query("SELECT * FROM Levels WHERE id = :levelId LIMIT 1")
    LevelEntity getLevelByIdSync(int levelId);

    @Query("UPDATE Levels SET is_completed = 1 WHERE id = :levelId")
    void markLevelCompleted(int levelId);
}
