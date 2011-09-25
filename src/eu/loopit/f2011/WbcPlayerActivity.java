package eu.loopit.f2011;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;

import dk.bregnvig.formula1.client.domain.wbc.ClientWBCEntry;
import eu.loopit.f2011.util.RestHelper;

public class WbcPlayerActivity extends BaseActivity {

	static final String NAME = "NAME";
	static final String PLAYER_NAME = "PLAYER_NAME";
	
	public static final String TAG = WbcPlayerActivity.class.getSimpleName();
	private TextView title;
	private ListView list;
	private List<ClientWBCEntry> entries = new ArrayList<ClientWBCEntry>(); 
	private WbcPlayerAdapter adapter;
	private ProgressDialog waitingDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wbc);
		title = (TextView) findViewById(R.id.title);
		list = (ListView) findViewById(R.id.entries);
		adapter = new WbcPlayerAdapter(this, R.layout.wbc_entry, entries, false);
		//get reference to the ListView and tell it to use our TasksAdapter
		list.setAdapter(adapter);
		title.setText(getIntent().getExtras().getString(NAME));
		waitingDialog = ProgressDialog.show(this, "", getString(R.string.loading_wbc_player_label, getIntent().getExtras().getString(NAME)), true);
		new WBCTask().execute();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (waitingDialog != null && waitingDialog.isShowing()) waitingDialog.dismiss();
	}



	private class WBCTask extends AsyncTask<Void, Void, List<ClientWBCEntry>> {

		@Override
		protected List<ClientWBCEntry> doInBackground(Void... params) {
			RestHelper helper = new RestHelper(getF2011Application());
			return helper.getJSONData("/wbc/"+getIntent().getExtras().getString(PLAYER_NAME), ClientWBCEntry.class, new TypeToken<List<ClientWBCEntry>>(){}.getType());
		}

		@Override
		protected void onPostExecute(List<ClientWBCEntry> result) {
			if (result == null || result.size() == 0) return;
			for (ClientWBCEntry entry : result) {
				adapter.add(entry);
			}
			waitingDialog.dismiss();
		}
	}	
}
