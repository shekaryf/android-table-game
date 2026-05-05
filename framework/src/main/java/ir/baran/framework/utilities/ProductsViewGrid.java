package ir.baran.framework.utilities;

import ir.baran.framework.R;
import ir.baran.framework.database.StorableController;
import ir.baran.framework.database.StorableObject;

import java.util.Vector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class ProductsViewGrid extends GridView implements android.widget.AdapterView.OnItemClickListener {

	private Vector<StorableObject> items;

	public ProductsViewGrid(final Context context, AttributeSet attrs) {
		super(context, attrs);
//		ProductController pc = new ProductController();
//		items = pc.getItems("");
//		setNumColumns(3);
//		setOnItemClickListener(this);
//		setAdapter(new GridAdapter());
	}

	private void startClick(final Context context, String url, String urlBazar) {
		try {
			Functions.startUrl(urlBazar, context);
		} catch (Exception e) {
			Functions.startUrl(url, context);
		}
	}

	private class ProductItem extends StorableObject {
		public int _id;
		public String _description;
		public String _urlBazaar;
		public String _urlSite;
		public String _puc;

		@Override
		public void load(Cursor cr) {
			_id = cr.getInt(0);
			_description = cr.getString(1);
			_urlBazaar = cr.getString(2);
			_urlSite = cr.getString(3);
			_puc = cr.getString(4);
		}

		@Override
		public StorableObject clone() {
			ProductItem item = new ProductItem();
			item._description = _description;
			item._puc = _puc;
			item._urlBazaar = _urlBazaar;
			item._urlSite = _urlSite;
			item._id = _id;
			return item;
		}

		@Override
		public String getFieldsSelect() {
			return "id,description,urlBazaar,urlSite,puc";
		}

	}

	private class ProductController extends StorableController {

		public ProductController() {
			super("Products", "id", new ProductItem());
		}

		@Override
		protected int putUpdates(ContentValues cv, Object item) {
			// TODO Auto-generated method stub
			return 0;
		}

	}

	private class GridAdapter extends ArrayAdapter {

		public GridAdapter() {
			super(ProductsViewGrid.this.getContext(), android.R.layout.simple_list_item_1);
			// TODO Auto-generated constructor stub
		}

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.product_item, null);
			TextView tv = (TextView) view.findViewById(R.id.tvListItem);
			ProductItem pi = (ProductItem) items.elementAt(position);
			tv.setText(pi._description);
			ConfigurationUtils.setDefaultTextSize(tv);
			// tv.setTypeface(MyConfig.getDefaultTypeface());
			ImageView iv = (ImageView) view.findViewById(R.id.ivProduct);
			iv.setImageBitmap(ImageUtils.getBitmapFromAssets(pi._puc));
			view.setTag(pi);
			return view;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {
		ProductItem item = (ProductItem) view.getTag();
		startClick(getContext(), item._urlSite, item._urlBazaar);
	}
}
