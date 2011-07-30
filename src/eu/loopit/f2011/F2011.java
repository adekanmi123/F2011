package eu.loopit.f2011;

import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import dk.bregnvig.formula1.client.domain.ClientRace;
import dk.bregnvig.formula1.client.domain.ClientSeason;
import eu.loopit.f2011.util.RestHelper;

public class F2011 extends Activity {

	public static final String TAG = F2011.class.getSimpleName();
	private ClientSeason season;
	private TextView seasonName;
	private TextView message;
	private Button participateButton;
	private boolean publicMessageFailed;
	private SimpleAdapter driverAdapter;
	private static User user;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		resetCredentials();
		setContentView(R.layout.main);
		user = new User(getApplicationContext());
		seasonName = (TextView) findViewById(R.id.seasonName);
		message = (TextView) findViewById(R.id.message);
		participateButton = (Button) findViewById(R.id.participate);
		printPrefs(getPreferences());
	}

	@Override
	protected void onResume() {
		super.onResume();
		getGameData();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settings:
			startActivity(new Intent(this, Preferences.class));
			return true;
		case R.id.exit:
			finish();
			return true;
		case R.id.refresh:
			getGameData();
			return true;
		}
		return false;
	}

	public void setSeason(ClientSeason season) {
		this.season = season;
		ClientRace currentRace = season.getCurrentRace();
		if (currentRace == null) {
			message.setText(getString(R.string.game_season_closed));
		} else if (currentRace.isWaiting()) {
			message.setText(getString(R.string.game_waiting, currentRace.getName()));
		} else if (currentRace.isClosed()) {
			message.setText(getString(R.string.game_closed, currentRace.getName()));
		} else if (currentRace.isOpened()) {
			if (currentRace.isParticipant()) {
				message.setText(getString(R.string.game_already_played, currentRace.getName()));
			} else {
				participateButton.setVisibility(View.VISIBLE);
			}
		}
	}

	private void resetCredentials() {
		SharedPreferences preference = getPreferences();
		if (preference.getBoolean(Preferences.REMEMBER_ME, true) == false) {
			Log.i(TAG, "Reseting credentials");
			SharedPreferences.Editor editor = preference.edit();
			editor.putString(Preferences.PLAYER_NAME, "");
			editor.putString(Preferences.AUTORIZATION_TOKEN, "");
			editor.commit();
		} else {
			Log.i(TAG, "Reusing credentials");
		}
	}

	private SharedPreferences getPreferences() {
		return PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	}

	public static void printPrefs(SharedPreferences preferences) {
		Map<String, ?> prefs = preferences.getAll();
		Log.i(TAG, "Number of prefs: " + prefs.size());
		for (Map.Entry<String, ?> entry : prefs.entrySet()) {
			Log.i(TAG, entry.getKey() + ":" + entry.getValue());
		}
	}
	
	private void getGameData() {
		if ("".equals(seasonName.getText())) {
			new GetSeasonNameTask().execute();
		} else if (user.isLoggedIn() && season == null) {
			new GetSeasonTask().execute();
		}
	}

	private class GetSeasonTask extends AsyncTask<Void, Void, ClientSeason> {

		@Override
		protected ClientSeason doInBackground(Void... params) {
			if (publicMessageFailed) return null;
			RestHelper<ClientSeason> helper = new RestHelper<ClientSeason>(getPreferences().getString(Preferences.AUTORIZATION_TOKEN, ""));
			try {
				return season = helper.getJSONData("/season", ClientSeason.class);
			} catch (Exception e) {
				Log.e(TAG, "Unable to fetch season from server.", e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(ClientSeason result) {
			if (result != null) {
				setSeason(result);
			} else {
				if (publicMessageFailed == false) {
					new AlertDialog.Builder(F2011.this)
					.setTitle(R.string.error_label)
					.setMessage(R.string.get_season_failure)
					.setCancelable(true)
					.show();
				}
			}
		}
	}

	private class GetSeasonNameTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			RestHelper<String> helper = new RestHelper<String>(getPreferences().getString(Preferences.AUTORIZATION_TOKEN, ""));
			try {
				return helper.getJSONData("/season-name", String.class);
			} catch (Exception e) {
				Log.e(TAG, "Unable to fetch season from server.", e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				seasonName.setText(result);
				if (user.isLoggedIn()) {
					new GetSeasonTask().execute();
				} else {
					Log.i(TAG, "Launching settings, since the user is not logged in");
					startActivity(new Intent(F2011.this, Preferences.class));
				}
			} else {
				publicMessageFailed = true;
				new AlertDialog.Builder(F2011.this)
					.setTitle(R.string.error_label)
					.set
					.setMessage(R.string.get_season_failure)
					.setPositiveButton(R.string.label_ok, new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
						}
					}).show();
			}
		}
	}

}