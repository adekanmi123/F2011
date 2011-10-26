package eu.loopit.f2011;

import android.app.Activity;
import android.os.Bundle;
import android.view.GestureDetector;

public abstract class BaseActivity extends Activity {

	GestureDetector detector;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		getF2011Application().persistBid();
	}

	public F2011Application getF2011Application() {
		return (F2011Application) super.getApplication();
	}
}
