package ir.baran.framework.components;

import ir.baran.framework.R;
import ir.baran.framework.utilities.MyConfig;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SeekDialog extends Dialog {

	private LinearLayout llConfirm;
	private TextView lblConfirmHeader;
	private SeekBar sbSeek;
	private Button btnConfirmCancel;
	protected Button btnConfirmOk;
	private String header;
	private android.view.View.OnClickListener okClick;

	public SeekDialog(Context context, float progress, String header,
			View.OnClickListener okClick, View.OnClickListener onCancel,
			SeekDialogListener onchange) {
		super(context);
		this.header = header;
		this.okClick = okClick;
		setContentView(R.layout.seek_form);
		getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));
		initComponents();
		initGraphics();
		// -------------
		this.sbSeek.setOnSeekBarChangeListener(onchange);
		this.sbSeek.setProgress((int) progress);
		this.btnConfirmCancel.setOnClickListener(onCancel);

	}

	private void initGraphics() {
		this.llConfirm.setBackgroundDrawable(MyConfig.getWindowBackground());
		// -------------
		this.btnConfirmCancel.setTextColor(MyConfig.getDefaultTextColor());
		this.btnConfirmCancel.setBackgroundDrawable(MyConfig
				.getButtonBackground());
		// -------------
		this.btnConfirmOk.setBackgroundDrawable(MyConfig.getButtonBackground());
		this.btnConfirmOk.setOnClickListener(okClick);
		this.btnConfirmOk.setTextColor(MyConfig.getDefaultTextColor());
		// --------------
		MyConfig.initTextView(lblConfirmHeader);
		this.lblConfirmHeader.setTextColor(MyConfig.getDefaultTextColor());
		// --------------

		this.lblConfirmHeader.setText(header);
	}

	private void initComponents() {
		this.llConfirm = (LinearLayout) findViewById(R.id.llConfirm);
		this.lblConfirmHeader = (TextView) findViewById(R.id.lblConfirmHeader);
		this.sbSeek = (SeekBar) findViewById(R.id.sbSeek);
		this.btnConfirmCancel = (Button) findViewById(R.id.btnConfirmCancel);
		this.btnConfirmOk = (Button) findViewById(R.id.btnConfirmOk);
	}

	public int getProgress() {
		return sbSeek.getProgress();
	}
}
