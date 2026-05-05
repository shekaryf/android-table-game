package ir.baran.framework.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Helper class for managing info.sqlite database
 * This database contains dictionary/reference data in 'moin' table
 */
public class InfoDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "info.sqlite";
    private static final int DB_VERSION = 1;
    
    private String dbPath;
    private final Context context;
    private SQLiteDatabase database;

    public InfoDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
        this.dbPath = "/data/data/" + context.getPackageName() + "/databases/";
    }

    /**
     * Creates database if it doesn't exist by copying from assets
     */
    public void createDatabase() throws IOException {
        boolean dbExist = checkDatabase();

        if (!dbExist) {
            // Create empty database
            this.getReadableDatabase();
            try {
                copyDatabase();
            } catch (IOException e) {
                throw new Error("Error copying info.sqlite database: " + e.getMessage());
            }
        }
    }

    /**
     * Check if database already exists
     */
    private boolean checkDatabase() {
        SQLiteDatabase checkDB = null;
        try {
            String fullPath = dbPath + DB_NAME;
            File dbFile = new File(fullPath);
            if (dbFile.exists()) {
                checkDB = SQLiteDatabase.openDatabase(fullPath, null, SQLiteDatabase.OPEN_READONLY);
            }
        } catch (SQLiteException e) {
            // Database doesn't exist
        }

        if (checkDB != null) {
            checkDB.close();
        }

        return checkDB != null;
    }

    /**
     * Copy database from assets to internal storage
     */
    private void copyDatabase() throws IOException {
        InputStream input = null;
        OutputStream output = null;

        try {
            // Open asset database
            input = context.getAssets().open(DB_NAME);

            // Create directory if needed
            File dir = new File(dbPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Output path
            String outFileName = dbPath + DB_NAME;
            output = new FileOutputStream(outFileName);

            // Transfer bytes
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
                    // Ignore
                }
            }
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    /**
     * Open database for reading
     */
    public void openDatabase() throws SQLException {
        String fullPath = dbPath + DB_NAME;
        database = SQLiteDatabase.openDatabase(fullPath, null, SQLiteDatabase.OPEN_READONLY);
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

    /**
     * Get readable database instance
     */
    public SQLiteDatabase getDatabase() {
        if (database == null || !database.isOpen()) {
            try {
                openDatabase();
            } catch (SQLException e) {
                // Try to create if doesn't exist
                try {
                    createDatabase();
                    openDatabase();
                } catch (IOException ioException) {
                    throw new Error("Unable to create info.sqlite database");
                }
            }
        }
        return database;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Database is copied from assets, no need to create tables
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // No upgrade logic needed for now
    }
}
