package ir.baran.bookPack.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Database helper for managing info.sqlite database
 * Handles copying database from assets and providing access
 */
public class InfoDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "InfoDatabaseHelper";
    private static final String DB_NAME = "info.sqlite";
    private static final int DB_VERSION = 1;
    
    private final Context context;
    private String dbPath;
    private SQLiteDatabase database;

    public InfoDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
        this.dbPath = context.getDatabasePath(DB_NAME).getPath();
    }

    /**
     * Creates database by copying from assets if it doesn't exist
     */
    public void createDatabase() throws IOException {
        boolean dbExist = checkDatabase();
        
        if (!dbExist) {
            // Create empty database to establish path
            this.getReadableDatabase();
            this.close();
            
            try {
                copyDatabase();
                Log.d(TAG, "Database copied successfully");
            } catch (IOException e) {
                Log.e(TAG, "Error copying database", e);
                throw new IOException("Error copying database from assets", e);
            }
        }
    }

    /**
     * Check if database already exists
     */
    private boolean checkDatabase() {
        File dbFile = new File(dbPath);
        return dbFile.exists();
    }

    /**
     * Copy database from assets to internal storage
     */
    private void copyDatabase() throws IOException {
        InputStream input = null;
        OutputStream output = null;
        
        try {
            input = context.getAssets().open(DB_NAME);
            
            // Ensure parent directory exists
            File dbFile = new File(dbPath);
            File parentDir = dbFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            output = new FileOutputStream(dbPath);
            
            byte[] buffer = new byte[1024];
            int length;
            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            
            output.flush();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing output stream", e);
                }
            }
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing input stream", e);
                }
            }
        }
    }

    /**
     * Open database for reading
     */
    public SQLiteDatabase openDatabase() throws IOException {
        if (database == null || !database.isOpen()) {
            createDatabase();
            database = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);
        }
        return database;
    }

    /**
     * Close database connection
     */
    @Override
    public synchronized void close() {
        if (database != null && database.isOpen()) {
            database.close();
        }
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Database is copied from assets, no need to create tables
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrades if needed
    }
}
