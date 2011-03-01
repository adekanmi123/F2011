package eu.loopit.f2011;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;

public class Preferences extends PreferenceActivity {
	
	public static final String PLAYER_NAME = "player_name";
	public static final String PASSWORD = "password";
	public static final String LOGGED_IN = "logged_in";
	public static final String REMEMBER_ME = "remember_me";
	
	private EditTextPreference passwordPreference;
	private EditTextPreference playerName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		playerName = (EditTextPreference) getPreferenceScreen().findPreference(PLAYER_NAME);
		playerName.setSummary(playerName.getText());
		playerName.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				playerName.setSummary(newValue.toString());
				loginPlayer(newValue.toString(), passwordPreference.getText());
				return true;
			}
		});
		passwordPreference = (EditTextPreference) getPreferenceScreen().findPreference(PASSWORD);
		setPasswordHint(passwordPreference.getText());
		passwordPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				setPasswordHint(newValue.toString());
				loginPlayer(playerName.getText(), newValue.toString());
				return true;
			}
		});
		Log.i(F2011.TAG, "Logged in: " + getPreferenceManager().getSharedPreferences().getBoolean(Preferences.LOGGED_IN, false));
	}
	
	private void setPasswordHint(String password) {
		passwordPreference.setSummary(password != null && password.length() != 0 ? getString(R.string.password_hint): "");
	}
	
	private void loginPlayer(String playerName, String password) {
		//String playerName = 
		Log.i(F2011.TAG, "Player name: " + playerName + " Password: " + (password != null && password.length() == 0 ? "none" : "********"));
		if (playerName.length() == 0 || password == null || password.length() == 0) {
			getPreferenceManager().getSharedPreferences().edit().putBoolean(LOGGED_IN, false).commit();
			return;
		}
		
		//TODO Login the user
		playerSuccessfullyLoggedIn();
	}
	
	private void playerSuccessfullyLoggedIn() {
		getPreferenceManager().getSharedPreferences().edit().putBoolean(LOGGED_IN, true).commit();
		displayPlayerResult(getString(R.string.logon_success, playerName.getText()));
	}
	
	private void displayPlayerResult(String text) {
		Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
	}
}
