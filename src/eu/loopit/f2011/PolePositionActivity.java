package eu.loopit.f2011;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import eu.loopit.f2011.util.RestHelper;
import eu.loopit.f2011.util.Validator;

public class PolePositionActivity extends BaseActivity {
	
	private TextView minutes;
	private TextView seconds;
	private TextView thousands;
	
	private Validator validator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pole_position);
		
		minutes = (TextView) findViewById(R.id.minutes);
		seconds = (TextView) findViewById(R.id.seconds);
		thousands = (TextView) findViewById(R.id.thousands);
		
		minutes.setText("");
		seconds.setText("");
		thousands.setText("");
		
//		minutes.setOn
		
		validator = new Validator(this);
		
		Button previous = (Button) findViewById(R.id.previous);
		previous.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startActivity(getPreviousIntent());
			}
		});
		Button next = (Button) findViewById(R.id.submit);
		next.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				boolean result = validator.validateNumber(getString(R.string.label_minutes), minutes.getText().toString());
				if (result) result = validator.validateNumber(getString(R.string.label_seconds), seconds.getText().toString());
				if (result) result = validator.validateNumber(getString(R.string.label_thousands), thousands.getText().toString());
				if (result) new SubmitTask().execute();
			}
		});
	}
	
	private Intent getNextIntent() {
		Intent intent = new Intent(this, WelcomeActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		return intent;
	}

	private Intent getPreviousIntent() {
		Intent intent = new Intent(this, PodiumActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		return intent;
	}
	
	private class SubmitTask extends AsyncTask<Void, Void, Void> {
		
		private RestHelper helper = new RestHelper(getF2011Application());

		@Override
		protected Void doInBackground(Void... params) {
//			helper.getJSONData(String.format("/race"), Void.class);
//			getF2011Application().
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			startActivity(getNextIntent());
		}
		
		
	}

}
