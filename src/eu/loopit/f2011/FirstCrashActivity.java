package eu.loopit.f2011;

import android.content.Intent;
import dk.bregnvig.formula1.client.domain.ClientDriver;

public class FirstCrashActivity extends DriverActivity {

	private ClientDriver[] drivers = new ClientDriver[1];
	
	
	@Override
	protected void onPause() {
		updateBid();
		super.onPause();
	}

	@Override
	ClientDriver[] getDrivers() {
		drivers[0] = getF2011Application().getBid().getFirstCrash();
		return drivers;
	}

	@Override
	Intent getNextIntent() {
		updateBid();
		return new Intent(this, PolePositionActivity.class);
	}

	@Override
	Intent getPreviousIntent() {
		updateBid();
		Intent intent = new Intent(this, SelectedDriverActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		return intent;
	}

	@Override
	String getActivityTitle() {
		return getString(R.string.first_crash_title);
	}

	private void updateBid() {
		getF2011Application().getBid().setFirstCrash(drivers[0]);
	}
	
}
