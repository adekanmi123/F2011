package eu.loopit.f2011;

import java.util.List;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.google.gson.reflect.TypeToken;

import dk.bregnvig.formula1.client.domain.wbc.ClientWBCEntry;
import eu.loopit.f2011.util.RestHelper;

public class WbcActivity extends BaseActivity {

	public static final String TAG = WbcActivity.class.getSimpleName();
	private ListView list;
	private List<ClientWBCEntry> entries; 
	private WbcPlayerAdapter adapter;
	private ProgressDialog waitingDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wbc);
		list = (ListView) findViewById(R.id.entries);
		entries = getF2011Application().getWbcEntries();
		adapter = new WbcPlayerAdapter(this, R.layout.wbc_entry, entries, true);
		//get reference to the ListView and tell it to use our TasksAdapter
		list.setAdapter(adapter);
		if (getF2011Application().getWbcEntries().size() == 0) {
			waitingDialog = ProgressDialog.show(this, "", getString(R.string.loading_wbc_label), true);
			new WBCTask().execute();
		}
		
        View.OnTouchListener gestureListener = new View.OnTouchListener() { 
            public boolean onTouch(View v, MotionEvent event) { 
                if (detector.onTouchEvent(event)) { 
                    return true; 
                } 
                return false; 
            } 
        }; 
        list.setOnTouchListener(gestureListener);
        list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				ClientWBCEntry entry = entries.get(position);
				startActivity(getWBCPlayerIntent(entry));
			}
		});
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
			return helper.getJSONData("/wbc", ClientWBCEntry.class, new TypeToken<List<ClientWBCEntry>>(){}.getType());
		}

		@Override
		protected void onPostExecute(List<ClientWBCEntry> result) {
			if (result == null || result.size() == 0) return;
			getF2011Application().setWbcEntries(result);
			for (ClientWBCEntry entry : result) {
				adapter.add(entry);
			}
			waitingDialog.dismiss();
		}
		
	}
	
	private Intent getWBCPlayerIntent(ClientWBCEntry entry) {
		Intent intent = new Intent(this, WbcPlayerActivity.class);
		intent.putExtra(WbcPlayerActivity.NAME, entry.getPlayer().getName());
		intent.putExtra(WbcPlayerActivity.PLAYER_NAME, entry.getPlayer().getPlayername());
    	return intent;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (velocityX > 0) {
    		Intent intent = new Intent(this, WelcomeActivity.class);
    		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    		startActivity(intent);
        	return true;
        }
        return false;
	}
	
}
