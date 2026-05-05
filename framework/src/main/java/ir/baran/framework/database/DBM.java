package ir.baran.framework.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public abstract class DBM {
	protected SQLiteDatabase _DB;
	protected String _TableName;
	protected String _KeyFieldName;

	public DBM() {
	}

	protected DBM open() {
		try {
			_DB = SQLiteDatabase.openDatabase(DataBaseHelper.DB_PATH + "/" + DataBaseHelper.DB_NAME, null, SQLiteDatabase.OPEN_READWRITE
					| SQLiteDatabase.CREATE_IF_NECESSARY);
			return this;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	protected void closeDB() {
		_DB.close();
	}

	protected boolean isOpen() {
		return _DB.isOpen();
	}

	public void update(Object item) {
		open();
		ContentValues cv = new ContentValues();
		int id = putUpdates(cv, item);
		_DB.update(_TableName, cv, _KeyFieldName + "=" + id, null);
		closeDB();
	}

	protected abstract int putUpdates(ContentValues cv, Object item);

	public int insert(Object item) {
		open();
		ContentValues cv = new ContentValues();
		putUpdates(cv, item);
		_DB.insert(_TableName, null, cv);
		Cursor cr = _DB.rawQuery("select last_insert_rowid() as c", null);
		int id = 0;
		if (cr.moveToNext())
			id = cr.getInt(0);
		closeDB();
		return id;
	}

	public Cursor select(String fields, String caseSelect, String orderBy) {
		return select(fields, caseSelect, orderBy, "");
	}

	public Cursor select(String fields, String caseSelect, String orderBy, String limit) {
		open();
		String query = "SELECT " + fields + " FROM " + _TableName;
		if (caseSelect != null && caseSelect.length() > 0)
			query += " WHERE " + caseSelect;
		if (orderBy != null && orderBy.length() > 0)
			query += " ORDER BY " + orderBy;
		if (limit != null && limit.length() > 0)
			query += " limit " + limit;
		Cursor cursor = _DB.rawQuery(query, null);
		System.out.println("qqqq : " + query);
		// cursor.close();
		// closeDB();
		return cursor;
	}

	public void deleteRow(int id) {
		open();
		_DB.delete(_TableName, _KeyFieldName + "=" + id, null);
		closeDB();
	}

}
