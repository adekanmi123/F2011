package eu.loopit.f2011;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.google.gson.reflect.TypeToken;

import dk.bregnvig.formula1.client.domain.ClientPlayer;
import eu.loopit.f2011.util.RestHelper;

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
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				startActivity(getBidPlayerIntent(players.get(position) ));
			}
		});
		title.setText(getString(R.string.players_bid_title));
		openLoadingDialog(getString(R.string.loading_bids));
		new PlayersTask().execute();
	}
	
	
	private Intent getBidPlayerIntent(ClientPlayer player) {
		Intent intent = new Intent(this, BidPlayerActivity.class);
		intent.putExtra(WbcPlayerActivity.NAME, player.getName());
		intent.putExtra(WbcPlayerActivity.PLAYER_NAME, player.getPlayername());
    	return intent;
	}

	private class PlayersTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			names = null;
			RestHelper helper = new RestHelper(getF2011Application());
			BidPlayersActivity.this.players = helper.getJSONData("/race?players", ClientPlayer.class, new TypeToken<List<ClientPlayer>>(){}.getType());
			names = new ArrayList<String>(BidPlayersActivity.this.players.size());
			for (ClientPlayer player : BidPlayersActivity.this.players) {
				names.add(player.getName());
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void dd) {
			if (names != null) {
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(BidPlayersActivity.this, R.layout.player, names);
				list.setAdapter(adapter);
			}
			closeLoadingDialog();
		}
	}	
}
