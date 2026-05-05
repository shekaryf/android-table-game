package ir.baran.framework.utilities;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

public class ImageUtils {
	private static Hashtable<String, Bitmap> images = new Hashtable<String, Bitmap>();

	public static Bitmap getBitmapFromAssets(String path) {
		if (path == null || path.length() <= 0)
			return null;
		Bitmap bmp = images.get(path);
		if (bmp != null)
			return bmp;
		try {
			InputStream is = MyConfig._AssetManager.open(path);
			try {
				bmp = BitmapFactory.decodeStream(is);
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
				disposeImages();
				Functions.restartApp();
			}
			is.close();
			if (bmp == null || path == null)
				return null;
			images.put(path, bmp);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bmp;
	}

	public static Bitmap getBitmapFromBase64(String content) {
		byte[] data = Base64.decode(content, Base64.DEFAULT);
		if (data != null) {
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			Bitmap bmp = BitmapFactory.decodeStream(bis);
			try {
				bis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			String key = "bmpIDIMG" + images.size();
			images.put(key, bmp);
			return bmp;
		}
		return null;
	}

	public static void disposeImages() {
		for (Bitmap element : images.values()) {
			element.recycle();
		}
		images = new Hashtable<String, Bitmap>();
	}

	public static void disposeImage(String key) {
		try {
			System.out.println("dispose image : " + key);
			Bitmap bitmap = images.get(key);
			if (bitmap != null) {
				bitmap.recycle();
				images.remove(key);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
