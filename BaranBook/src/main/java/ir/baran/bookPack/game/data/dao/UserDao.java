package ir.baran.bookPack.game.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import ir.baran.bookPack.game.data.model.UserEntity;

@Dao
public interface UserDao {

    @Query("SELECT * FROM User WHERE id = 1 LIMIT 1")
    LiveData<UserEntity> observeUser();

    @Query("SELECT * FROM User WHERE id = 1 LIMIT 1")
    UserEntity getUserSync();

    @Query("UPDATE User SET coins = coins + :coinReward, current_level_id = :nextLevelId WHERE id = 1")
    void rewardAndMoveToNextLevel(int coinReward, int nextLevelId);
}
