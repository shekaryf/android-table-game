package ir.baran.framework.database;

import java.util.Vector;
import android.database.Cursor;

public abstract class StorableController extends DBM {
	private StorableObject _storableObject;

	public StorableController(String tableName, String keyFieldName,
			StorableObject storableObject) {
		this._TableName = tableName;
		this._KeyFieldName = keyFieldName;
		this._storableObject = storableObject;
	}

	public Vector<StorableObject> getItems(String where, String orderBy) {
		Vector<StorableObject> items = new Vector<StorableObject>();
		Cursor cr = select(_storableObject.getFieldsSelect(), where, orderBy);
		while (cr.moveToNext()) {
			StorableObject item = _storableObject.clone();
			item.load(cr);
			items.add(item);
		}
		cr.close();
		closeDB();
		return items;
	}

	public Vector<StorableObject> getItems(String where) {
		return getItems(where, "");
	}

	public StorableObject getItem(int id) {
		Cursor cr = select(_storableObject.getFieldsSelect(), _KeyFieldName
				+ "=" + id, "");
		StorableObject item = _storableObject.clone();
		if (cr.moveToNext()) {
			item.load(cr);
			cr.close();
			closeDB();
			return item;
		}
		cr.close();
		closeDB();
		return null;
	}

	public StorableObject getItemAtIndex(int index) {
		Cursor cr = select(_storableObject.getFieldsSelect(), "", "", index
				+ ",1");
		StorableObject item = _storableObject.clone();
		if (cr.moveToNext()) {
			item.load(cr);
			cr.close();
			closeDB();
			return item;
		}
		cr.close();
		closeDB();
		return null;
	}

}
