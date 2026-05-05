package ir.baran.framework.utilities;

import ir.baran.framework.R;
import ir.baran.framework.forms.Form;
import android.R.attr;
import android.app.Activity;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.graphics.drawable.StateListDrawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MyConfig {
	public static String _AppName;
	public static AssetManager _AssetManager;
	public static Activity _FirstForm;
	// -------------------------------------------
	private static String _DefaultTypeface;
	private static Typeface _DTypeface = null;
	private static int[] _DefaultGradiant = null;
	private static int[] _DefaultPressGradiant = null;

	private static int _DefaultStrokeColor = 0;
	private static int _DefaultStrokeWidth = 0;
	private static int _DefaultCornerRadios = 0;
	private static int _DefaultTextColor = 0;
	private static int _DefaultTextSize = 0;
	private static int _DefaultHighlightBackColor = 0xff8fceff;

	//
	// -------------------------------------------
	private static String _NextTypeface = "STEHRAN.ttf";
	private static Typeface _NTypeface = null;
	private static int[] _NextGradiant = null;
	private static int[] _NextPressGradiant = null;
	private static int _NextStrokeColor;
	private static int _NextStrokeWidth;
	private static int _NextCornerRadios;
	private static int _NextTextColor;
	private static int _NextTextSize;
	private static int _NextHighlightTextColor;
	private static int _DefaultHighlightTextColor;

	public static void loadDefaults() {
		if (_DefaultGradiant == null) {
			Resources res = _FirstForm.getResources();
			_DefaultGradiant = new int[] {
					res.getColor(R.color.DefaultGradiantStart),
					res.getColor(R.color.DefaultGradiantCenter),
					res.getColor(R.color.DefaultGradiantEnd) };
			//
			_DefaultPressGradiant = new int[] {
					res.getColor(R.color.DefaultPressGradiantStart),
					res.getColor(R.color.DefaultPressGradiantCenter),
					res.getColor(R.color.DefaultPressGradiantEnd) };
			//
			_NextGradiant = new int[] {
					res.getColor(R.color.NextGradiantStart),
					res.getColor(R.color.NextGradiantCenter),
					res.getColor(R.color.NextGradiantEnd) };
			//
			_NextPressGradiant = new int[] {
					res.getColor(R.color.NextPressGradiantStart),
					res.getColor(R.color.NextPressGradiantCenter),
					res.getColor(R.color.NextPressGradiantEnd) };
			//
			_DefaultTypeface = res.getString(R.string.DefaultTypeface);
			//
			_NextTypeface = res.getString(R.string.NextTypeface);
			//
			_DefaultStrokeColor = res.getColor(R.color.DefaultStrokeColor);
			_DefaultStrokeWidth = (int) res
					.getDimension(R.dimen.DefaultStrokeWidth);
			_DefaultCornerRadios = (int) res
					.getDimension(R.dimen.DefaultCornerRadios);
			_DefaultTextColor = res.getColor(R.color.DefaultTextColor);
			_DefaultTextSize = (int) res.getDimension(R.dimen.DefaultTextSize);
			_DefaultHighlightTextColor = res
					.getColor(R.color.DefaultHighlightTextColor);
			//
			_NextStrokeColor = res.getColor(R.color.NextStrokeColor);
			_NextStrokeWidth = (int) res.getDimension(R.dimen.NextStrokeWidth);
			_NextCornerRadios = (int) res
					.getDimension(R.dimen.NextCornerRadios);
			_NextTextColor = res.getColor(R.color.NextTextColor);
			_NextTextSize = (int) res.getDimension(R.dimen.NextTextSize);
			_NextHighlightTextColor = res
					.getColor(R.color.NextHighlightTextColor);
			_DefaultHighlightBackColor = res
					.getColor(R.color.NextHighlightBackColor);

			//

		}
	}

	public static Typeface getDefaultTypeface() {
		if (_DTypeface == null) {
			try {
				_DTypeface = Typeface.createFromAsset(_AssetManager,
						_DefaultTypeface);
			} catch (Exception e) {
				_DefaultTypeface = "Vazir-Light.ttf";
				_DTypeface = Typeface.createFromAsset(_AssetManager,
						_DefaultTypeface);
			}
		}
		return _DTypeface;
	}

	public static GradientDrawable getDefaultGradiant() {
		GradientDrawable g = new GradientDrawable(Orientation.TOP_BOTTOM,
				_DefaultGradiant);
		return g;

	}

	public static GradientDrawable getDefaultPressGradiant() {
		GradientDrawable g = new GradientDrawable(Orientation.TOP_BOTTOM,
				_DefaultPressGradiant);
		return g;

	}

	public static StateListDrawable getDefaultStatesDrawable() {
		StateListDrawable sd = new StateListDrawable();

		GradientDrawable gd1 = getDefaultPressGradiant();
		gd1.setCornerRadius(_DefaultCornerRadios);
		gd1.setStroke(_DefaultStrokeWidth, _DefaultStrokeColor);
		sd.addState(new int[] { attr.state_pressed }, gd1);

		GradientDrawable gd2 = getDefaultGradiant();
		gd2.setCornerRadius(_DefaultCornerRadios);
		gd2.setStroke(_DefaultStrokeWidth, _DefaultStrokeColor);
		sd.addState(new int[] {}, gd2);
		return sd;
	}

	public static GradientDrawable getNextGradiant() {
		GradientDrawable g = new GradientDrawable(Orientation.TOP_BOTTOM,
				_NextGradiant);
		return g;

	}

	public static GradientDrawable getNextPressGradiant() {
		GradientDrawable g = new GradientDrawable(Orientation.TOP_BOTTOM,
				_NextPressGradiant);
		return g;

	}

	public static Drawable getNextBackgound() {
		StateListDrawable sd = new StateListDrawable();

		GradientDrawable gd1 = getNextPressGradiant();
		gd1.setCornerRadius(_DefaultCornerRadios);
		gd1.setStroke(_DefaultStrokeWidth, _DefaultStrokeColor);
		sd.addState(new int[] { attr.state_pressed }, gd1);

		GradientDrawable gd2 = getNextGradiant();
		gd2.setCornerRadius(_DefaultCornerRadios);
		gd2.setStroke(_DefaultStrokeWidth, _DefaultStrokeColor);
		sd.addState(new int[] {}, gd2);

		return sd;
	}

	public static Typeface getNextTypeface() {
		if (_NTypeface == null) {
			try {
				_NTypeface = Typeface.createFromAsset(_AssetManager, _NextTypeface);
			} catch (Exception e) {
				_NextTypeface = "Vazir-Light.ttf";
				_NTypeface = Typeface.createFromAsset(_AssetManager, _NextTypeface);
			}

		}
		return _NTypeface;
	}

	// --------------------------------------------

	public static Drawable getWindowBackground() {
		GradientDrawable gd = getDefaultPressGradiant();
		gd.setCornerRadius(_DefaultCornerRadios);
		gd.setStroke(_DefaultStrokeWidth, _DefaultStrokeColor);
		return gd;
	}

	public static Drawable getButtonBackground() {
		StateListDrawable sd = new StateListDrawable();

		GradientDrawable gd1 = getDefaultPressGradiant();
		gd1.setCornerRadius(_DefaultCornerRadios);
		gd1.setStroke(_DefaultStrokeWidth, _DefaultStrokeColor);
		sd.addState(new int[] { attr.state_pressed }, gd1);

		GradientDrawable gd2 = getDefaultGradiant();
		gd2.setCornerRadius(_DefaultCornerRadios);
		gd2.setStroke(_DefaultStrokeWidth, _DefaultStrokeColor);
		sd.addState(new int[] {}, gd2);

		return sd;
	}

	public static void initTextView(TextView lbl) {
		lbl.setTypeface(getDefaultTypeface());
		lbl.setTextColor(_DefaultTextColor);
		// lbl.setTextSize(TypedValue.COMPLEX_UNIT_DIP, _DefaultTextSize);
	}

	public static void setStyle(View ll) {
		if (TextView.class.isAssignableFrom(ll.getClass())) {
			TextView lbl = (TextView) ll;
			lbl.setTypeface(getDefaultTypeface());
			//lbl.setTextSize(TypedValue.COMPLEX_UNIT_DIP, _DefaultTextSize);
			ConfigurationUtils.setDefaultTextSize(lbl);
			return;
		}
		if (ViewGroup.class.isAssignableFrom(ll.getClass())) {
			int count = ((ViewGroup) ll).getChildCount();
			for (int i = 0; i < count; i++) {
				View v = ((ViewGroup) ll).getChildAt(i);
				setStyle(v);
			}
		}
	}

	public static int getDefaultHighlightBackColor() {
		return _DefaultHighlightBackColor;
	}

	public static int getDefaultTextColor() {
		return _DefaultTextColor;
	}

	public static int getNextHighlightTextColor() {
		return _NextHighlightTextColor;
	}

	public static int getDefaultHighlightTextColor() {
		return _DefaultHighlightTextColor;
	}

	public static float getDefaultTextSize() {
		return _DefaultTextSize;
	}

	public static int getNextTextColor() {
		return _NextTextColor;
	}

	public static float getDefaultCornerRadios() {
		return _DefaultCornerRadios;
	}

	public static int getDefaultStrokeWidth() {
		return _DefaultStrokeWidth;
	}

	public static int getDefaultStrokeColor() {
		return _DefaultStrokeColor;
	}
}
