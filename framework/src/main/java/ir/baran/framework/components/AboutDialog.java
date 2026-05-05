package ir.baran.framework.components;

import ir.baran.framework.utilities.MyConfig;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class AboutDialog extends Dialog {

	protected Button btnConfirmOk;

	public AboutDialog(Context context, int contentViewRes, int okResButton) {
		super(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(contentViewRes);
		getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));
		if (okResButton != 0) {
			btnConfirmOk = (Button) findViewById(okResButton);
			initGraphics();
		}
	}

	protected void initGraphics() {
		// -------------
		this.btnConfirmOk.setBackgroundDrawable(MyConfig.getButtonBackground());
		this.btnConfirmOk.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				AboutDialog.this.dismiss();
			}
		});
		this.btnConfirmOk.setTextColor(MyConfig.getDefaultTextColor());
	}
}
