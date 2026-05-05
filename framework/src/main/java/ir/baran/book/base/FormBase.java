package ir.baran.book.base;

import ir.baran.framework.forms.Form;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public abstract class FormBase extends Form {
	@Override
	protected void initHeader(LinearLayout llHeader) {
		Animation animation = AnimationUtils.loadAnimation(this,
				android.R.anim.fade_in);
		llHeader.startAnimation(animation);
	}

	@Override
	public void initContent(LinearLayout llContent) {

	}

	@Override
	protected void initFooter(LinearLayout llFooter) {
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) llFooter
				.getLayoutParams();
		lp.height = 0;
	}
}
