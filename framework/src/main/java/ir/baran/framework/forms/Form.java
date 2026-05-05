package ir.baran.framework.forms;

import ir.baran.framework.R;
import ir.baran.framework.components.ConfirmDialog;
import ir.baran.framework.utilities.Functions;
import ir.baran.framework.utilities.MyConfig;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.ComponentActivity;

public abstract class Form extends ComponentActivity {
	/** Called when the activity is first created. */
	protected LinearLayout llHeader;
	protected LinearLayout llFooter;
	protected LinearLayout llContent;
	protected ConfirmDialog confirmDialog;
	protected RelativeLayout rlMain;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		onCreate(savedInstanceState, R.layout.main);
		loadViews();
	}

	protected void onCreate(Bundle savedInstanceState, int mainResourceId) {
		super.onCreate(savedInstanceState);
		setContentView(mainResourceId);
	}

	protected void loadViews() {
		llHeader = (LinearLayout) findViewById(R.id.llHeader);
		llFooter = (LinearLayout) findViewById(R.id.llFooter);
		llContent = (LinearLayout) findViewById(R.id.llContent);
		rlMain = (RelativeLayout) findViewById(R.id.rlMain);
		initContent(llContent);
		initHeader(llHeader);
		initFooter(llFooter);
	}

	protected void baseOnCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	protected void initFooter(LinearLayout llFooter) {

	}

	protected void initHeader(LinearLayout llHeader) {

	}

	public abstract void initContent(LinearLayout llContent);

	public void confirm(final String text, final String title, final OnClickListener okClick) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Form.this.confirmDialog = new ConfirmDialog(Form.this, text, title, new OnClickListener() {

					@Override
					public void onClick(View v) {
						confirmDialog.dismiss();
						okClick.onClick(v);
					}
				});
				confirmDialog.show();
			}
		});
	}

	public void confirm(final String text, final String title, final OnClickListener okClick, final OnClickListener cancelClick) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Form.this.confirmDialog = new ConfirmDialog(Form.this, text, title, new OnClickListener() {

					@Override
					public void onClick(View v) {
						confirmDialog.dismiss();
						okClick.onClick(v);
					}
				}, cancelClick);
				confirmDialog.show();
			}
		});
	}

	public void showMessage(final String message) {
		final int time = 2000;
		showMessage(message, time);
	}

	public void showMessage(final String message, final int time) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast toast = new Toast(Form.this);
				toast.setDuration(time);
				LinearLayout ll = new LinearLayout(Form.this);
				toast.setView(ll);
				ll.setBackgroundColor(0x00000000);
				TextView view = new TextView(Form.this);

				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT);
				int margin = Functions.dp2px(15);
				view.setPadding(margin, margin, margin, margin);

				lp.setMargins(margin, margin, margin, margin);
				lp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
				view.setGravity(lp.gravity);
				view.setLayoutParams(lp);
				ll.addView(view);
				GradientDrawable gradiant = MyConfig.getDefaultPressGradiant();
				gradiant.setCornerRadius(MyConfig.getDefaultCornerRadios());
				gradiant.setStroke(MyConfig.getDefaultStrokeWidth(), MyConfig.getDefaultStrokeColor());

				view.setBackgroundDrawable(gradiant);
				view.setText(message);
				view.setTextColor(MyConfig.getDefaultTextColor());
				toast.show();
			}
		});
	}

	public void superStartActivity(Intent intent) {
		super.startActivity(intent);
	}

	@Override
	public void startActivity(Intent intent) {
		super.startActivity(intent);
		// overridePendingTransition(R.anim.slide_in_right,
		// R.anim.slide_out_left);
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}

	public int getScreenWidth() {
		Display display = getWindowManager().getDefaultDisplay();
		return display.getWidth();
	}

	public int getScreenHeight() {
		Display display = getWindowManager().getDefaultDisplay();
		return display.getHeight();
	}

}
