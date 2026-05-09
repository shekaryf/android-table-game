package ir.baran.bookPack.game.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ir.baran.bookPack.game.data.dao.LevelDao;
import ir.baran.bookPack.game.data.dao.UserDao;
import ir.baran.bookPack.game.data.local.GameDatabase;
import ir.baran.bookPack.game.data.model.LevelEntity;
import ir.baran.bookPack.game.data.model.UserEntity;

public class GameRepository {

    private static final int WIN_REWARD_COINS = 20;

    private final LevelDao levelDao;
    private final UserDao userDao;
    private final ExecutorService ioExecutor;

    public GameRepository(Context context) {
        GameDatabase db = GameDatabase.getInstance(context);
        this.levelDao = db.levelDao();
        this.userDao = db.userDao();
        this.ioExecutor = Executors.newSingleThreadExecutor();
    }

    public LiveData<LevelEntity> observeLevel(int levelId) {
        return levelDao.observeLevelById(levelId);
    }

    public LiveData<UserEntity> observeUser() {
        return userDao.observeUser();
    }

    public void completeLevelAndRewardUser(final int completedLevelId) {
        ioExecutor.execute(new Runnable() {
            @Override
            public void run() {
                levelDao.markLevelCompleted(completedLevelId);
                userDao.rewardAndMoveToNextLevel(WIN_REWARD_COINS, completedLevelId + 1);
            }
        });
    }
}
