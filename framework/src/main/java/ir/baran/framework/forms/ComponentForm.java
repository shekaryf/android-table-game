package ir.baran.framework.forms;

import ir.baran.framework.R;
import ir.baran.framework.components.MultiLangTextView;
import ir.baran.framework.utilities.ImageUtils;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class ComponentForm extends Form {

	protected LinearLayout llComponents;
	protected ScrollView svComponents;

	@Override
	public void initContent(LinearLayout llContent) {
		LayoutInflater inf = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View viewItems = inf.inflate(R.layout.component_form, llContent);
		this.llComponents = (LinearLayout) viewItems
				.findViewById(R.id.llComponents);
		this.svComponents = (ScrollView) viewItems
				.findViewById(R.id.svComponents);
	}

	protected TextView addText(String text) {
		TextView tv = addText();
		tv.setText(text);
		return tv;
	}

	protected TextView addText() {
		TextView tv = new TextView(this);
		this.llComponents.addView(tv);
		return tv;
	}

	protected ImageView addImage(int resource) {
		ImageView iv = addImage();
		iv.setBackgroundResource(resource);
		return iv;
	}

	protected ImageView addImage() {
		ImageView iv = new ImageView(this);
		this.llComponents.addView(iv);
		return iv;
	}

	protected ImageView addImage(String bitmapPath) {
		ImageView iv = addImage();
		this.llComponents.setBackgroundDrawable(new BitmapDrawable(ImageUtils
				.getBitmapFromAssets(bitmapPath)));
		return iv;
	}

	protected ImageView addImageAsBase64(String content) {
		ImageView iv = addImage();
		this.llComponents.setBackgroundDrawable(new BitmapDrawable(ImageUtils
				.getBitmapFromBase64(content)));
		return iv;
	}

	protected void scrollTo(final int startText, final TextView textView) {
		ViewTreeObserver observer = textView.getViewTreeObserver();
		observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout() {
				Layout lay = textView.getLayout();
				int line = lay.getLineForOffset(startText);
				int top = lay.getLineTop(line) + textView.getTop();
				svComponents.scrollTo(0, top);
			}
		});
	}

	protected void scrollTo(final int startText, final MultiLangTextView textView) {
		ViewTreeObserver observer = textView.getViewTreeObserver();
		observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout() {
				Layout lay = textView.getLayout();
				int line = lay.getLineForOffset(startText);
				int top = lay.getLineTop(line) + textView.getTop();
				svComponents.scrollTo(0, top);
			}
		});
	}

}
