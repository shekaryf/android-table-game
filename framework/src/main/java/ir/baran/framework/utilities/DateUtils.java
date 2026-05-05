package ir.baran.framework.utilities;

import ir.baran.framework.utilities.date.PersianDate;

import java.util.Locale;

public class DateUtils {
	public static String getCurrentShamsidate() {
		Locale loc = new Locale("en_US");
		PersianDate sc = new PersianDate();
		return String.valueOf(sc.year) + "/"
				+ String.format(loc, "%02d", sc.month) + "/"
				+ String.format(loc, "%02d", sc.date);
	}

	public static String getCurrentShamsiFullDate() {
		Locale loc = new Locale("en_US");
		PersianDate sc = new PersianDate();
		return String.valueOf(sc.year) + "/"
				+ String.format(loc, "%02d", sc.month) + "/"
				+ String.format(loc, "%02d", sc.date) + " "
				+ String.format(loc, "%02d", sc.hours) + ":"
				+ String.format(loc, "%02d", sc.minutes);
	}
}
