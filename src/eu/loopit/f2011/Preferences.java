package eu.loopit.f2011;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity {
	
	private IntentFilter filter = new IntentFilter(F2011Application.INTENT_LOGGED_IN);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		registerReceiver(new LoggedInReceiver(), filter);
	}
	
	private class LoggedInReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			F2011Application app = (F2011Application) getApplication();
			if (app.isLoggedIn()) finish();
		}
		
	}
}
