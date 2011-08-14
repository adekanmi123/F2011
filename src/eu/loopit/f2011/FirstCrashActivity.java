package eu.loopit.f2011;

import android.content.Intent;
import dk.bregnvig.formula1.client.domain.ClientDriver;

public class FirstCrashActivity extends DriverActivity {

	@Override
	ClientDriver[] getDrivers() {
		return new ClientDriver[] {getF2011Application().getBid().getFirstCrash()};
	}

	@Override
	Intent getNextIntent() {
		//return new Intent(this, PolePositionActivity.class);
		return null;
	}

	@Override
	Intent getPreviousIntent() {
		Intent intent = new Intent(this, SelectedDriverActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		return intent;
	}

	@Override
	String getActivityTitle() {
		return getString(R.string.first_crash_title);
	}

}
