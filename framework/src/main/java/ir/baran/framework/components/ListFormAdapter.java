package ir.baran.framework.components;

import ir.baran.framework.R;
import ir.baran.framework.forms.Form;
import ir.baran.framework.utilities.ConfigurationUtils;
import ir.baran.framework.utilities.ImageUtils;
import ir.baran.framework.utilities.MyConfig;

import java.util.Vector;

import android.database.DataSetObserver;
import android.graphics.drawable.BitmapDrawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

public abstract class ListFormAdapter implements ListAdapter {
	private Vector<ListFormItem> _Items;
	private Form form;

	public ListFormAdapter(Vector items, Form form) {
		this._Items = items;
		this.form = form;
		if (items == null)
			_Items = new Vector<ListFormItem>();
	}

	@Override
	public int getCount() {
		return _Items.size();
	}

	@Override
	public Object getItem(int position) {
		return _Items.elementAt(position);
	}

	@Override
	public long getItemId(int position) {
		return _Items.elementAt(position)._Id;
	}

	@Override
	public int getItemViewType(int position) {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ListFormItem item = (ListFormItem) getItem(position);
		return getView(item, position);
	}

	protected abstract View getView(ListFormItem item, int position);

	protected TextView createTextView(ListFormItem item) {
		TextView tv = new TextView(form);
		tv.setTypeface(MyConfig.getDefaultTypeface());
		tv.setGravity(Gravity.RIGHT);
		SpannableString sb = new SpannableString(item._Text);
		for (int i = 0; i < item._Highlights.size(); i++) {
			Highlight hi = item._Highlights.elementAt(i);
			try {
				ForegroundColorSpan what = new ForegroundColorSpan(hi._Color);
				sb.setSpan(what, hi._From, hi._To, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
				if (hi._Bold) {
					StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD);
					sb.setSpan(bss, hi._From, hi._To, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		tv.setText(sb);
		return tv;
	}

	protected ImageView createImageView(ListFormItem item) {
		if (item._bitmapResource <= 0 || item._bitmapBase64.length() <= 0 || item._bitmapPath.length() <= 0)
			return null;
		ImageView imageView = new ImageView(form);
		if (item._bitmapResource != 0)
			imageView.setBackgroundResource(item._bitmapResource);
		if (item._bitmapBase64.length() > 0)
			imageView.setBackgroundDrawable(new BitmapDrawable(ImageUtils.getBitmapFromBase64(item._bitmapBase64)));
		if (item._bitmapPath.length() > 0)
			imageView.setBackgroundDrawable(new BitmapDrawable(ImageUtils.getBitmapFromAssets(item._bitmapPath)));
		return imageView;
	}

	protected LinearLayout initSimpleView(ListFormItem itm, OnClickListener clickListener, OnLongClickListener onLongClickListener,
			String text) {
		LinearLayout v = (LinearLayout) form.getLayoutInflater().inflate(R.layout.list_item_simple, null);

		TextView tv = (TextView) v.findViewById(R.id.tvListItem);
		tv.setText(text);
		tv.setTypeface(MyConfig.getDefaultTypeface());
		tv.setTag(itm);
		tv.setOnLongClickListener(onLongClickListener);
		tv.setOnClickListener(clickListener);
		ConfigurationUtils.setDefaultTextSize(tv);
		return v;
	}

	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEmpty() {
		return _Items.size() <= 0;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean areAllItemsEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		// TODO Auto-generated method stub
		return false;
	}

	public void setItems(Vector<ListFormItem> items) {
		this._Items = items;
	}
}
