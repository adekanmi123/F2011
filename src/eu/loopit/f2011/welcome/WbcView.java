package eu.loopit.f2011.welcome;

import java.util.List;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.google.gson.reflect.TypeToken;

import dk.bregnvig.formula1.client.domain.wbc.ClientWBCEntry;
import eu.loopit.f2011.PageView;
import eu.loopit.f2011.R;
import eu.loopit.f2011.WbcPlayerActivity;
import eu.loopit.f2011.WbcPlayerAdapter;
import eu.loopit.f2011.util.RestHelper;

public class WbcView implements PageView{
	private ListView list;
	private List<ClientWBCEntry> entries; 
	private WbcPlayerAdapter adapter;
	private final View wbcView;
	private boolean initialized;
	private boolean hasFocus;
	private WelcomeActivity owner;
	private WBCTask task;
	
	WbcView(WelcomeActivity owner, View wbcView) {
		this.wbcView = wbcView;
		this.owner = owner;
	}
	
	public void load() {
		task = new WBCTask();
		displayLoadingDialog();
		task.execute();
	}
	
	public boolean isLoading() {
		return task == null ? false : task.getStatus() != Status.FINISHED;
	}
	
	public void onFocusChange(boolean hasFocus) {
		this.hasFocus = hasFocus;
		displayLoadingDialog();
	}
	
	public void initialize() {
		if (initialized == true) return;
		list = (ListView) wbcView.findViewById(R.id.entries);
		entries = owner.getF2011Application().getWbcEntries();
		adapter = new WbcPlayerAdapter(owner, R.layout.wbc_entry, entries, true);
		//get reference to the ListView and tell it to use our TasksAdapter
		list.setAdapter(adapter);
		if (owner.getF2011Application().getWbcEntries().size() == 0) {
			load();
		}
		
        list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				ClientWBCEntry entry = entries.get(position);
				owner.startActivity(getWBCPlayerIntent(entry));
			}
		});
        initialized = true;
	}
	
	private void displayLoadingDialog() {
		if (hasFocus && isLoading()) owner.showLoadingDialog(owner.getString(R.string.loading_wbc_label));
	}
	
	private Intent getWBCPlayerIntent(ClientWBCEntry entry) {
		Intent intent = new Intent(owner, WbcPlayerActivity.class);
		intent.putExtra(WbcPlayerActivity.NAME, entry.getPlayer().getName());
		intent.putExtra(WbcPlayerActivity.PLAYER_NAME, entry.getPlayer().getPlayername());
    	return intent;
	}
	
	private class WBCTask extends AsyncTask<Void, Void, List<ClientWBCEntry>> {

		@Override
		protected List<ClientWBCEntry> doInBackground(Void... params) {
			RestHelper helper = new RestHelper(owner.getF2011Application());
			return helper.getJSONData("/wbc", ClientWBCEntry.class, new TypeToken<List<ClientWBCEntry>>(){}.getType());
		}

		@Override
		protected void onPostExecute(List<ClientWBCEntry> result) {
			if (result == null || result.size() == 0) return;
			owner.getF2011Application().setWbcEntries(result);
			for (ClientWBCEntry entry : result) {
				adapter.add(entry);
			}
			owner.waitingDialog.dismiss();
			owner.pageLoaded();
		}
	}
}
