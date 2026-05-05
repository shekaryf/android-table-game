package ir.baran.framework.components;

import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public abstract class SeekDialogListener implements OnSeekBarChangeListener {

	@Override
	public abstract void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser);

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

}
