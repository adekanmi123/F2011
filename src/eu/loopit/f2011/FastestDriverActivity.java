package eu.loopit.f2011;

import android.content.Intent;
import dk.bregnvig.formula1.client.domain.ClientDriver;

public class FastestDriverActivity extends DriverActivity {

	@Override
	ClientDriver[] getDrivers() {
		return new ClientDriver[] {getF2011Application().getBid().getFastestLap()};
	}

	@Override
	Intent getNextIntent() {
		return new Intent(this, PodiumActivity.class);
	}

	@Override
	Intent getPreviousIntent() {
		Intent intent = new Intent(this, GridActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		return intent;
	}

	@Override
	String getActivityTitle() {
		return getString(R.string.fastestLap_title);
	}

}
