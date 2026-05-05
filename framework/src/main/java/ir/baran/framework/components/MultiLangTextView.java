package ir.baran.framework.components;

import java.io.IOException;

import ir.baran.framework.R;
import ir.baran.framework.utilities.AssetAudioUtils;
import ir.baran.framework.utilities.ConfigurationUtils;
import ir.baran.framework.utilities.Functions;
import ir.baran.framework.utilities.ImageUtils;
import ir.baran.framework.utilities.MyConfig;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Handler;
import android.os.Looper;
import android.media.MediaPlayer;
import android.text.Layout;
import android.text.SpannableString;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class MultiLangTextView extends LinearLayout {
	private static final String SRC_PATH = "src='";
	public static final String IMG_TAG = "<img";
	public static final String RES_TAG = "<res";
	public static String _TAG = "<img";
	private TextView defaultTextView;
	private Typeface typeface;
	private int textColor;
	private float textSize;
	private float add;
	private float mult;
	private MediaPlayer mediaPlayer;
	private Button activeAudioButton;
	private String activeAudioSource;
	private Dialog activeAudioDialog;
	private SeekBar audioProgressSeekBar;
	private TextView audioCurrentTimeTextView;
	private TextView audioTotalTimeTextView;
	private final Handler progressHandler = new Handler(Looper.getMainLooper());
	private Runnable progressUpdater;
	private boolean isUserSeeking;

	public MultiLangTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MultiLangTextView(Context context) {
		super(context);
		setOrientation(LinearLayout.VERTICAL);
	}

	public void setText(SpannableString sb) {
		createDefaultTextView();
		defaultTextView.setText(sb);
	}

	public void setText(String content) {
		stopCurrentAudio();
		if (defaultTextView == null)
			removeAllViews();

		if (!isHtmlContent(content)) {
			createDefaultTextView();
			defaultTextView.setText(content);
		} else
			createRichText(content);
	}

	private void createRichText(String content) {
		defaultTextView = null;
		removeAllViews();
		int idx = content.indexOf(_TAG);
		int start = 0;
		int end = 0;
		if (idx == 0) {
			String imagePath = getImagePath(content, idx);
			addObject(imagePath);
			end = content.indexOf(">", idx) + 1;
			idx = content.indexOf(_TAG, end);
			start = end;
		}
		int preIdx = idx;
		while (idx > 0) {
			preIdx = idx;
			String text = content.substring(start, idx);
			addText(text);

			String imagePath = getImagePath(content, idx + 1);
			addObject(imagePath);
			end = content.indexOf(">", idx);
			idx = content.indexOf(_TAG, end + 1);
			start = end + 1;
		}
		if (preIdx > 0) {
			end = content.indexOf(">", preIdx);
			int length = content.length();
			if (end + 1 < length) {
				String text = content.substring(end + 1, length - 1);
				addText(text);
			}
		}
	}

	private void addText(String text) {
		TextView tv = new TextView(getContext());
		tv.setText(text);
		addTextView(tv);
	}

	private void addTextView(TextView tv) {
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		tv.setLayoutParams(lp);
		tv.setTextSize(textSize);
		tv.setTypeface(typeface);
		tv.setTextColor(textColor);
		this.addView(tv);
	}

	private void addObject(String src) {
		if (src.startsWith("http://")) {
			addLink(src);
		} else if (isAudioAsset(src)) {
			addAudioPlayer(src);
		} else
			addImage(src);
	}

	private boolean isAudioAsset(String src) {
		if (src == null)
			return false;
		String lowerCaseSrc = src.toLowerCase();
		return lowerCaseSrc.endsWith(".mp3") || lowerCaseSrc.endsWith(".wav") || lowerCaseSrc.endsWith(".ogg")
				|| lowerCaseSrc.endsWith(".m4a");
	}

	private void addLink(String src) {
		final String[] strArr = src.split(";");
		TextView tv = new TextView(getContext());
		tv.setText(strArr[1]);
		addTextView(tv);
		tv.setGravity(Gravity.CENTER);
		tv.setTextColor(getResources().getColor(R.color.link_text_color));
		tv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Functions.startUrl(strArr[0], getContext());
			}
		});
	}

	private void addImage(String src) {
		Bitmap bitmap = null;
		if (_TAG.compareTo(IMG_TAG) == 0)
			bitmap = ImageUtils.getBitmapFromAssets(src);
		else {
			int resID = getResources().getIdentifier(src, "drawable", getContext().getPackageName());
			bitmap = BitmapFactory.decodeResource(getResources(), resID);
		}
		if (bitmap == null)
			return;
		ImageView iv = new ImageView(getContext());
		BitmapDrawable drawable = new BitmapDrawable(bitmap);
		iv.setBackgroundDrawable(drawable);
		iv.setScaleType(ImageView.ScaleType.FIT_XY);

		int w = ConfigurationUtils.getScreenWidth(getContext()) - Functions.dp2px(20);
		int h = w * Functions.dp2px(bitmap.getHeight()) / Functions.dp2px(bitmap.getWidth());
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(w,
				h);
		lp.gravity = Gravity.CENTER;
		iv.setLayoutParams(lp);
		this.addView(iv);
	}

	private void addAudioPlayer(final String src) {
		final Button button = new Button(getContext());
		styleAudioButton(button);
		button.setText(getAudioLabel(src, false));
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.gravity = Gravity.CENTER;
		lp.topMargin = Functions.dp2px(2);
		lp.bottomMargin = Functions.dp2px(1);
		button.setLayoutParams(lp);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				toggleAudioPlayback(src, button);
			}
		});
		this.addView(button);
	}

	private String getAudioLabel(String src, boolean isPlaying) {
		String fileName = src;
		int slashIndex = src.lastIndexOf("/");
		if (slashIndex >= 0 && slashIndex + 1 < src.length())
			fileName = src.substring(slashIndex + 1);
		String prefix = isPlaying ? "❚❚ Pause  " : "▶ Play  ";
		return prefix;// + fileName
	}

	private void styleAudioButton(Button button) {
		button.setAllCaps(false);
		button.setTextColor(getResources().getColor(R.color.audio_player_text));
		button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		int horizontalPadding = Functions.dp2px(18);
		int verticalPadding = Functions.dp2px(10);
		button.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
		button.setGravity(Gravity.CENTER);
		button.setShadowLayer(0, 0, 0, Color.TRANSPARENT);
		button.setBackgroundDrawable(createAudioButtonBackground());
	}

	private StateListDrawable createAudioButtonBackground() {
		StateListDrawable states = new StateListDrawable();
		states.addState(new int[] { android.R.attr.state_pressed }, createAudioShape(true));
		states.addState(new int[] {}, createAudioShape(false));
		return states;
	}

	private GradientDrawable createAudioShape(boolean pressed) {
		int startColor = getResources().getColor(pressed ? R.color.audio_player_pressed_start : R.color.audio_player_bg_start);
		int endColor = getResources().getColor(pressed ? R.color.audio_player_pressed_end : R.color.audio_player_bg_end);
		GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[] { startColor,
				endColor });
		drawable.setCornerRadius(Functions.dp2px(22));
		drawable.setStroke(Functions.dp2px(1), getResources().getColor(R.color.audio_player_stroke));
		return drawable;
	}

	private void toggleAudioPlayback(String src, Button button) {
		if (mediaPlayer != null && mediaPlayer.isPlaying() && src.equals(activeAudioSource)) {
			stopCurrentAudio();
			return;
		}

		stopCurrentAudio();
		try {
			MediaPlayer player = new MediaPlayer();
			AssetAudioUtils.setDataSourceFromAssets(getContext(), MyConfig._AssetManager, player, src);
			player.prepare();
			final int duration = player.getDuration();
			player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

				@Override
				public void onCompletion(MediaPlayer mp) {
					stopCurrentAudio();
				}
			});
			player.start();
			mediaPlayer = player;
			activeAudioButton = button;
			activeAudioSource = src;
			button.setText(getAudioLabel(src, true));
			if (isLongMp3(src, duration))
				showAudioDialog(src);
		} catch (IOException e) {
			e.printStackTrace();
			stopCurrentAudio();
		}
	}

	private void stopCurrentAudio() {
		stopProgressUpdater();
		if (activeAudioButton != null && activeAudioSource != null)
			activeAudioButton.setText(getAudioLabel(activeAudioSource, false));
		if (activeAudioDialog != null) {
			activeAudioDialog.setOnDismissListener(null);
			activeAudioDialog.dismiss();
			activeAudioDialog = null;
		}
		if (mediaPlayer != null) {
			try {
				if (mediaPlayer.isPlaying())
					mediaPlayer.stop();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
			mediaPlayer.release();
		}
		mediaPlayer = null;
		activeAudioButton = null;
		activeAudioSource = null;
		audioProgressSeekBar = null;
		audioCurrentTimeTextView = null;
		audioTotalTimeTextView = null;
		isUserSeeking = false;
	}

	private boolean isLongMp3(String src, int duration) {
		if (src == null)
			return false;
		return src.toLowerCase().endsWith(".mp3") && duration > 60000;
	}

	private void showAudioDialog(String src) {
		if (mediaPlayer == null)
			return;

		LinearLayout container = new LinearLayout(getContext());
		container.setOrientation(LinearLayout.VERTICAL);
		int padding = Functions.dp2px(18);
		container.setPadding(padding, padding, padding, padding);
		container.setBackgroundDrawable(createDialogPanelBackground());
		container.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));

		TextView fileNameView = new TextView(getContext());
		fileNameView.setText(getFileName(src));
		fileNameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
		fileNameView.setTypeface(typeface);
		fileNameView.setTextColor(getResources().getColor(R.color.audio_dialog_title));
		fileNameView.setGravity(Gravity.CENTER_HORIZONTAL);
		container.addView(fileNameView);

		LinearLayout timeRow = new LinearLayout(getContext());
		timeRow.setOrientation(LinearLayout.HORIZONTAL);
		timeRow.setGravity(Gravity.CENTER_VERTICAL);
		LinearLayout.LayoutParams timeRowLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		timeRowLp.topMargin = Functions.dp2px(10);
		timeRow.setLayoutParams(timeRowLp);

		audioCurrentTimeTextView = new TextView(getContext());
		audioCurrentTimeTextView.setText("00:00");
		audioCurrentTimeTextView.setTextColor(getResources().getColor(R.color.audio_dialog_time));
		audioCurrentTimeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
		LinearLayout.LayoutParams currentLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
		audioCurrentTimeTextView.setLayoutParams(currentLp);
		timeRow.addView(audioCurrentTimeTextView);

		audioTotalTimeTextView = new TextView(getContext());
		audioTotalTimeTextView.setGravity(Gravity.END);
		audioTotalTimeTextView.setTextColor(getResources().getColor(R.color.audio_dialog_time));
		audioTotalTimeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
		audioTotalTimeTextView.setText(formatDuration(mediaPlayer.getDuration()));
		LinearLayout.LayoutParams totalLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
		audioTotalTimeTextView.setLayoutParams(totalLp);
		timeRow.addView(audioTotalTimeTextView);

		container.addView(timeRow);

		audioProgressSeekBar = new SeekBar(getContext());
		audioProgressSeekBar.setMax(mediaPlayer.getDuration());
		audioProgressSeekBar.setProgress(mediaPlayer.getCurrentPosition());
		audioProgressSeekBar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.audio_dialog_seek_progress)));
		audioProgressSeekBar.setThumbTintList(ColorStateList.valueOf(getResources().getColor(R.color.audio_dialog_seek_thumb)));
		audioProgressSeekBar.setProgressBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.audio_dialog_seek_track)));
		LinearLayout.LayoutParams seekLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		seekLp.topMargin = Functions.dp2px(4);
		audioProgressSeekBar.setLayoutParams(seekLp);
		audioProgressSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser && audioCurrentTimeTextView != null)
					audioCurrentTimeTextView.setText(formatDuration(progress));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				isUserSeeking = true;
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				if (mediaPlayer != null) {
					mediaPlayer.seekTo(seekBar.getProgress());
					if (audioCurrentTimeTextView != null)
						audioCurrentTimeTextView.setText(formatDuration(seekBar.getProgress()));
				}
				isUserSeeking = false;
			}
		});
		container.addView(audioProgressSeekBar);

		LinearLayout buttonRow = new LinearLayout(getContext());
		buttonRow.setOrientation(LinearLayout.HORIZONTAL);
		buttonRow.setGravity(Gravity.CENTER);
		LinearLayout.LayoutParams btnRowLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		btnRowLp.topMargin = Functions.dp2px(12);
		buttonRow.setLayoutParams(btnRowLp);

		final Button pauseButton = new Button(getContext());
		styleDialogButton(pauseButton, R.color.audio_dialog_btn_play, android.R.drawable.ic_media_pause);
		pauseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mediaPlayer == null)
					return;
				if (mediaPlayer.isPlaying()) {
					mediaPlayer.pause();
					setButtonIcon(pauseButton, android.R.drawable.ic_media_play);
					if (activeAudioButton != null && activeAudioSource != null)
						activeAudioButton.setText(getAudioLabel(activeAudioSource, false));
				} else {
					mediaPlayer.start();
					setButtonIcon(pauseButton, android.R.drawable.ic_media_pause);
					if (activeAudioButton != null && activeAudioSource != null)
						activeAudioButton.setText(getAudioLabel(activeAudioSource, true));
				}
			}
		});
		buttonRow.addView(pauseButton);

		Button stopButton = new Button(getContext());
		styleDialogButton(stopButton, R.color.audio_dialog_btn_stop, android.R.drawable.ic_media_pause);
		stopButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mediaPlayer == null)
					return;
				try {
					mediaPlayer.pause();
					mediaPlayer.seekTo(0);
					if (audioProgressSeekBar != null)
						audioProgressSeekBar.setProgress(0);
					if (audioCurrentTimeTextView != null)
						audioCurrentTimeTextView.setText("00:00");
					setButtonIcon(pauseButton, android.R.drawable.ic_media_play);
					if (activeAudioButton != null && activeAudioSource != null)
						activeAudioButton.setText(getAudioLabel(activeAudioSource, false));
				} catch (IllegalStateException e) {
					e.printStackTrace();
					stopCurrentAudio();
				}
			}
		});
		buttonRow.addView(stopButton);

		Button closeButton = new Button(getContext());
		styleDialogButton(closeButton, R.color.audio_dialog_btn_close, android.R.drawable.ic_menu_close_clear_cancel);
		closeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				stopCurrentAudio();
			}
		});
		buttonRow.addView(closeButton);

		container.addView(buttonRow);

		activeAudioDialog = new Dialog(getContext());
		activeAudioDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		activeAudioDialog.setContentView(container);
		activeAudioDialog.setCancelable(true);
		activeAudioDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				if (mediaPlayer != null)
					stopCurrentAudio();
			}
		});
		activeAudioDialog.show();
		Window window = activeAudioDialog.getWindow();
		if (window != null) {
			window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
			window.setGravity(Gravity.BOTTOM);
			window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
			WindowManager.LayoutParams params = window.getAttributes();
			params.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
			params.dimAmount = 0f;
			params.horizontalMargin = 0f;
			params.width = WindowManager.LayoutParams.MATCH_PARENT;
			window.setAttributes(params);
			View decorView = window.getDecorView();
			if (decorView != null)
				decorView.setPadding(0, 0, 0, 0);
			window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
			window.setDimAmount(0f);
			window.setWindowAnimations(android.R.style.Animation_Dialog);
		}
		startProgressUpdater();
	}

	private GradientDrawable createDialogPanelBackground() {
		GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] {
				getResources().getColor(R.color.audio_dialog_panel_start), getResources().getColor(R.color.audio_dialog_panel_end) });
		drawable.setCornerRadii(new float[] { Functions.dp2px(20), Functions.dp2px(20), Functions.dp2px(20),
				Functions.dp2px(20), 0, 0, 0, 0 });
		drawable.setStroke(Functions.dp2px(1), getResources().getColor(R.color.audio_dialog_panel_stroke));
		return drawable;
	}

	private void styleDialogButton(Button button, int bgColorRes, int iconRes) {
		button.setAllCaps(false);
		button.setText("");
		button.setMinWidth(0);
		int horizontal = Functions.dp2px(12);
		int vertical = Functions.dp2px(10);
		button.setPadding(horizontal, vertical, horizontal, vertical);
		GradientDrawable buttonBg = new GradientDrawable();
		buttonBg.setColor(getResources().getColor(bgColorRes));
		buttonBg.setCornerRadius(Functions.dp2px(16));
		button.setBackgroundDrawable(buttonBg);
		setButtonIcon(button, iconRes);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.leftMargin = Functions.dp2px(4);
		lp.rightMargin = Functions.dp2px(4);
		button.setLayoutParams(lp);
	}

	private void setButtonIcon(Button button, int iconRes) {
		android.graphics.drawable.Drawable icon = getResources().getDrawable(iconRes);
		if (icon != null) {
			icon.mutate().setColorFilter(getResources().getColor(R.color.audio_dialog_btn_icon), PorterDuff.Mode.SRC_IN);
			button.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
			button.setCompoundDrawablePadding(0);
		}
	}

	private void startProgressUpdater() {
		stopProgressUpdater();
		progressUpdater = new Runnable() {
			@Override
			public void run() {
				if (mediaPlayer == null)
					return;
				if (!isUserSeeking && audioProgressSeekBar != null) {
					int currentPosition = mediaPlayer.getCurrentPosition();
					audioProgressSeekBar.setProgress(currentPosition);
					if (audioCurrentTimeTextView != null)
						audioCurrentTimeTextView.setText(formatDuration(currentPosition));
				}
				progressHandler.postDelayed(this, 250);
			}
		};
		progressHandler.post(progressUpdater);
	}

	private void stopProgressUpdater() {
		if (progressUpdater != null)
			progressHandler.removeCallbacks(progressUpdater);
		progressUpdater = null;
	}

	private String formatDuration(int durationMs) {
		int totalSeconds = Math.max(0, durationMs / 1000);
		int minutes = totalSeconds / 60;
		int seconds = totalSeconds % 60;
		String minText = minutes < 10 ? "0" + minutes : String.valueOf(minutes);
		String secText = seconds < 10 ? "0" + seconds : String.valueOf(seconds);
		return minText + ":" + secText;
	}

	private String getFileName(String src) {
		if (src == null)
			return "";
		int slashIndex = src.lastIndexOf("/");
		if (slashIndex >= 0 && slashIndex + 1 < src.length())
			return src.substring(slashIndex + 1);
		return src;
	}

	private String getImagePath(String content, int idx) {
		idx = content.indexOf(SRC_PATH, idx + 1);
		int start = idx + SRC_PATH.length();
		int end = content.indexOf("'", start + 1);
		return content.substring(start, end);
	}

	private boolean isHtmlContent(String content) {
		if (content == null)
			return false;
		return content.contains(_TAG);
	}

	public void setTypeface(Typeface tf) {
		this.typeface = tf;
		int cCount = getChildCount();
		for (int i = 0; i < cCount; i++) {
			View v = getChildAt(i);
			if (TextView.class.isAssignableFrom(v.getClass())) {
				((TextView) v).setTypeface(tf);
			}
		}
	}

	public void setTextColor(int color) {
		this.textColor = color;
		int cCount = getChildCount();
		for (int i = 0; i < cCount; i++) {
			View v = getChildAt(i);
			if (TextView.class.isAssignableFrom(v.getClass())) {
				((TextView) v).setTextColor(color);
			}
		}
	}

	public void setLineSpacing(float add, float mult) {
		this.add = add;
		this.mult = mult;
		int cCount = getChildCount();
		for (int i = 0; i < cCount; i++) {
			View v = getChildAt(i);
			if (TextView.class.isAssignableFrom(v.getClass())) {
				((TextView) v).setLineSpacing(add, mult);
			}
		}
	}

	public void setTextSize(float size) {
		this.textSize = size;
		int cCount = getChildCount();
		for (int i = 0; i < cCount; i++) {
			View v = getChildAt(i);
			if (TextView.class.isAssignableFrom(v.getClass())) {
				((TextView) v).setTextSize(size);
			}
		}
	}

	public Layout getLayout() {
		if (defaultTextView == null)
			createDefaultTextView();
		return defaultTextView.getLayout();
	}

	private void createDefaultTextView() {
		if (defaultTextView != null)
			return;
		defaultTextView = new TextView(getContext());
		addTextView(defaultTextView);
	}

	@Override
	protected void onDetachedFromWindow() {
		stopCurrentAudio();
		super.onDetachedFromWindow();
	}
}
