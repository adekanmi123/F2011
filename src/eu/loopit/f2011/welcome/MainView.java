package eu.loopit.f2011.welcome;

import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;

import dk.bregnvig.formula1.client.domain.ClientDriver;
import dk.bregnvig.formula1.client.domain.ClientRace;
import eu.loopit.f2011.BidPlayersActivity;
import eu.loopit.f2011.GridActivity;
import eu.loopit.f2011.PageView;
import eu.loopit.f2011.Preferences;
import eu.loopit.f2011.R;
import eu.loopit.f2011.util.RestHelper;

public class MainView implements PageView {

	private WelcomeActivity owner;
	private final View mainView;
	private boolean initialized;
	private boolean hasFocus = true;
	private boolean publicMessageFailed;
	private InitiateRaceTask task;

	private Button participateButton;
	private Button playersButton;
	private TextView seasonName;
	private TextView race;
	private TextView message;

	MainView(WelcomeActivity owner, View mainView) {
		this.mainView = mainView;
		this.owner = owner;
	}

	public void initialize() {
		if (initialized == true) return;
		
		seasonName = (TextView) mainView.findViewById(R.id.seasonName);
		race = (TextView) mainView.findViewById(R.id.race);
		message = (TextView) mainView.findViewById(R.id.message);
		participateButton = (Button) mainView.findViewById(R.id.participate);
		participateButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				owner.startActivity(new Intent(owner, GridActivity.class));
			}
		});
		playersButton = (Button) mainView.findViewById(R.id.players);
		playersButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				owner.startActivity(new Intent(owner, BidPlayersActivity.class));
			}
		});
		new GetSeasonNameTask().execute();
		
		initialized = true;
	}
	
	public void load() {
		task = new InitiateRaceTask();
		displayLoadingDialog();
		task.execute();
	}

	public boolean isLoading() {
		return task == null ? false : task.getStatus() != Status.FINISHED;
	}

	public void onFocusChange(boolean hasFocus) {
		this.hasFocus = hasFocus;
		displayLoadingDialog();
	}
	
	private void displayLoadingDialog() {
		if (hasFocus && isLoading()) owner.showLoadingDialog(owner.getString(R.string.loading_label));
	}
	
	private void setCurrentRace(ClientRace currentRace) {
		owner.getF2011Application().setCurrentRace(currentRace);
		message.setVisibility(View.VISIBLE);
		participateButton.setVisibility(View.GONE);
		playersButton.setVisibility(View.GONE);
		if (currentRace == null) {
			message.setText(owner.getString(R.string.game_season_closed));
		} else if (currentRace.isWaiting()) {
			message.setText(owner.getString(R.string.game_waiting, currentRace.getName()));
		} else if (currentRace.isClosed() && currentRace.isParticipant() == false) {
			message.setText(owner.getString(R.string.game_closed, currentRace.getName()));
		} else if (currentRace.isOpened() && currentRace.isParticipant() == false) {
			message.setVisibility(View.GONE);
			participateButton.setVisibility(View.VISIBLE);
		} else if (currentRace.isParticipant() == true) {
			playersButton.setVisibility(View.VISIBLE);
			message.setVisibility(View.GONE);
		}
		if (currentRace != null) {
			race.setText(currentRace.getName());
		}
	}

	private class InitiateRaceTask extends AsyncTask<Void, Void, ClientRace> {

		@Override
		protected ClientRace doInBackground(Void... params) {
			if (publicMessageFailed) return null;
			RestHelper helper = new RestHelper(owner.getF2011Application());
			try {
				owner.getF2011Application().setActiveDrivers(helper.getJSONData("/race/drivers", ClientDriver.class, new TypeToken<List<ClientDriver>>(){}.getType()));
				ClientRace result = helper.getJSONData("/race", ClientRace.class); 
				return result;
			} catch (Exception e) {
				Log.e(WelcomeActivity.TAG, "Unable to fetch season from server.", e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(ClientRace result) {
			if (result != null) {
				setCurrentRace(result);
			} else {
				if (publicMessageFailed == false) {
					new AlertDialog.Builder(owner)
					.setTitle(R.string.error_label)
					.setMessage(R.string.get_season_failure)
					.setCancelable(true)
					.show();
				}
			}
			owner.dismissLoadingDialog();
			owner.pageLoaded();
		}
	}
	
	private class GetSeasonNameTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			RestHelper helper = new RestHelper(owner.getF2011Application());
			try {
				return helper.getJSONData("/season-name", String.class);
			} catch (Exception e) {
				Log.e(WelcomeActivity.TAG, "Unable to fetch season from server.", e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				seasonName.setText(result.trim());
				if (owner.getF2011Application().isLoggedIn()) {
					load();
				} else {
					Log.i(WelcomeActivity.TAG, "Launching settings, since the user is not logged in");
					owner.startActivity(new Intent(owner, Preferences.class));
				}
			} else {
				publicMessageFailed = true;
				new AlertDialog.Builder(owner)
					.setTitle(R.string.error_label)
					.setMessage(R.string.get_season_failure)
					.setPositiveButton(R.string.label_ok, new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
					}
					}).show();
			}
		}
	}


}
