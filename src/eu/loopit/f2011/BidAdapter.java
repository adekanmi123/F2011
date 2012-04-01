package eu.loopit.f2011;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import dk.bregnvig.formula1.client.domain.ClientDriver;
import dk.bregnvig.formula1.client.domain.bid.ClientBid;
import eu.loopit.f2011.library.BitmapManager;

public class BidAdapter extends ArrayAdapter<Object> {
	
	public static final String TAG = BidAdapter.class.getSimpleName();
	private BitmapManager imageManager;
	private F2011Application application;
	private static Map<Integer, CharSequence> separator;
	private static Set<Integer> textViewPosition;
	private Bitmap defaultImage;
	
	public BidAdapter(Context context) {
		super(context, R.layout.bid_row);
		imageManager = new BitmapManager(context);
		application = ((BaseActivity)context).getF2011Application();
		initializeSeparator();
		initializeTextViewPosistion();
		defaultImage = BitmapFactory.decodeResource(application.getResources(), R.drawable.unknown);
	}
	
	public void setBid(ClientBid bid) {
		for (ClientDriver driver : bid.getGrid()) {
			add(driver);
		} 
		add(bid.getFastestLap());
		for (ClientDriver driver : bid.getPodium()) {
			add(driver);
		} 
		add(bid.getSelectedDriver()[0]);
		add(bid.getSelectedDriver()[1]);
		add(bid.getFirstCrash());
		add(bid.getPolePositionTimeInText());
		notifyDataSetChanged();
	}

	private void initializeTextViewPosistion() {
		if (separator == null) {
			separator = new HashMap<Integer, CharSequence>();
			separator.put(0, application.getResources().getText(R.string.grid_title));
			separator.put(6, application.getResources().getText(R.string.fastestLap_title));
			separator.put(7, application.getResources().getText(R.string.podium_title));
			separator.put(10, application.getResources().getText(R.string.select_driver_title));
			separator.put(12, application.getResources().getText(R.string.first_crash_title));
			separator.put(13, application.getResources().getText(R.string.pole_position_title));
		}
	}

	private void initializeSeparator() {
		if (textViewPosition == null) {
			textViewPosition = new HashSet<Integer>();
			textViewPosition.add(10);
			textViewPosition.add(11);
			textViewPosition.add(13);
		}
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    //setup the view and handle it ourselves
	    View view = convertView;
	 
	    //if the view is null, we need to inflate our XML layout for it
	    if(view == null) {
	        LayoutInflater li = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        view = li.inflate(R.layout.bid_row, null);
	    }
	    Integer objectPosition = new Integer(position);
	    TextView separatorView = (TextView) view.findViewById(R.id.separator); 
    	separatorView.setVisibility(separator.containsKey(objectPosition) ? View.VISIBLE : View.GONE);
    	view.findViewById(R.id.driver).setVisibility(textViewPosition.contains(objectPosition) ? View.GONE : View.VISIBLE);
    	TextView textOnlyView = (TextView) view.findViewById(R.id.textOnly); 
    	textOnlyView.setVisibility(textViewPosition.contains(objectPosition) ? View.VISIBLE : View.GONE);
    	if (separator.containsKey(objectPosition)) {
    		separatorView.setText(separator.get(objectPosition));
    	}
	    if(textOnlyView.getVisibility() == View.GONE) {
	        //get a reference to this layout's TextView
	    	ClientDriver driver = (ClientDriver) getItem(position);
	        TextView txtView = (TextView)view.findViewById(R.id.driverName);
	        ImageView image = (ImageView) view.findViewById(R.id.driverImage);
	        txtView.setText(driver.getName());
	        if (driver != application.getNoDriver()) {
	        	imageManager.fetchBitmapOnThread(application.getDriverImageURL(driver), image, defaultImage);
	        } else {
	        	txtView.setBackgroundResource(R.drawable.unknown);
	        }
	    } else {
	    	switch (position) {
			case 10:
				textOnlyView.setText(String.format("%s: %d", application.getString(R.string.starts_at), getItem(position)));
				break;
			case 11:
				textOnlyView.setText(String.format("%s: %d", application.getString(R.string.ends_at), getItem(position)));
				break;
			default:
				textOnlyView.setText(getItem(position).toString());
				break;
			}
	    }
	    //return our constructed view to the ListView
	    return view;
	}

}