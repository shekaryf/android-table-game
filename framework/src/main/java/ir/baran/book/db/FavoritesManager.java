package ir.baran.book.db;

import ir.baran.framework.database.DBM;

import java.util.Vector;

import android.content.ContentValues;
import android.database.Cursor;

public class FavoritesManager extends DBM {
	private static FavoritesManager instance;

	// CREATE TABLE "favorites" ("id" INTEGER PRIMARY KEY NOT NULL UNIQUE ,
	// "title" TEXT, "date" TEXT)
	public static FavoritesManager getInstance() {
		if (instance == null) {
			instance = new FavoritesManager();
		}
		return instance;
	}

	public FavoritesManager() {
		_KeyFieldName = "id";
		_TableName = "favorites";
	}

	public Vector<FavoritesItem> getItems() {
		Cursor cr = select("id,title,date", "", "date desc");
		return addItems(cr);
	}

	public boolean contains(int id) {
		Cursor cr = select("id", "id=" + id, "", "1");
		boolean exists = cr.moveToNext();
		cr.close();
		closeDB();
		return exists;
	}

	private Vector<FavoritesItem> addItems(Cursor cr) {
		Vector<FavoritesItem> finds = new Vector<FavoritesItem>();
		while (cr.moveToNext()) {
			FavoritesItem item = new FavoritesItem();
			item._Id = cr.getInt(0);
			item._Text = " " + cr.getString(1);
			item.date = " " + cr.getString(2);
			finds.add(item);
		}
		cr.close();
		closeDB();
		return finds;
	}

	@Override
	protected int putUpdates(ContentValues cv, Object item) {
		FavoritesItem itm = (FavoritesItem) item;
		if (itm._Id > 0)
			cv.put("id", itm._Id);
		cv.put("title", itm._Text);
		cv.put("date", itm.date);
		return 0;
	}
}
