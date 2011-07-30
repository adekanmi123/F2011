package eu.loopit.f2011;

import android.content.Context;
import android.preference.PreferenceManager;

public class User {
	
	private Context context;
	
	public User(Context context) {
		this.context = context;
	}
	
	public String getPlayername() {
		return PreferenceManager.getDefaultSharedPreferences(context).getString(Preferences.PLAYER_NAME, "");
	}
	
	public boolean isLoggedIn() {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Preferences.LOGGED_IN, false);
	}
}
