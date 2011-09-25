package eu.loopit.f2011;

import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;

import dk.bregnvig.formula1.client.domain.ClientDriver;
import dk.bregnvig.formula1.client.domain.ClientRace;
import eu.loopit.f2011.util.RestHelper;

public class WelcomeActivity extends BaseActivity {

	public static final String TAG = WelcomeActivity.class.getSimpleName();
	public static final String FORCE_REFRESH = "forceRefresh";
	private TextView seasonName;
	private TextView race;
	private TextView message;
	private Button participateButton;
	private boolean publicMessageFailed;
	private ProgressDialog waitingDialog;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		seasonName = (TextView) findViewById(R.id.seasonName);
		race = (TextView) findViewById(R.id.race);
		message = (TextView) findViewById(R.id.message);
		participateButton = (Button) findViewById(R.id.participate);
		participateButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(WelcomeActivity.this, GridActivity.class));
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		getGameData();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (intent.getBooleanExtra(FORCE_REFRESH, false) == true) {
			Log.i(TAG, "Forcing refresh of game data");
			refreshGameData();
		}
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
			refreshGameData();
			return true;
		}
		return false;
	}

	public void setCurrentRace(ClientRace currentRace) {
		getF2011Application().setCurrentRace(currentRace);
		message.setVisibility(View.VISIBLE);
		participateButton.setVisibility(View.GONE);
		if (currentRace == null) {
			message.setText(getString(R.string.game_season_closed));
		} else if (currentRace.isWaiting()) {
			message.setText(getString(R.string.game_waiting, currentRace.getName()));
		} else if (currentRace.isClosed() && currentRace.isParticipant() == false) {
			message.setText(getString(R.string.game_closed, currentRace.getName()));
		} else if (currentRace.isOpened() && currentRace.isParticipant() == false) {
			message.setVisibility(View.GONE);
			participateButton.setVisibility(View.VISIBLE);
		} else if (currentRace.isParticipant() == true) {
			message.setText(getString(R.string.game_already_played, currentRace.getName()));
		}
		if (currentRace != null) {
			race.setText(currentRace.getName());
		}
	}
	
	private void getGameData() {
		if (getF2011Application().getCurrentRace() == null) {
			if (waitingDialog == null || waitingDialog.isShowing() == false) {
				waitingDialog = ProgressDialog.show(this, "", getString(R.string.loading_label), true);
			}
		}
		if ("".equals(seasonName.getText())) {
			new GetSeasonNameTask().execute();
		} else if (getF2011Application().isLoggedIn() && getF2011Application().getCurrentRace() == null) {
			new InitiateRaceTask().execute();
		}
	}
	

	private void refreshGameData() {
		participateButton.setVisibility(View.INVISIBLE);
		getF2011Application().setCurrentRace(null);
		getGameData();
	}

	private class InitiateRaceTask extends AsyncTask<Void, Void, ClientRace> {

		@Override
		protected ClientRace doInBackground(Void... params) {
			if (publicMessageFailed) return null;
			RestHelper helper = new RestHelper(getF2011Application());
			try {
				getF2011Application().setActiveDrivers(helper.getJSONData("/race/drivers", ClientDriver.class, new TypeToken<List<ClientDriver>>(){}.getType()));
				return helper.getJSONData("/race", ClientRace.class);
			} catch (Exception e) {
				Log.e(TAG, "Unable to fetch season from server.", e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(ClientRace result) {
			if (result != null) {
				setCurrentRace(result);
				if (waitingDialog != null && waitingDialog.isShowing()) waitingDialog.dismiss();
			} else {
				if (publicMessageFailed == false) {
					new AlertDialog.Builder(WelcomeActivity.this)
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
			RestHelper helper = new RestHelper(getF2011Application());
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
				seasonName.setText(result.trim());
				if (getF2011Application().isLoggedIn()) {
					new InitiateRaceTask().execute();
				} else {
					Log.i(TAG, "Launching settings, since the user is not logged in");
					startActivity(new Intent(WelcomeActivity.this, Preferences.class));
				}
			} else {
				publicMessageFailed = true;
				new AlertDialog.Builder(WelcomeActivity.this)
					.setTitle(R.string.error_label)
					.setMessage(R.string.get_season_failure)
					.setPositiveButton(R.string.label_ok, new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
					}
					}).show();
			}
		}
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (velocityX < 0) {
    		Intent intent = new Intent(this, WbcActivity.class);
    		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        	startActivity(intent);
        	return true;
        }
        return false;
	}

}