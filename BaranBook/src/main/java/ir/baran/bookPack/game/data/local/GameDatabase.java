package ir.baran.bookPack.game.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import ir.baran.bookPack.game.data.dao.LevelDao;
import ir.baran.bookPack.game.data.dao.UserDao;
import ir.baran.bookPack.game.data.model.AchievementEntity;
import ir.baran.bookPack.game.data.model.CategoryEntity;
import ir.baran.bookPack.game.data.model.DailyPuzzleEntity;
import ir.baran.bookPack.game.data.model.LevelEntity;
import ir.baran.bookPack.game.data.model.TransactionEntity;
import ir.baran.bookPack.game.data.model.UserEntity;

@Database(
        entities = {
                CategoryEntity.class,
                LevelEntity.class,
                UserEntity.class,
                DailyPuzzleEntity.class,
                AchievementEntity.class,
                TransactionEntity.class
        },
        version = 1,
        exportSchema = false
)
public abstract class GameDatabase extends RoomDatabase {

    private static final String DB_NAME = "dt";
    private static volatile GameDatabase instance;

    public abstract LevelDao levelDao();
    public abstract UserDao userDao();

    public static GameDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (GameDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    GameDatabase.class,
                                    DB_NAME
                            )
                            // Uses existing asset DB copied from app/src/main/assets/dt
                            .createFromAsset(DB_NAME)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}
