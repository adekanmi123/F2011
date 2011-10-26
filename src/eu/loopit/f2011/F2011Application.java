package eu.loopit.f2011;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import dk.bregnvig.formula1.client.domain.ClientDriver;
import dk.bregnvig.formula1.client.domain.ClientPlayer;
import dk.bregnvig.formula1.client.domain.ClientRace;
import dk.bregnvig.formula1.client.domain.bid.ClientBid;
import dk.bregnvig.formula1.client.domain.wbc.ClientWBCEntry;
import eu.loopit.f2011.util.Base64Coder;
import eu.loopit.f2011.util.CommunicationException;
import eu.loopit.f2011.util.RestHelper;
import eu.loopit.f2011.welcome.WelcomeActivity;

public class F2011Application extends Application implements OnSharedPreferenceChangeListener{
	
	
	private static final String BID_SUFFIX = ".bid";
	private static final String TAG =  F2011Application.class.getSimpleName();
	private static final String PLAYER_NAME = "player_name";
	private static final String PASSWORD = "password";
	private static final String AUTORIZATION_TOKEN = "token";
	private static final String LOGGED_IN = "logged_in";
	private static final String REMEMBER_ME = "remember_me";
	
	public static final int GRID =  0;
	
	public static final String INTENT_LOGGED_IN = "eu.loopit.f2011.LOGGED_IN";
	
	private ClientDriver NO_DRIVER;
	
	private SharedPreferences prefs;
	private ClientRace currentRace;
	private List<ClientDriver> activeDrivers = Collections.emptyList();
	private List<ClientWBCEntry> wbcEntries = new ArrayList<ClientWBCEntry>();
	private String[] driverNames;
	private ClientBid bid;

	
	@Override
	public void onCreate() {
		super.onCreate();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		resetCredentials();
		prefs.registerOnSharedPreferenceChangeListener(this);
		printPrefs();
		
		NO_DRIVER = new ClientDriver();
		NO_DRIVER.setName(getString(R.string.no_driver_title));
	}
	
	public List<ClientDriver> getActiveDrivers() {
		return activeDrivers;
	}

	public void setActiveDrivers(List<ClientDriver> activeDrivers) {
		this.activeDrivers = activeDrivers;
		if (activeDrivers != null) {
			driverNames = new String[activeDrivers.size()];
			for (int i = 0; i < activeDrivers.size(); i++) {
				driverNames[i] = activeDrivers.get(i).getName();
			}
		}
	}
	
	public ClientDriver getActiveDriver(int index) {
		if (activeDrivers == null ||  index+1 > activeDrivers.size()) {
			String message = "ActiveDrivers either not set, or the index is out of bound";
			Log.e(TAG, message);
			throw new IllegalStateException(message);
		}
		return activeDrivers.get(index);
	}
	
	public String getDriverImageURL(ClientDriver driver) {
		return getBaseURL() + "/mobile/drivers/" + driver.getName().replace(" ", "%20")+".png";
	}
	
	public AlertDialog getDriverDialog(Context context, OnClickListener listener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(getString(R.string.select_driver_title))
			.setItems(driverNames, listener);
		return builder.create();
	}
	
	public ClientBid getBid() {
		validateRaceSelected();
		if (bid != null) return bid;
		
		ObjectInputStream ois = null;
		try {
			FileInputStream fis = openFileInput(getCurrentRace().getId()+BID_SUFFIX);
			ois = new ObjectInputStream(fis);
			bid = (ClientBid) ois.readObject();
		} catch (FileNotFoundException e) {
			Log.i(TAG, "No previous bid was found. Creating a new bid");
			initializeBid();
		} catch (Exception e) {
			Log.e(TAG, "could not read bid, even though the file existed in the phone", e);
		} finally {
			if (ois != null) {
				try {
					ois.close();
				} catch (IOException ioe) {
					Log.e(TAG, "Could not close ois", ioe);
				}
			}
		}
		return bid;
	}
	
	public void removeBid() {
		validateRaceSelected();
		if (bid == null) return;
		
		deleteFile(getCurrentRace().getId()+BID_SUFFIX);
		bid = null;
	}
	
