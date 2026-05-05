package ir.baran.bookPack;

import ir.baran.baranBook.R;
import ir.baran.book.SplashPage;
import ir.baran.framework.database.DataBaseHelper;
import ir.baran.framework.utilities.Functions;
import ir.baran.framework.utilities.MyConfig;
import android.content.Intent;
import android.view.ViewGroup;

public class FirstSplashPage extends SplashPage {

	@Override
	protected void initProject(ViewGroup llSplash) {
		MyConfig._AppName = Functions.getApplicationName(this);
		DataBaseHelper.DB_NAME = "dt";
		BgMediaPlayer.getInstance(this);
		DataBaseHelper.databaseHelper(this, 2000);
		MyConfig._AssetManager = getAssets();
//		Intent intent = new Intent(this, GamePage.class);
//
//		startActivity(intent);
	}

}
