package ir.baran.framework.components;

import ir.baran.framework.R;
import ir.baran.framework.utilities.MyConfig;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ConfirmDialog extends Dialog {

	private LinearLayout llConfirm;
	private TextView lblConfirmHeader;
	private TextView lblConfirmText;
	private Button btnConfirmCancel;
	private Button btnConfirmOk;
	private String text;
	private String header;
	private android.view.View.OnClickListener okClick;
	private android.view.View.OnClickListener cancelClick;

	public ConfirmDialog(Context context, String text, String header,
			View.OnClickListener okClick) {
		super(context);
		this.text = text;
		this.header = header;
		this.okClick = okClick;
		setContentView(R.layout.confirm_form);
		getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));
		initComponents();
		initGraphics();
	}

	public ConfirmDialog(Context context, String text, String header,
			View.OnClickListener okClick, View.OnClickListener cancelClick) {
		super(context);
		this.text = text;
		this.header = header;
		this.okClick = okClick;
		this.cancelClick = cancelClick;
		setContentView(R.layout.confirm_form);
		getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));
		initComponents();
		initGraphics();
	}

	private void initGraphics() {
		this.llConfirm.setBackgroundDrawable(MyConfig.getWindowBackground());
		// -------------
		this.btnConfirmCancel.setTextColor(MyConfig.getDefaultTextColor());
		this.btnConfirmCancel.setBackgroundDrawable(MyConfig
				.getButtonBackground());
		if (this.cancelClick != null)
			btnConfirmCancel.setOnClickListener(this.cancelClick);
		else
			btnConfirmCancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ConfirmDialog.this.dismiss();
				}
			});
		// -------------
		this.btnConfirmOk.setBackgroundDrawable(MyConfig.getButtonBackground());
		if (okClick != null)
			this.btnConfirmOk.setOnClickListener(okClick);
		else
			this.btnConfirmOk.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ConfirmDialog.this.dismiss();
				}
			});
		this.btnConfirmOk.setTextColor(MyConfig.getDefaultTextColor());
		// --------------
		MyConfig.initTextView(lblConfirmHeader);
		this.lblConfirmHeader.setTextColor(MyConfig.getDefaultTextColor());
		this.lblConfirmHeader.setTypeface(MyConfig.getDefaultTypeface(),
				android.graphics.Typeface.BOLD);
		// --------------
		MyConfig.initTextView(lblConfirmText);
		this.lblConfirmHeader.setText(header);
		// -------------
		this.lblConfirmText.setText(text);
	}

	private void initComponents() {
		this.llConfirm = (LinearLayout) findViewById(R.id.llConfirm);
		this.lblConfirmHeader = (TextView) findViewById(R.id.lblConfirmHeader);
		this.lblConfirmText = (TextView) findViewById(R.id.lblConfirmText);
		this.btnConfirmCancel = (Button) findViewById(R.id.btnConfirmCancel);
		this.btnConfirmOk = (Button) findViewById(R.id.btnConfirmOk);
	}
}
