package ir.baran.bookPack.game.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ir.baran.bookPack.game.data.dao.LevelDao;
import ir.baran.bookPack.game.data.dao.UserDao;
import ir.baran.bookPack.game.data.local.GameDatabase;
import ir.baran.bookPack.game.data.model.LevelEntity;
import ir.baran.bookPack.game.data.model.UserEntity;
import ir.baran.bookPack.game.data.validation.GridDataValidator;

/**
 * لایه Repository: اتصال ViewModel به DAO و عملیات پس‌زمینه.
 */
public class GameRepository {

    private static final String TAG = "GameRepository";
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
                // عملیات پس از برد: تکمیل مرحله + پاداش سکه + رفتن به مرحله بعد
                levelDao.markLevelCompleted(completedLevelId);
                userDao.rewardAndMoveToNextLevel(WIN_REWARD_COINS, completedLevelId + 1);
            }
        });
    }

    public interface ValidationCallback {
        void onValidationDone(List<String> errors);
    }

    public void validateAllLevelsAsync(final ValidationCallback callback) {
        ioExecutor.execute(new Runnable() {
            @Override
            public void run() {
                // اعتبارسنجی همه مراحل در Thread پس‌زمینه
                List<LevelEntity> levels = levelDao.getAllLevelsSync();
                List<String> allErrors = new ArrayList<>();
                if (levels == null || levels.isEmpty()) {
                    allErrors.add("No levels found in Levels table.");
                } else {
                    for (LevelEntity level : levels) {
                        allErrors.addAll(GridDataValidator.validateLevel(level));
                    }
                }

                if (allErrors.isEmpty()) {
                    Log.i(TAG, "Grid validation passed for all levels.");
                } else {
                    for (String err : allErrors) {
                        Log.e(TAG, "Grid validation error: " + err);
                    }
                }

                if (callback != null) {
                    callback.onValidationDone(allErrors);
                }
            }
        });
    }
}
