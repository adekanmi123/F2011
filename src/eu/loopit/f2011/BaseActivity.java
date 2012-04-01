package eu.loopit.f2011;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;

public abstract class BaseActivity extends Activity {

	protected ProgressDialog waitingDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		getF2011Application().persistBid();
		closeLoadingDialog();
	}

	public F2011Application getF2011Application() {
		return (F2011Application) super.getApplication();
	}
	
	protected void openLoadingDialog(String text) {
		waitingDialog = ProgressDialog.show(this, "", text, true);
	}
	
	protected void closeLoadingDialog() {
		if (waitingDialog != null && waitingDialog.isShowing()) waitingDialog.dismiss();
	}

}
