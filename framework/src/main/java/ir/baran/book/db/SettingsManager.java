package ir.baran.book.db;

import ir.baran.framework.database.DBM;
import ir.baran.framework.utilities.Functions;
import android.content.ContentValues;
import android.database.Cursor;

public class SettingsManager extends DBM {
	public int TEXT_FONT_SIZE;
	public int READ_CONTINUE_ID;
	public int READ_SCROULLY;
	protected static SettingsManager instance;

	// CREATE TABLE "settings" ("id" TEXT PRIMARY KEY NOT NULL UNIQUE , "value"
	// TEXT)
	public static SettingsManager getInstance() {
		if (instance == null) {
			instance = new SettingsManager();
		}
		return instance;
	}

	public SettingsManager() {
		_KeyFieldName = "id";
		_TableName = "settings";
		Cursor cr = select("id,value", "", "");
		addItems(cr);
	}

	private void addItems(Cursor cr) {
		while (cr.moveToNext()) {
			String id = cr.getString(0);
			String value = cr.getString(1);
			if (id.compareTo("TEXT_FONT_SIZE") == 0)
				TEXT_FONT_SIZE = Functions.toInt32(value);
			else if (id.compareTo("READ_CONTINUE_ID") == 0)
				READ_CONTINUE_ID = Functions.toInt32(value);
			else if (id.compareTo("READ_SCROULLY") == 0)
				READ_SCROULLY = Functions.toInt32(value);
			else
				setOtherIdValues(id, value);
		}
		cr.close();
		closeDB();
	}

	protected void setOtherIdValues(String id, String value) {
		// TODO Auto-generated method stub

	}

	public void update(Object item) {
		SettingsManager setting = (SettingsManager) item;
		open();
		ContentValues cv = new ContentValues();
		cv.put("value", setting.TEXT_FONT_SIZE);
		_DB.update(_TableName, cv, _KeyFieldName + "='TEXT_FONT_SIZE'", null);
		closeDB();
	}

	public void updateReadContinue() {
		open();
		ContentValues cv = new ContentValues();
		cv.put("value", READ_CONTINUE_ID);
		_DB.update(_TableName, cv, _KeyFieldName + "='READ_CONTINUE_ID'", null);

		ContentValues cv2 = new ContentValues();
		cv2.put("value", READ_SCROULLY);
		_DB.update(_TableName, cv2, _KeyFieldName + "='READ_SCROULLY'", null);
		closeDB();
	}

	@Override
	protected int putUpdates(ContentValues cv, Object item) {
		return 0;// textFont
	}

	public int getIntegetValue(String selectid) {
		Cursor cr = select("id,value", "id='" + selectid + "'", "");
		if (cr.moveToNext()) {
			String value = cr.getString(1);
			cr.close();
			closeDB();
			return Functions.toInt32(value);
		}
		cr.close();
		closeDB();
		return Integer.MIN_VALUE;
	}

	public void saveInteger(String id, int value) {
		int val = getIntegetValue(id);
		if (val == Integer.MIN_VALUE) {
			open();
			ContentValues cv = new ContentValues();
			cv.put("id", id);
			cv.put("value", String.valueOf(value));
			_DB.insert(_TableName, "id,value", cv);
			closeDB();
		} else {
			open();
			ContentValues cv = new ContentValues();
			cv.put("value", String.valueOf(value));
			_DB.update(_TableName, cv, _KeyFieldName + "='" + id + "'", null);
			closeDB();
		}

	}

}
