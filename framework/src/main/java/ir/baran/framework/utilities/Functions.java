package ir.baran.framework.utilities;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Vibrator;
import android.telephony.gsm.SmsManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

public class Functions {
	public static void startUrl(String url, Context src) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		src.startActivity(intent);
	}

	public static String getApplicationName(Context context) {
		int stringId = context.getApplicationInfo().labelRes;
		return context.getString(stringId);
	}

	public static int getNextSpaceIndex(String str, int index) {
		int length = str.length();
		for (int i = index; i < length; i++) {
			char charAt = str.charAt(i);
			if (charAt == ' ' || charAt == '\r' || charAt == '\n')
				return i;
		}
		return length;
	}

	public static int getPreSpaceIndex(String str, int index) {
		for (int i = index; i >= 0; i--) {
			char charAt = str.charAt(i);
			if (charAt == ' ' || charAt == '\r' || charAt == '\n')
				return i;
		}
		return 0;
	}

	public static String getNormalString(String str) {
		return str.replace('ی', 'ي').replace('ى', 'ي').replace('ك', 'ک');
	}

	public static int dp2px(int dp) {
		Resources res = MyConfig._FirstForm.getResources();
		DisplayMetrics metic = res.getDisplayMetrics();
		return (int) ((float) dp * (metic.densityDpi / 160f));
	}

	public static int dx2dp(int px) {
		Resources res = MyConfig._FirstForm.getResources();
		DisplayMetrics metic = res.getDisplayMetrics();
		return (int) ((float) px / (metic.densityDpi / 160f));
	}

	public static String readAllText(String path) {
		StringBuilder sb = new StringBuilder();
		try {
			InputStream is = MyConfig._AssetManager.open(path);
			byte[] buf = new byte[2048];
			int count = is.read(buf);

			do {
				ByteArrayInputStream bis = new ByteArrayInputStream(buf);
				Reader r = new InputStreamReader(bis, "UTF-8");
				while (true) {
					int c = r.read();
					if (c < 0)
						break;
					sb.append((char) c);

				}
				r.close();
				bis.close();
				count = is.read(buf);
			} while (count > 0);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return sb.toString();
	}

	public static void share(Activity activity, String title, String subject, String text) {

		Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
		shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, text);
		activity.startActivity(Intent.createChooser(shareIntent, title));
	}

	// ----------------------------
	public static int toInt32(String value) {
		try {
			return Integer.valueOf(value);
		} catch (Exception e) {
			return -1;
		}
	}

	public static void vibrate(final Activity f) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Vibrator v = (Vibrator) f.getSystemService(Context.VIBRATOR_SERVICE);
					// long[] pattern = { 0, 100, 1000, 300, 200, 100, 500, 200,
					// 100 };
					// v.vibrate(pattern, -1);
					v.vibrate(500);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public static void playSoundFromAssets(String path) {
		try {
			MediaPlayer player = new MediaPlayer();
			AssetAudioUtils.setDataSourceFromAssets(MyConfig._FirstForm, MyConfig._AssetManager, player, path);
			player.prepare();
			player.start();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// String myVersion = android.os.Build.VERSION.RELEASE; // e.g. myVersion :=
	// "1.6"
	// int sdkVersion = android.os.Build.VERSION.SDK_INT; // e.g. sdkVersion :=
	// 8;
	public static void restartApp(Activity activity) {
		if (activity == null)
			return;
		Intent LaunchIntent = activity.getPackageManager().getLaunchIntentForPackage(activity.getPackageName());
		activity.startActivity(LaunchIntent);
	}

	public static void restartApp() {
		if (MyConfig._FirstForm == null)
			return;
		Intent LaunchIntent = MyConfig._FirstForm.getPackageManager().getLaunchIntentForPackage(MyConfig._FirstForm.getPackageName());
		MyConfig._FirstForm.startActivity(LaunchIntent);
	}

	public static String removeHtml(String content) {
		if (content == null)
			return "";
		return content.replaceAll("\\<[^>]*>", "");
	}

	@SuppressWarnings("deprecation")
	public static void sendSMS(String phoneNumber, String message, final Activity form) {
		SmsManager smsManager = SmsManager.getDefault();

		String SENT = "SMS_SENT";
		String DELIVERED = "SMS_DELIVERED";

		SmsManager sms = SmsManager.getDefault();
		ArrayList<String> parts = sms.divideMessage(message);
		int messageCount = parts.size();

		Log.i("Message Count", "Message Count: " + messageCount);

		ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>();
		ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();

		PendingIntent sentPI = PendingIntent.getBroadcast(form, 0, new Intent(SENT), 0);
		PendingIntent deliveredPI = PendingIntent.getBroadcast(form, 0, new Intent(DELIVERED), 0);

		for (int j = 0; j < messageCount; j++) {
			sentIntents.add(sentPI);
			deliveryIntents.add(deliveredPI);
		}

		// ---when the SMS has been sent---
		form.registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					Toast.makeText(form.getBaseContext(), "SMS sent", Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					Toast.makeText(form.getBaseContext(), "Generic failure", Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
					Toast.makeText(form.getBaseContext(), "No service", Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_NULL_PDU:
					Toast.makeText(form.getBaseContext(), "Null PDU", Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					Toast.makeText(form.getBaseContext(), "Radio off", Toast.LENGTH_SHORT).show();
					break;
				}
			}
		}, new IntentFilter(SENT));

		// ---when the SMS has been delivered---
		form.registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode()) {

				case Activity.RESULT_OK:
					Toast.makeText(form.getBaseContext(), "SMS delivered", Toast.LENGTH_SHORT).show();
					break;
				case Activity.RESULT_CANCELED:
					Toast.makeText(form.getBaseContext(), "SMS not delivered", Toast.LENGTH_SHORT).show();
					break;
				}
			}
		}, new IntentFilter(DELIVERED));
		smsManager.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
		/*
		 * sms.sendMultipartTextMessage(phoneNumber, null, parts, sentIntents,
		 * deliveryIntents);
		 */
	}

	public static void copyFromAssets(Context context, String fromAssetFileName, String toPath, String tofileName) throws IOException {

		// Open your local db as the input stream
		InputStream myInput = context.getAssets().open(fromAssetFileName);
		File path = new File(toPath);
		if (!path.exists())
			path.mkdirs();
		// Path to the just created empty db
		String outFileName = toPath + tofileName;

		// Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(outFileName);

		// transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer)) > 0) {
			myOutput.write(buffer, 0, length);
		}

		// Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();

	}

}
