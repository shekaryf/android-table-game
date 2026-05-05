package ir.baran.bookPack.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Manager class for performing search operations on Moin dictionary
 * Handles all database queries with proper resource management
 */
public class MoinManager {
    private static final String TAG = "MoinManager";
    private static final String TABLE_NAME = "moin";
    private static final int SEARCH_LIMIT = 100;
    
    private final InfoDatabaseHelper dbHelper;
    private SQLiteDatabase database;

    public MoinManager(Context context) {
        this.dbHelper = new InfoDatabaseHelper(context);
    }

    /**
     * Open database connection
     */
    public void open() throws IOException {
        database = dbHelper.openDatabase();
    }

    /**
     * Close database connection
     */
    public void close() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    /**
     * Get top 100 items from database
     * @return List of first 100 items ordered by id
     */
    public List<MoinItem> getTopItems() {
        List<MoinItem> results = new ArrayList<>();
        Cursor cursor = null;
        
        try {
            cursor = database.query(
                TABLE_NAME,
                new String[]{"id", "title", "des"},
                null,
                null,
                null,
                null,
                "id ASC",
                String.valueOf(SEARCH_LIMIT)
            );
            
            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex("id");
                int titleIndex = cursor.getColumnIndex("title");
                int desIndex = cursor.getColumnIndex("des");
                
                do {
                    MoinItem item = new MoinItem();
                    item.setId(cursor.getInt(idIndex));
                    item.setTitle(cursor.getString(titleIndex));
                    item.setDes(cursor.getString(desIndex));
                    results.add(item);
                } while (cursor.moveToNext());
            }
            
            Log.d(TAG, "Retrieved top " + results.size() + " items");
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting top items", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return results;
    }

    /**
     * Search in title field
     * @param query Search query (will be trimmed and case-insensitive)
     * @return List of matching items (max 100)
     */
    public List<MoinItem> searchByTitle(String query) {
        if (query == null) {
            return new ArrayList<>();
        }
        
        query = query.trim();
        if (query.isEmpty()) {
            return new ArrayList<>();
        }

        List<MoinItem> results = new ArrayList<>();
        Cursor cursor = null;
        
        try {
            String selection = "title LIKE ? COLLATE NOCASE";
            String[] selectionArgs = new String[]{"%" + query + "%"};
            
            cursor = database.query(
                TABLE_NAME,
                new String[]{"id", "title", "des"},
                selection,
                selectionArgs,
                null,
                null,
                "id ASC",
                String.valueOf(SEARCH_LIMIT)
            );
            
            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex("id");
                int titleIndex = cursor.getColumnIndex("title");
                int desIndex = cursor.getColumnIndex("des");
                
                do {
                    MoinItem item = new MoinItem();
                    item.setId(cursor.getInt(idIndex));
                    item.setTitle(cursor.getString(titleIndex));
                    item.setDes(cursor.getString(desIndex));
                    results.add(item);
                } while (cursor.moveToNext());
            }
            
            Log.d(TAG, "Search by title: '" + query + "' found " + results.size() + " results");
            
        } catch (Exception e) {
            Log.e(TAG, "Error searching by title", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return results;
    }

    /**
     * Search in description field
     * @param query Search query (will be trimmed and case-insensitive)
     * @return List of matching items (max 100)
     */
    public List<MoinItem> searchByDescription(String query) {
        if (query == null) {
            return new ArrayList<>();
        }
        
        query = query.trim();
        if (query.isEmpty()) {
            return new ArrayList<>();
        }

        List<MoinItem> results = new ArrayList<>();
        Cursor cursor = null;
        
        try {
            String selection = "des LIKE ? COLLATE NOCASE";
            String[] selectionArgs = new String[]{"%" + query + "%"};
            
            cursor = database.query(
                TABLE_NAME,
                new String[]{"id", "title", "des"},
                selection,
                selectionArgs,
                null,
                null,
                "id ASC",
                String.valueOf(SEARCH_LIMIT)
            );
            
            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex("id");
                int titleIndex = cursor.getColumnIndex("title");
                int desIndex = cursor.getColumnIndex("des");
                
                do {
                    MoinItem item = new MoinItem();
                    item.setId(cursor.getInt(idIndex));
                    item.setTitle(cursor.getString(titleIndex));
                    item.setDes(cursor.getString(desIndex));
                    results.add(item);
                } while (cursor.moveToNext());
            }
            
            Log.d(TAG, "Search by description: '" + query + "' found " + results.size() + " results");
            
        } catch (Exception e) {
            Log.e(TAG, "Error searching by description", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return results;
    }

    /**
     * Check if database is open and ready
     */
    public boolean isOpen() {
        return database != null && database.isOpen();
    }
}
