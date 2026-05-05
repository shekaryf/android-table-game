package ir.baran.framework.utilities;

import java.util.Vector;

import ir.baran.framework.R;
import ir.baran.framework.database.StorableController;
import ir.baran.framework.database.StorableObject;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ProductsView extends LinearLayout {

	public ProductsView(final Context context, AttributeSet attrs) {
		super(context, attrs);
//		ProductController pc = new ProductController();
//		Vector<StorableObject> items = pc.getItems("");
//		for (StorableObject storableObject : items) {
//			final ProductItem pi = (ProductItem) storableObject;
//
//			OnClickListener click = new OnClickListener() {
//
//				@Override
//				public void onClick(View arg0) {
//					startClick(context, pi._urlSite, pi._urlBazaar);
//				}
//			};
//			addProduct(context, pi._description, click, ImageUtils.getBitmapFromAssets(pi._puc));
//		}
	}

	private void startClick(final Context context, String url, String urlBazar) {
		try {
			Functions.startUrl(urlBazar, context);
		} catch (Exception e) {
			Functions.startUrl(url, context);
		}
	}

	private void addProduct(Context context, String text, OnClickListener click, Bitmap bmp) {
		LinearLayout ll = new LinearLayout(context);
		LinearLayout.LayoutParams params = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		this.addView(ll);
		params.topMargin = Functions.dp2px(10);
		params.gravity = Gravity.RIGHT;
		ll.setLayoutParams(params);
		ll.setGravity(Gravity.RIGHT);
		ll.setOrientation(LinearLayout.HORIZONTAL);
		TextView tv = new TextView(context);
		LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		tvParams.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
		tvParams.leftMargin = Functions.dp2px(49);
		tvParams.rightMargin = Functions.dp2px(1);
		tv.setGravity(Gravity.RIGHT);
		int pd = Functions.dp2px(1);
		tv.setPadding(pd, pd, pd, pd);
		tv.setLayoutParams(tvParams);
		tv.setOnClickListener(click);
		text = Functions.getNormalString(text);
		tv.setText(text);
		tv.setTypeface(MyConfig.getDefaultTypeface());
		tv.setTextColor(getResources().getColor(R.color.aboutTextColor));
		ll.addView(tv);
		ImageView iv = new ImageView(context);
		iv.setOnClickListener(click);
		int w = Functions.dp2px(50);
		LinearLayout.LayoutParams ivParams = new LinearLayout.LayoutParams(w, w);
		ivParams.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
		iv.setBackgroundDrawable(new BitmapDrawable(bmp));
		iv.setLayoutParams(ivParams);
		ll.addView(iv);
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
}
