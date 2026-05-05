package ir.baran.book;

import ir.baran.framework.R;
import ir.baran.framework.forms.Form;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public abstract class SplashPage extends Form {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		baseOnCreate(savedInstanceState);
		setContentView(R.layout.splash);
		new Thread(new Runnable() {

			@Override
			public void run() {
				ViewGroup llSplash = (ViewGroup) findViewById(R.id.llSplash);
				initProject(llSplash);
				finish();
			}

		}).start();

	}

	protected abstract void initProject(ViewGroup llSplash);

	@Override
	public void initContent(LinearLayout llContent) {
		// TODO Auto-generated method stub

	}

}
