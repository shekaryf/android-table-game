package ir.baran.bookPack;

import java.io.IOException;
import java.util.Random;
import java.util.Vector;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import ir.baran.framework.utilities.AssetAudioUtils;

public class BgMediaPlayer implements OnCompletionListener {
	private static BgMediaPlayer instance;
	private Context context;
	private MediaPlayer _mediaPlayer;
	private String mp3PlayPath;
	private Vector<String> mp3List = new Vector<String>();

	public BgMediaPlayer(Context context) {
		this.context = context;
		try {
			String[] list = context.getAssets().list("");
			for (String string : list) {
				if (string.endsWith(".mp3"))
					mp3List.addElement(string);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static BgMediaPlayer getInstance(Context context) {
		if (instance == null)
			instance = new BgMediaPlayer(context);
		return instance;
	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		playSound();
	}

	public void onDestroy() {
		if (mp3List.size() <= 0)
			return;
		stopSound();
	}

	public void stopSound() {
		if (mp3List.size() <= 0)
			return;

		if (this._mediaPlayer != null) {
			try {
				this._mediaPlayer.stop();
			} catch (Exception e) {
			}
		}
	}

	public void playSound() {
		if (mp3List.size() <= 0)
			return;
		// if (mp3PlayPath == null)
		int idx = new Random().nextInt(mp3List.size());
		mp3PlayPath = mp3List.elementAt(idx);
		_mediaPlayer = new MediaPlayer();
		_mediaPlayer.setOnCompletionListener(this);
		try {
			AssetAudioUtils.setDataSourceFromAssets(context, context.getAssets(), _mediaPlayer, mp3PlayPath);
			_mediaPlayer.prepare();

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			_mediaPlayer.start();

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

}
