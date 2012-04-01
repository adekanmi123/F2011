package eu.loopit.f2011;

import java.util.HashMap;
import java.util.Map;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import dk.bregnvig.formula1.client.domain.bid.ClientBid;
import eu.loopit.f2011.util.RestHelper;

public class BidPlayerActivity extends BaseActivity {

	public static final String NAME = "NAME";
	public static final String PLAYER_NAME = "PLAYER_NAME";
	
	public static final String TAG = BidPlayerActivity.class.getSimpleName();
	public static final Map<String, ClientBid> bids = new HashMap<String, ClientBid>();
	
	private TextView title;
	private ListView list;
	private BidAdapter adapter;
	private ProgressDialog waitingDialog;
	private String playerName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wbc);
		title = (TextView) findViewById(R.id.title);
		list = (ListView) findViewById(R.id.entries);
		adapter = new BidAdapter(this);

		playerName = getIntent().getExtras().getString(PLAYER_NAME);
		list.setAdapter(adapter);
		title.setText(getIntent().getExtras().getString(NAME));
		if (bids.containsKey(playerName)) {
			adapter.setBid(bids.get(playerName));
		} else {
			waitingDialog = ProgressDialog.show(this, "", getString(R.string.loading_wbc_player_label, getIntent().getExtras().getString(NAME)), true);
			new BidTask().execute();
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (waitingDialog != null && waitingDialog.isShowing()) waitingDialog.dismiss();
	}

	private class BidTask extends AsyncTask<Void, Void, ClientBid> {

		@Override
		protected ClientBid doInBackground(Void... params) {
			RestHelper helper = new RestHelper(getF2011Application());
			return helper.getJSONData("/race/"+BidPlayerActivity.this.getF2011Application().getCurrentRace().getId()+"/bid/" + BidPlayerActivity.this.playerName, ClientBid.class);
		}

		@Override
		protected void onPostExecute(ClientBid result) {
			if (result == null) return;
			BidPlayerActivity.bids.put(result.getPlayer().getPlayername(), result);
			adapter.setBid(result);
			waitingDialog.dismiss();
		}
	}	
}
