package eu.loopit.f2011;

import java.util.Map;

import eu.loopit.f2011.library.DrawableManager;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.TextView;

public class F2011 extends Activity {
	
	public static final String TAG = F2011.class.getSimpleName();
	private DrawableManager manager = new DrawableManager();
	private TextView text;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resetCredentials();
        setContentView(R.layout.main);
        text = (TextView) findViewById(R.id.swipe);
        printPrefs(getPreferences());
    }
    
    

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch(event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			text.setText("Started. X: " + event.getX() + " Y: " + event.getY());
			break;
		case MotionEvent.ACTION_UP:
			text.append("Ended. X: " + event.getX() + " Y: " + event.getY());
			break;
		}
		return super.onTouchEvent(event);
	}



	@Override
	protected void onResume() {
		super.onResume();
        if (getPreferences().getBoolean(Preferences.LOGGED_IN, false) == false) {
        	Log.i(TAG, "Launching settings, since the user is not logged in");
        	startActivity(new Intent(this, Preferences.class));
        }
        ImageView image = (ImageView) findViewById(R.id.driver);
        //manager.fetchDrawableOnThread("http://formel1.loopit.eu/drivers/Michael%20Schumacher.png", image);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		startActivity(new Intent(this, Preferences.class));
		return false;
	}
	
	private void resetCredentials() {
		SharedPreferences preference = getPreferences();
		if (preference.getBoolean(Preferences.REMEMBER_ME, true) == false) {
			Log.i(TAG, "Reseting credentials");
			SharedPreferences.Editor editor = preference.edit(); 
			editor.putString(Preferences.PLAYER_NAME, "");
			editor.putString(Preferences.PASSWORD, "");
			editor.commit();
		} else {
			Log.i(TAG, "Reusing credentials");
		}
	}
	
	private SharedPreferences getPreferences() {
//		return getSharedPreferences("eu.loopit.f2011.Preferences", MODE_PRIVATE);
		return PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	}
	
	public static void printPrefs(SharedPreferences preferences) {
		Map<String, ?> prefs = preferences.getAll();
		Log.i(TAG, "Number of prefs: " + prefs.size());
		for (Map.Entry<String, ?> entry : prefs.entrySet()) {
			Log.i(TAG, entry.getKey() + ":" + entry.getValue());
		}
	}
}