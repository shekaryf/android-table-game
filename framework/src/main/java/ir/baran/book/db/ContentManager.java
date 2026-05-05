package ir.baran.book.db;

import ir.baran.framework.components.ListFormItem;
import ir.baran.framework.database.DBM;

import java.util.Vector;

import android.content.ContentValues;
import android.database.Cursor;

public class ContentManager extends DBM {
	public static final int MAX_CONTENT_SEARCH = 100;
	protected static ContentManager instance;

	// CREATE TABLE "data" ("id" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL
	// UNIQUE , "title" TEXT NOT NULL , "content" TEXT)
	public static ContentManager getInstance() {
		if (instance == null) {
			instance = new ContentManager();
		}
		return instance;
	}

	public ContentManager() {
		_KeyFieldName = "id";
		_TableName = "data";
	}

	public Vector<ListFormItem> getTitlesInFirst(String searchItem) {
		searchItem = searchItem.trim();
		Cursor cr = selectTitle("title like '" + searchItem + "%'");
		return addItems(cr);
	}

	protected Vector<ListFormItem> addItems(Cursor cr) {
		Vector<ListFormItem> finds = new Vector<ListFormItem>();
		while (cr.moveToNext()) {
			ListFormItem item = new ListFormItem();
			item._Id = cr.getInt(0);
			item._Text = " " + cr.getString(1);
			finds.add(item);
		}
		cr.close();
		closeDB();
		return finds;
	}

	public String getContent(int id) {
		Cursor cr = select("content", "id=" + id, "title");
		String description = " ";
		while (cr.moveToNext()) {
			description = cr.getString(0);
		}
		cr.close();
		closeDB();
		return description;
	}

	public ListFormItem getItem(int id) {
		Cursor cr = select("id,title", "id=" + id, "");
		ListFormItem item = new ListFormItem();
		if (cr.moveToNext()) {
			item._Id = cr.getInt(0);
			item._Text = cr.getString(1);
		}
		cr.close();
		closeDB();
		return item;
	}

	private Cursor selectTitle(String caseSelect) {
		return select("id,title", caseSelect, "title");
	}

	public Vector<ListFormItem> getTitlesInText(String searchItem) {
		searchItem = searchItem.trim();
		Cursor cr = selectTitle("title like '%" + searchItem + "%'");
		return addItems(cr);
	}

	@Override
	protected int putUpdates(ContentValues cv, Object item) {
		// TODO Auto-generated method stub
		return 0;
	}

	public Vector<ContentItem> getContents(String searchText) {
		searchText = searchText.trim();
		Cursor cr = select("id,title,content", "content like '%" + searchText + "%'", "title", "0," + MAX_CONTENT_SEARCH);
		Vector<ContentItem> finds = new Vector<ContentItem>();
		while (cr.moveToNext()) {
			ContentItem item = new ContentItem();
			item._Id = cr.getInt(0);
			item._Text = " " + cr.getString(1);
			item.content = cr.getString(2);
			finds.add(item);
		}
		cr.close();
		closeDB();
		return finds;
	}

	public int getMaxIndex() {
		Cursor cr = select("MAX(id) as mx", "", "", "");
		int maxIdx = 0;
		if (cr.moveToNext()) {
			maxIdx = cr.getInt(0);
		}
		cr.close();
		closeDB();
		return maxIdx;

	}

}
