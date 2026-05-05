package ir.baran.framework.components;

import ir.baran.framework.forms.Form;
import ir.baran.framework.utilities.ConfigurationUtils;
import ir.baran.framework.utilities.Functions;
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
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

public abstract class ListFormArrayAdapter extends ArrayAdapter {
	private Vector<ListFormItem> _Items;
	private Form form;

	public ListFormArrayAdapter(Vector items, Form form) {
		super(form, android.R.layout.simple_list_item_1);
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
				sb.setSpan(what, hi._From, hi._To,
						Spannable.SPAN_INCLUSIVE_INCLUSIVE);
				if (hi._Bold) {
					StyleSpan bss = new StyleSpan(
							android.graphics.Typeface.BOLD);
					sb.setSpan(bss, hi._From, hi._To,
							Spannable.SPAN_INCLUSIVE_INCLUSIVE);
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		tv.setText(sb);
		return tv;
	}

	protected ImageView createImageView(ListFormItem item) {
		if (item._bitmapResource <= 0 || item._bitmapBase64.length() <= 0
				|| item._bitmapPath.length() <= 0)
			return null;
		ImageView imageView = new ImageView(form);
		if (item._bitmapResource != 0)
			imageView.setBackgroundResource(item._bitmapResource);
		if (item._bitmapBase64.length() > 0)
			imageView.setBackgroundDrawable(new BitmapDrawable(ImageUtils
					.getBitmapFromBase64(item._bitmapBase64)));
		if (item._bitmapPath.length() > 0)
			imageView.setBackgroundDrawable(new BitmapDrawable(ImageUtils
					.getBitmapFromAssets(item._bitmapPath)));
		return imageView;
	}

	protected LinearLayout initSimpleView(ListFormItem itm,
			OnClickListener clickListener,
			OnLongClickListener onLongClickListener, String text) {
		AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
				AbsListView.LayoutParams.MATCH_PARENT,
				AbsListView.LayoutParams.WRAP_CONTENT);
		LinearLayout lv = new LinearLayout(form);
		lv.setLayoutParams(lp);
		lv.setGravity(Gravity.CENTER_HORIZONTAL);

		TextView tv = new TextView(form);
		LinearLayout.LayoutParams lpText = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT);
		int margin = Functions.dp2px(5);
		lpText.leftMargin = margin;
		lpText.rightMargin = margin;
		lpText.topMargin = margin;
		lpText.bottomMargin = margin;
		tv.setLayoutParams(lpText);
		tv.setPadding(0, margin, 0, margin);
		tv.setText(text);
		tv.setGravity(Gravity.CENTER_HORIZONTAL);
//		tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP,
//				MyConfig.getDefaultTextSize());
		ConfigurationUtils.setDefaultTextSize(tv);
		tv.setTextColor(MyConfig.getNextTextColor());
		tv.setTypeface(MyConfig.getDefaultTypeface());
		tv.setBackgroundDrawable(MyConfig.getDefaultStatesDrawable());

		lv.addView(tv);
		tv.setTag(itm);
		tv.setOnLongClickListener(onLongClickListener);
		tv.setOnClickListener(clickListener);
		return lv;
	}


}
