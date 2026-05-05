package ir.baran.framework.utilities;

import ir.baran.framework.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ConfigurationUtils {
    private static Typeface labelFont = null;
    private static Typeface labelFontBold = null;
    private static Typeface labelFont3 = null;
    private static String _deviceId;
    private static float _THRESHOLD_TEXT_SIZE = Float.MIN_VALUE;
    public final static float START_SIZE_PERCENT = 2f;
    public static float START_SIZE = 16f * START_SIZE_PERCENT;

    public static Typeface getLabelFont(Context page) {
        if (labelFont == null)
            labelFont = Typeface.createFromAsset(page.getAssets(), "font/font.ttf");
        return labelFont;
    }

    public static Typeface getLabelFont2(Context page) {
        if (labelFontBold == null)
            labelFontBold = Typeface.createFromAsset(page.getAssets(), "font/font2.ttf");
        return labelFontBold;
    }

    public static Typeface getLabelFont3(Context page) {
        if (labelFont3 == null)
            labelFont3 = Typeface.createFromAsset(page.getAssets(), "font/font3.ttf");
        return labelFont3;
    }

    public static Typeface getCommonLabelFont(Context page, String fontName) {
        if (labelFont3 == null)
            labelFont3 = Typeface.createFromAsset(page.getAssets(), "font/" + fontName);
        return labelFont3;
    }

    public static void initTypefaces(ViewGroup llVg) {
        Typeface tf = getLabelFont(llVg.getContext());
        initTypefaces(llVg, tf);
    }

    public static void initTypefaces(ViewGroup llVg, Typeface tf) {
        initTypefaces(llVg, tf, Typeface.NORMAL);
    }

    public static void initTypefacesAndSize(ViewGroup llVg, Typeface tf, float fontSize) {
        int size = llVg.getChildCount();
        for (int i = 0; i < size; i++) {
            View v = llVg.getChildAt(i);
            if (ViewGroup.class.isAssignableFrom(v.getClass())) {
                initTypefacesAndSize((ViewGroup) v, tf, fontSize);
            } else if (TextView.class.isAssignableFrom(v.getClass())) {
                ((TextView) v).setTypeface(tf);
                ((TextView) v).setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
            }
        }
    }

    public static void initTypefaces(ViewGroup llVg, Typeface tf, int typeface) {
        int size = llVg.getChildCount();
        for (int i = 0; i < size; i++) {
            View v = llVg.getChildAt(i);
            if (ViewGroup.class.isAssignableFrom(v.getClass())) {
                initTypefaces((ViewGroup) v, tf, typeface);
            } else if (TextView.class.isAssignableFrom(v.getClass())) {
                ((TextView) v).setTypeface(tf);
            }
        }
    }

    public static void initFontSize(ViewGroup llVg, float textsize) {
        int size = llVg.getChildCount();
        for (int i = 0; i < size; i++) {
            View v = llVg.getChildAt(i);
            if (ViewGroup.class.isAssignableFrom(v.getClass())) {
                initFontSize((ViewGroup) v, textsize);
            } else if (TextView.class.isAssignableFrom(v.getClass())) {
                ((TextView) v).setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
            }
        }
    }

    public static void initTextColor(ViewGroup llVg, int textColor) {
        int size = llVg.getChildCount();
        for (int i = 0; i < size; i++) {
            View v = llVg.getChildAt(i);
            if (ViewGroup.class.isAssignableFrom(v.getClass())) {
                initTextColor((ViewGroup) v, textColor);
            } else if (TextView.class.isAssignableFrom(v.getClass())) {
                ((TextView) v).setTextColor(textColor);
            }
        }
    }

    public static void initBGColor(ViewGroup llVg, int bgColor) {
        llVg.setBackgroundColor(bgColor);
        int size = llVg.getChildCount();
        for (int i = 0; i < size; i++) {
            View v = llVg.getChildAt(i);
            if (ViewGroup.class.isAssignableFrom(v.getClass())) {
                initTextColor((ViewGroup) v, bgColor);
            } else
                v.setBackgroundColor(bgColor);
        }
    }

    public static void initOnTouch(ViewGroup llVg, OnTouchListener imageOnTouch) {
        int size = llVg.getChildCount();
        llVg.setOnTouchListener(imageOnTouch);
        for (int i = 0; i < size; i++) {
            View v = llVg.getChildAt(i);
            if (ViewGroup.class.isAssignableFrom(v.getClass())) {
                initOnTouch((ViewGroup) v, imageOnTouch);
            } else
                v.setOnTouchListener(imageOnTouch);

        }

    }

    public static void restartApp(Context ac) {
        if (ac == null)
            return;
        Intent LaunchIntent = ac.getPackageManager().getLaunchIntentForPackage(ac.getPackageName());
        ac.startActivity(LaunchIntent);
    }

    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    public static String getdeviceId(Context context) {

        // if (_deviceId != null && _deviceId.length() > 0)
        // return _deviceId;
        // final TelephonyManager tm = (TelephonyManager)
        // context.getSystemService(Context.TELEPHONY_SERVICE);
        //
        // final String tmDevice, tmSerial, androidId;
        // tmDevice = "" + tm.getDeviceId();
        // tmSerial = "" + tm.getSimSerialNumber();
        // androidId = ""
        // +
        // android.provider.Settings.Secure.getString(context.getContentResolver(),
        // android.provider.Settings.Secure.ANDROID_ID);
        //
        // UUID deviceUuid = new UUID(androidId.hashCode(), ((long)
        // tmDevice.hashCode() << 32)
        // | tmSerial.hashCode());
        // _deviceId = deviceUuid.toString();
        // return _deviceId;

        // get internal android device id :

        if (_deviceId != null && _deviceId.length() > 0)
            return _deviceId;
        _deviceId = android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        return _deviceId;

    }

    public static String getVesionName(Context context) {
        PackageInfo pInfo;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
        } catch (NameNotFoundException e) {
            return "0";
        }
    }

    /**
     * @param ac
     * @return
     */
    public static float getTextSizeDiferent(Activity ac) {
        if (_THRESHOLD_TEXT_SIZE != Float.MIN_VALUE) {
            return _THRESHOLD_TEXT_SIZE;
        }
        if (ac == null) {
            return 0;
        }
        START_SIZE = Functions.dx2dp((int) ac.getResources().getDimension(R.dimen.DefaultTextSize));
        int screenWidth = getScreenWidth(ac);
        int dp2pxW = Functions.dp2px(screenWidth);
        if ((dp2pxW / screenWidth) > 2) {
            dp2pxW = screenWidth * 2;
        }
        float screenW = Math.max(dp2pxW, screenWidth);
        final float TARGET_WIDTH = (float) (579.0f * screenW / 1440f);// 681.0,1440f

        float hi = 100f;
        float lo = 2f;
        final float threshold = 0.001f; // How close we have to be

        TextPaint paint = new TextPaint();

        String text = "درصورتیکهبخواهیدیکخروجیاولیهازمحصولیکهتاکنونایجادکردهاید";
        Typeface font = Typeface.createFromAsset(ac.getAssets(), "BNazanin.ttf");
        if (font == null) {
            System.out.println("Error ************ : font/BNazanin.ttf NOT EXISTS");
            _THRESHOLD_TEXT_SIZE = 0;
            return 0;
        }
        paint.setTypeface(font);
        float w = 0;

        // paint.setTextSize(30f);
        // w = paint.measureText(text);

        Rect bounds = new Rect();
        while ((hi - lo) > threshold) {
            float size = (hi + lo) / 2;
            paint.setTextSize(size);

            paint.getTextBounds(text, 0, text.length(), bounds);
            w = bounds.width();// paint.measureText(text)
            // System.out.println(">>>>>> w :" + w + " size :" + size + " lo:" +
            // lo);
            if (w >= TARGET_WIDTH)
                hi = size; // too big
            else
                lo = size; // too small
        }

        _THRESHOLD_TEXT_SIZE = lo / 30f;// - START_SIZE
        if (_THRESHOLD_TEXT_SIZE <= 0.60f)
            _THRESHOLD_TEXT_SIZE = 0.60f;

        return _THRESHOLD_TEXT_SIZE;
    }

    public static void setDefaultTextSize(TextView tv) {
        float dif = getTextSizeDiferent(MyConfig._FirstForm);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, START_SIZE * dif);
    }

    public static void setDefaultTextSizeTF(TextView tv, Context context) {
        float dif = getTextSizeDiferent(MyConfig._FirstForm);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, START_SIZE * dif);
        tv.setTypeface(getLabelFont(context));
    }

    public static void openUrl(String url, Context ac) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        ac.startActivity(intent);
    }

    public static void openUrl(Context ac) {
        openUrl("https://baranapp.ir/products-fa?fromapp=1", ac);
    }

    public static void showMessage(final String message, Activity activity) {
        ConfigurationUtils.showMessage(message, 2000, activity);
    }

    public static void showMessage(final String message, final int time, final Activity ac) {
        ac.runOnUiThread(() -> {
            Toast toast = new Toast(ac);
            toast.setDuration(time);
            LinearLayout ll = new LinearLayout(ac);
            toast.setView(ll);
            ll.setBackgroundColor(0x00000000);
            TextView view = new TextView(ac);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            int margin = Functions.dp2px(15);
            view.setPadding(margin, margin, margin, margin);

            lp.setMargins(margin, margin, margin, margin);
            lp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
            view.setGravity(lp.gravity);
            view.setLayoutParams(lp);
            ll.addView(view);
            GradientDrawable gradiant = MyConfig.getDefaultPressGradiant();
            gradiant.setCornerRadius(MyConfig.getDefaultCornerRadios());
            gradiant.setStroke(MyConfig.getDefaultStrokeWidth(), MyConfig.getDefaultStrokeColor());

            view.setBackgroundDrawable(gradiant);
            view.setText(message);
            view.setTextColor(MyConfig.getDefaultTextColor());
            toast.show();
        });
    }
}
