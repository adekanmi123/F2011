package eu.loopit.f2011;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import dk.bregnvig.formula1.client.domain.wbc.ClientWBCEntry;

public class WbcPlayerAdapter extends ArrayAdapter<ClientWBCEntry> {
	
	public static final String TAG = WbcPlayerAdapter.class.getSimpleName();
	private List<ClientWBCEntry> entries;
	private boolean usePlayerName;
	
	public WbcPlayerAdapter(Context context, int textViewResourceId, List<ClientWBCEntry> objects, boolean usePlayerName) {
		super(context, textViewResourceId, objects);
		this.usePlayerName = usePlayerName;
		entries = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
	    //setup the view and handle it ourselves
	    View view = convertView;
	 
	    //if the view is null, we need to inflate our XML layout for it
	    if(view == null) {
	        //get a reference to the LayoutInflator
	        LayoutInflater li = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        //and inflate our XML into the View
	        view = li.inflate(R.layout.wbc_entry, null);
	    }
	    //we are passed a "position" - this is the index
	    //of the item for this particular row
	    ClientWBCEntry entry = entries.get(position);
	 
	    if(entry != null) {
	        //get a reference to this layout's TextView
	        TextView nameView = (TextView)view.findViewById(R.id.name);
	        if (usePlayerName) {
	        	nameView.setText(entry.getPlayer().getName());
	        } else {
	        	nameView.setText(entry.getRace().getName());
	        }
	        TextView pointsView = (TextView)view.findViewById(R.id.points);
	        pointsView.setText(Integer.toString(entry.getPoints()));
	    }
	    //return our constructed view to the ListView
	    return view;
	}	

}
