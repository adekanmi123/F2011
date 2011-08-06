package eu.loopit.f2011;

import android.app.Activity;

public abstract class BaseActivity extends Activity {

	
	F2011Application getF2011Application() {
		return (F2011Application) super.getApplication();
	}

}
