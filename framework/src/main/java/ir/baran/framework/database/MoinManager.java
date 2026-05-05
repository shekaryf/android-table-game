package ir.baran.framework.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import ir.baran.framework.model.MoinItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager class for CRUD operations on moin table
 * Handles search operations with proper null safety and error handling
 */
public class MoinManager {

    private static final String TABLE_NAME = "moin";
    private static final int DEFAULT_LIMIT = 100;
    
    private InfoDatabaseHelper dbHelper;
    private Context context;

    public MoinManager(Context context) {
        this.context = context;
        this.dbHelper = new InfoDatabaseHelper(context);
    }

    /**
     * Search in title field with case-insensitive matching
     * @param query Search query (will be trimmed)
     * @return List of matching items (max 100)
     */
    public List<MoinItem> searchByTitle(String query) {
        return searchByTitle(query, DEFAULT_LIMIT);
    }

    /**
     * Search in title field with custom limit
     * @param query Search query
     * @param limit Maximum number of results
     * @return List of matching items
     */
    public List<MoinItem> searchByTitle(String query, int limit) {
        if (query == null) {
            query = "";
        }
        query = query.trim();
        
        if (query.isEmpty()) {
            return new ArrayList<>();
        }

        List<MoinItem> results = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getDatabase();
            
            // Use LIKE with COLLATE NOCASE for case-insensitive search
            String selection = "title LIKE ? COLLATE NOCASE";
            String[] selectionArgs = new String[]{"%" + query + "%"};
            
            cursor = db.query(
                TABLE_NAME,
                new String[]{"id", "title", "des"},
                selection,
                selectionArgs,
                null,
                null,
                "title ASC",
                String.valueOf(limit)
            );

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(0);
                    String title = cursor.getString(1);
                    String des = cursor.getString(2);
                    
                    MoinItem item = new MoinItem(id, title, des);
                    results.add(item);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            // Don't close db here, let helper manage it
        }

        return results;
    }

    /**
     * Search in description (des) field with case-insensitive matching
     * @param query Search query (will be trimmed)
     * @return List of matching items (max 100)
     */
    public List<MoinItem> searchByDescription(String query) {
        return searchByDescription(query, DEFAULT_LIMIT);
    }

    /**
     * Search in description field with custom limit
     * @param query Search query
     * @param limit Maximum number of results
     * @return List of matching items
     */
    public List<MoinItem> searchByDescription(String query, int limit) {
        if (query == null) {
            query = "";
        }
        query = query.trim();
        
        if (query.isEmpty()) {
            return new ArrayList<>();
        }

        List<MoinItem> results = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getDatabase();
            
            // Use LIKE with COLLATE NOCASE for case-insensitive search
            String selection = "des LIKE ? COLLATE NOCASE";
            String[] selectionArgs = new String[]{"%" + query + "%"};
            
            cursor = db.query(
                TABLE_NAME,
                new String[]{"id", "title", "des"},
                selection,
                selectionArgs,
                null,
                null,
                "id ASC",
                String.valueOf(limit)
            );

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(0);
                    String title = cursor.getString(1);
                    String des = cursor.getString(2);
                    
                    MoinItem item = new MoinItem(id, title, des);
                    results.add(item);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return results;
    }

    /**
     * Get item by ID
     * @param id Item ID
     * @return MoinItem or null if not found
     */
    public MoinItem getById(int id) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        MoinItem item = null;

        try {
            db = dbHelper.getDatabase();
            
            cursor = db.query(
                TABLE_NAME,
                new String[]{"id", "title", "des"},
                "id = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
            );

            if (cursor != null && cursor.moveToFirst()) {
                int itemId = cursor.getInt(0);
                String title = cursor.getString(1);
                String des = cursor.getString(2);
                
                item = new MoinItem(itemId, title, des);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return item;
    }

    /**
     * Close database helper
     */
    public void close() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
