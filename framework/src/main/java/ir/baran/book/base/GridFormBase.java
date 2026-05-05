package ir.baran.book.base;

import ir.baran.framework.R;
import ir.baran.framework.forms.GridForm;
import ir.baran.framework.utilities.Functions;
import ir.baran.framework.utilities.MyConfig;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public abstract class GridFormBase extends GridForm {
	protected TextView lblHead;

	@Override
	protected void initHeader(LinearLayout llHeader) {
		llHeader.setBackgroundResource(R.drawable.header);

		this.lblHead = new TextView(this);
		this.lblHead.setTypeface(MyConfig.getNextTypeface());
		this.lblHead.setGravity(Gravity.CENTER_HORIZONTAL
				| Gravity.CENTER_VERTICAL);
		llHeader.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
		llHeader.addView(lblHead);
	}

	@Override
	protected void initFooter(LinearLayout llFooter) {
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) llFooter
				.getLayoutParams();
		lp.height = 0;
	}

	@Override
	public void initContent(LinearLayout llContent) {
		super.initContent(llContent);
		llContent.setBackgroundResource(R.drawable.bg_form);
		RelativeLayout.LayoutParams lp = (LayoutParams) llContent
				.getLayoutParams();
		int margin = Functions.dp2px(10);
		lp.setMargins(margin, margin, margin, margin);
	}

}
