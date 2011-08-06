package eu.loopit.f2011;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import dk.bregnvig.formula1.client.domain.ClientDriver;
import eu.loopit.f2011.util.Validator;


public class GridActivity extends BaseActivity {
	
	public static final String TAG = WelcomeActivity.class.getSimpleName();
	private ListView list;
	private ClientDriver[] drivers;
	private Validator validator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.grid);
		list = (ListView) findViewById(R.id.drivers);
		validator = new Validator(this);
		TextView title = (TextView) findViewById(R.id.title);
		title.setText(R.string.grid_title);
		drivers = getF2011Application().getBid().getGrid();
		final DriverAdapter adapter = new DriverAdapter(this, R.layout.driver, drivers);
		 
		//get reference to the ListView and tell it to use our TasksAdapter
		list.setAdapter(adapter);
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
				getF2011Application().getDriverDialog(GridActivity.this, new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						drivers[position] = getF2011Application().getActiveDriver(which);
						adapter.notifyDataSetChanged();
					}
				}).show();
			}
		});
		Button previous = (Button) findViewById(R.id.previous);
		previous.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				GridActivity.this.finish();
			}
		});
		Button next = (Button) findViewById(R.id.next);
		next.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				boolean result = validator.validateAllSelected(drivers); 
				if (result) result = validator.validateDrivers(drivers);
				if (result == true) {
					//startActivity(new Intent(GridActivity.this, FastestDriver.class));
				}
			}
		});
	}
}
