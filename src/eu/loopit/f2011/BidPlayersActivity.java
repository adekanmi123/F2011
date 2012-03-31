package eu.loopit.f2011;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;

import dk.bregnvig.formula1.client.domain.ClientPlayer;
import eu.loopit.f2011.util.RestHelper;
import eu.loopit.f2011.welcome.MainView;

public class BidPlayersActivity extends BaseActivity {

	public static final String NAME = "NAME";
	public static final String PLAYER_NAME = "PLAYER_NAME";
	
	public static final String TAG = BidPlayersActivity.class.getSimpleName();
	private TextView title;
	private ListView list;
	private BidAdapter adapter;
	private List<String> names;
	private List<ClientPlayer> players;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wbc);
		title = (TextView) findViewById(R.id.title);
		list = (ListView) findViewById(R.id.entries);
		adapter = new BidAdapter(this);

		list.setAdapter(adapter);
		title.setText(getIntent().getExtras().getString(NAME));
		openLoadingDialog(getString(R.string.players_bid_title));
		new PlayersTask().execute();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		closeLoadingDialog();
	}

	private class PlayersTask extends AsyncTask<Void, Void, List<ClientPlayer>> {

		@Override
		protected List<ClientPlayer> doInBackground(Void... params) {
			RestHelper helper = new RestHelper(getF2011Application());
			List<ClientPlayer> players = helper.getJSONData("/race?players", ClientPlayer.class, new TypeToken<List<ClientPlayer>>(){}.getType());
		}

		@Override
		protected void onPostExecute(List<ClientPlayer> result) {
			if (result == null) return;
			if (names != null) {
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(BidPlayersActivity.this, R.layout.player, names);
				((ListView)MainView.this.mainView.findViewById(R.id.players)).setAdapter(adapter);
			}
			closeLoadingDialog();
		}
	}	
}
