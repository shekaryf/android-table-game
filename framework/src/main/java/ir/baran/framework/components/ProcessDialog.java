package ir.baran.framework.components;

import ir.baran.framework.R;
import ir.baran.framework.utilities.MyConfig;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ProcessDialog extends Dialog {

	private LinearLayout llConfirm;
	private TextView lblConfirmHeader;
	private TextView lblConfirmText;
	private Button btnConfirmCancel;
	private String text;
	private String header;
	private ProgressBar progressbar;

	public ProcessDialog(Context context, String text, String header) {
		super(context);
		this.text = text;
		this.header = header;
		setContentView(R.layout.process_form);
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
		this.btnConfirmCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ProcessDialog.this.cancel();
				ProcessDialog.this.dismiss();
			}
		});
		// -------------
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
		this.progressbar = (ProgressBar) findViewById(R.id.progressBar1);
		this.llConfirm = (LinearLayout) findViewById(R.id.llConfirm);
		this.lblConfirmHeader = (TextView) findViewById(R.id.lblConfirmHeader);
		this.lblConfirmText = (TextView) findViewById(R.id.lblConfirmText);
		this.btnConfirmCancel = (Button) findViewById(R.id.btnConfirmCancel);
	}

	public void setMax(int max) {
		this.progressbar.setMax(max);
	}
	public void setProgress(int progress) {
		this.progressbar.setProgress(progress);
	}
}
