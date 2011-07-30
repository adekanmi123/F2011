package eu.loopit.f2011;

import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;
import dk.bregnvig.formula1.client.domain.ClientPlayer;
import eu.loopit.f2011.util.CommunicationException;
import eu.loopit.f2011.util.RestHelper;

public class Preferences extends PreferenceActivity {
	
	public static final String PLAYER_NAME = "player_name";
	private static final String PASSWORD = "password";
	public static final String AUTORIZATION_TOKEN = "token";
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
				handlePreferenceChange(newValue.toString(), passwordPreference.getText());
				return true;
			}
		});
		playerName.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		    	if (actionId == EditorInfo.IME_ACTION_GO) {
		    		playerName.setText(playerName.getEditText().getText().toString());
					handlePreferenceChange(playerName.getText(), passwordPreference.getText());
		    		playerName.getDialog().dismiss();
		            return true;
		        }
		        return false;
		    }
		});
		passwordPreference = (EditTextPreference) getPreferenceScreen().findPreference(PASSWORD);
		passwordPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				handlePreferenceChange(playerName.getText(), newValue.toString());
				return true;
			}
		});
		passwordPreference.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		    	if (actionId == EditorInfo.IME_ACTION_GO) {
		    		passwordPreference.setText(passwordPreference.getEditText().getText().toString());
					handlePreferenceChange(playerName.getText(), passwordPreference.getText());
		    		passwordPreference.getDialog().dismiss();
		            return true;
		        }
		        return false;
		    }
		});
		
		
		Log.i(F2011.TAG, "Logged in: " + getPreferenceManager().getSharedPreferences().getBoolean(Preferences.LOGGED_IN, false));
	}

	private void handlePreferenceChange(String player, String password) {
		playerName.setSummary(player);
		loginPlayer(player, password);
		
	}
	
	private void loginPlayer(String playerName, String password) {
		//String playerName = 
		Log.i(F2011.TAG, "Player name: " + playerName + " Password: " + (password != null && password.length() == 0 ? "none" : "********"));
		if (playerName.length() == 0 || password == null || password.length() == 0) {
			getPreferenceManager().getSharedPreferences().edit().putBoolean(LOGGED_IN, false).commit();
			return;
		}
		Log.i(F2011.TAG, "Performing login");
		LoginTask task = new LoginTask();
		task.execute(playerName, password);
	}
	
	private void displayPlayerResult(String text) {
		Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
	}
	
	private class LoginTask extends AsyncTask<String, Void, ClientPlayer> {
		
		private RestHelper<ClientPlayer> helper = new RestHelper<ClientPlayer>();
		private Exception storedException;
		
		@Override
		protected ClientPlayer doInBackground(String... params) {
			ClientPlayer player = null;
			try {
				player = helper.getJSONData(String.format("/login/%s/%s", params[0], params[1]), ClientPlayer.class);
				String authorizationToken = Base64.encodeToString((player.getPlayername()+":"+player.getToken()).getBytes(), Base64.NO_WRAP);
				getPreferenceManager().getSharedPreferences().edit().putBoolean(LOGGED_IN, true).putString(AUTORIZATION_TOKEN, authorizationToken).commit();
			} catch (Exception e) {
				storedException = e;
				Log.e(F2011.TAG, "Could not login player", e);
			}
			return player;
		}

		@Override
		protected void onPostExecute(ClientPlayer result) {
			if (result != null) {
				displayPlayerResult(getString(R.string.logon_success, result.getFirstName()));
				Preferences.this.finish();
			} else if (storedException.getClass() == CommunicationException.class) {
				displayPlayerResult(getString(R.string.communication_failure));
			} else {
				displayPlayerResult(getString(R.string.logon_failure));
			}
		}
	}
}
