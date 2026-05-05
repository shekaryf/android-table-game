package ir.baran.framework.database;

import android.database.Cursor;

public abstract class StorableObject {

	public abstract void load(Cursor cr);

	public abstract StorableObject clone();

	public abstract String getFieldsSelect();

}
