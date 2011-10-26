package eu.loopit.f2011;

import android.content.Intent;
import dk.bregnvig.formula1.client.domain.ClientDriver;
import eu.loopit.f2011.welcome.WelcomeActivity;

public class GridActivity extends DriverActivity {
	
	
	@Override
	Intent getNextIntent() {
		return new Intent(this, FastestDriverActivity.class);
	}

	@Override
	Intent getPreviousIntent() {
		Intent intent = new Intent(this, WelcomeActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		return intent;
	}

	@Override
	String getActivityTitle() {
		return getString(R.string.grid_title);
	}

	@Override
	ClientDriver[] getDrivers() {
		return getF2011Application().getBid().getGrid();
	}

}
