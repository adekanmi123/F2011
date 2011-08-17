package eu.loopit.f2011;

import dk.bregnvig.formula1.client.domain.ClientDriver;
import eu.loopit.f2011.library.BitmapManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class SelectedDriverActivity extends BaseActivity {
	
	private Spinner startsFrom;
	private Spinner endsAt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.selected_driver);
		
		startsFrom = (Spinner) findViewById(R.id.startsAt);
		endsAt = (Spinner) findViewById(R.id.endsAt);
		
		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, getPositions());
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		OnItemSelectedListener positionListener = new PositionSelected();
		startsFrom.setAdapter(adapter);
		startsFrom.setOnItemSelectedListener(positionListener);
		startsFrom.setSelection(getF2011Application().getBid().getSelectedDriver()[0]-1);
		endsAt.setAdapter(adapter);
		endsAt.setOnItemSelectedListener(positionListener);
		endsAt.setSelection(getF2011Application().getBid().getSelectedDriver()[1]-1);
		BitmapManager manager = new BitmapManager(this);
		ClientDriver selectedDriver = getF2011Application().getCurrentRace().getSelectedDriver();
		manager.fetchBitmapOnThread(
				getF2011Application().getDriverImageURL(selectedDriver), 
				(ImageView) findViewById(R.id.driverImage), null);
		TextView driverName = (TextView) findViewById(R.id.driverName);
		driverName.setText(selectedDriver.getName());
		
		Button previous = (Button) findViewById(R.id.previous);
		previous.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startActivity(getPreviousIntent());
			}
		});
		Button next = (Button) findViewById(R.id.next);
		next.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startActivity(getNextIntent());
			}
		});

	}
	
	private String[] getPositions() {
		String[] positions = new String[getF2011Application().getActiveDrivers().size()];
		for (int i = 0; i < positions.length; i++) {
			positions[i] = Integer.toString(i+1);
		}
		return positions;
	}
	
	Intent getNextIntent() {
		return new Intent(this, FirstCrashActivity.class);
	}

	Intent getPreviousIntent() {
		Intent intent = new Intent(this, PodiumActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		return intent;
	}
	
	private class PositionSelected implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			int index = parent == startsFrom ? 0 : 1;
			getF2011Application().getBid().getSelectedDriver()[index] = position + 1;
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};

}
