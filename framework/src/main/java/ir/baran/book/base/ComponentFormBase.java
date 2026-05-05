package ir.baran.book.base;

import ir.baran.framework.R;
import ir.baran.framework.forms.ComponentForm;
import ir.baran.framework.utilities.ConfigurationUtils;
import ir.baran.framework.utilities.Functions;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class ComponentFormBase extends ComponentForm {
	@Override
	protected void initHeader(LinearLayout llHeader) {
		llHeader.setBackgroundResource(R.drawable.header);
	}

	@Override
	protected void initFooter(LinearLayout llFooter) {
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) llFooter.getLayoutParams();
		lp.height = 0;
	}

	@Override
	public void initContent(LinearLayout llContent) {
		super.initContent(llContent);
		llContent.setBackgroundResource(R.drawable.bg_content);
		RelativeLayout.LayoutParams lp = (LayoutParams) llContent.getLayoutParams();
		int margin = Functions.dp2px(10);
		lp.setMargins(margin, margin, margin, margin);
	}

}
