package ir.baran.framework.utilities;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class AssetAudioUtils {
	private AssetAudioUtils() {
	}

	public static void setDataSourceFromAssets(Context context, AssetManager assetManager, MediaPlayer player, String assetPath)
			throws IOException {
		try {
			AssetFileDescriptor afd = assetManager.openFd(assetPath);
			try {
				player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
				return;
			} finally {
				afd.close();
			}
		} catch (IOException e) {
			if (context == null)
				throw e;
			File cachedAudioFile = copyAssetToCache(context, assetManager, assetPath);
			player.setDataSource(cachedAudioFile.getAbsolutePath());
		}
	}

	private static File copyAssetToCache(Context context, AssetManager assetManager, String assetPath) throws IOException {
		File audioCacheDir = new File(context.getCacheDir(), "asset_audio");
		if (!audioCacheDir.exists() && !audioCacheDir.mkdirs())
			throw new IOException("Unable to create audio cache directory");

		String safeName = assetPath.replace('/', '_').replace('\\', '_');
		File cachedAudioFile = new File(audioCacheDir, safeName);
		if (cachedAudioFile.exists() && cachedAudioFile.length() > 0)
			return cachedAudioFile;

		InputStream inputStream = null;
		FileOutputStream outputStream = null;
		try {
			inputStream = assetManager.open(assetPath);
			outputStream = new FileOutputStream(cachedAudioFile);
			byte[] buffer = new byte[8192];
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}
			outputStream.flush();
			return cachedAudioFile;
		} catch (IOException e) {
			if (cachedAudioFile.exists())
				cachedAudioFile.delete();
			throw e;
		} finally {
			if (inputStream != null)
				inputStream.close();
			if (outputStream != null)
				outputStream.close();
		}
	}
}