	public void persistBid() {
		try {
			validateRaceSelected();
		} catch (IllegalStateException ignore) {
			return;
		}
		if (bid == null) return;
		
		ObjectOutputStream oos = null;
		try {
			Log.i(TAG, "Persisting bid");
			FileOutputStream fos = openFileOutput(getCurrentRace().getId()+BID_SUFFIX, MODE_PRIVATE);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(bid);
		} catch (IOException e) {
			Log.e(TAG, "Could not persist bid", e);
		} finally {
			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
					Log.e(TAG, "Could not close the bid file", e);
				}
			}
		}
	}

	private void validateRaceSelected() {
		if (getCurrentRace() == null) throw new IllegalStateException("If there is no race, there is no bid!");
	}

	private void initializeBid() {
		bid = new ClientBid();
		bid.setGrid(getDriverArray(6));
		bid.setFastestLap(getDriver(0));
		bid.setPodium(getDriverArray(3));
		bid.setSelectedDriver(new int[] {1,1});
		bid.setFirstCrash(getDriver(0));
		bid.setPolePositionTime(0);
	}
	
	private ClientDriver[] getDriverArray(int size) {
		ClientDriver[] drivers = new ClientDriver[size];
		for (int i = 0; i < drivers.length; i++) {
			drivers[i] = getDriver(i);
		}
		return drivers;
	}
	
	private void resetCredentials() {
		if (getRememberMe() == false) {
			Log.i(TAG, "Reseting credentials");
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString(PLAYER_NAME, "");
			editor.putString(PASSWORD, "");
			editor.putString(AUTORIZATION_TOKEN, "");
			editor.commit();
		} else {
			Log.i(TAG, "Reusing credentials");
		}
	}
	
	public ClientDriver getNoDriver() {
		return NO_DRIVER;
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (PLAYER_NAME.equals(key) || PASSWORD.equals(key)) {
			login();
		}
	}

	public String getPlayerName() {
		return prefs.getString(PLAYER_NAME, "");
	}
	
	public String getPassword() {
		return prefs.getString(PASSWORD, "");
	}
	
	public String getToken() {
		return prefs.getString(AUTORIZATION_TOKEN, "");
	}
	
	public boolean getRememberMe() {
		return prefs.getBoolean(REMEMBER_ME, false);
	}
	
	public String getBaseURL() {
		if (isTest()) {
			return "http://10.0.2.2:8080/f2007";
		}
		return "http://formel1.loopit.eu";
	}

	public boolean isTest() {
		return "sdk".equals(Build.PRODUCT);
	}
	
	public boolean isLoggedIn() {
		return prefs.getBoolean(LOGGED_IN, false);
	}
	
	public List<ClientWBCEntry> getWbcEntries() {
		return wbcEntries;
	}

	public void setWbcEntries(List<ClientWBCEntry> wbcEntries) {
		this.wbcEntries = wbcEntries;
	}
	
	private void printPrefs() {
		Map<String, ?> preferences = prefs.getAll();
		Log.i(TAG, "Number of prefs: " + preferences.size());
		for (Map.Entry<String, ?> entry : preferences.entrySet()) {
			Log.i(TAG, entry.getKey() + ":" + entry.getValue());
		}
	}

	private void login() {
		if (isBlankOrNull(getPlayerName()) || isBlankOrNull(getPassword())) {
			prefs.edit().putBoolean(LOGGED_IN, false).commit();
		} else {
			Log.i(F2011Application.TAG, "Performing login");
			LoginTask task = new LoginTask();
			task.execute(getPlayerName(), getPassword());
		}
	}
	
	private boolean isBlankOrNull(String value) {
		return value == null || value.length() == 0;
	}
	
	private ClientDriver getDriver(int location) {
		return isTest() && activeDrivers.size() > location ? activeDrivers.get(location) : NO_DRIVER;
	}
	
	private void displayPlayerResult(String text) {
		Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
	}

	private class LoginTask extends AsyncTask<String, Void, ClientPlayer> {
		
		private RestHelper helper = new RestHelper(F2011Application.this);
		private Exception storedException;
		
		@Override
		protected ClientPlayer doInBackground(String... params) {
			ClientPlayer player = null;
			try {
				player = helper.getJSONData(String.format("/login/%s/%s", params[0], params[1]), ClientPlayer.class);
				String authorizationToken = Base64Coder.encodeString(player.getPlayername()+":"+player.getToken());
				prefs.edit().putBoolean(LOGGED_IN, true).putString(AUTORIZATION_TOKEN, authorizationToken).commit();
			} catch (Exception e) {
				storedException = e;
				Log.e(WelcomeActivity.TAG, "Could not login player", e);
			}
			return player;
		}

		@Override
		protected void onPostExecute(ClientPlayer result) {
			if (result != null) {
				displayPlayerResult(getString(R.string.logon_success, result.getFirstName()));
				sendBroadcast(new Intent(INTENT_LOGGED_IN));
			} else if (storedException.getClass() == CommunicationException.class) {
				displayPlayerResult(getString(R.string.communication_failure));
			} else {
				displayPlayerResult(getString(R.string.logon_failure));
			}
		}
	}
	
	public ClientRace getCurrentRace() {
		return currentRace;
	}

	public void setCurrentRace(ClientRace currentRace) {
		this.currentRace = currentRace;
	}

}
