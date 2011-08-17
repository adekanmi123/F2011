package eu.loopit.f2011;

import android.content.Intent;
import dk.bregnvig.formula1.client.domain.ClientDriver;

public class PodiumActivity extends DriverActivity {

	@Override
	ClientDriver[] getDrivers() {
		return getF2011Application().getBid().getPodium();
	}

	@Override
	Intent getNextIntent() {
		return new Intent(this, SelectedDriverActivity.class);
	}

	@Override
	Intent getPreviousIntent() {
		Intent intent = new Intent(this, FastestDriverActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		return intent;
	}

	@Override
	String getActivityTitle() {
		return getString(R.string.podium_title);
	}

}
