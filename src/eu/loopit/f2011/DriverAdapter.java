package eu.loopit.f2011;

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
import eu.loopit.f2011.library.BitmapManager;

public class DriverAdapter extends ArrayAdapter<ClientDriver> {
	
	public static final String TAG = DriverAdapter.class.getSimpleName();
	private ClientDriver[] drivers;
	private BitmapManager imageManager;
	private F2011Application application;
	private Bitmap defaultImage;
	
	public DriverAdapter(Context context, int textViewResourceId, ClientDriver[] objects) {
		super(context, textViewResourceId, objects);
		this.drivers = objects;
		imageManager = new BitmapManager(context);
		application = ((BaseActivity)context).getF2011Application(); 
		defaultImage = BitmapFactory.decodeResource(application.getResources(), R.drawable.unknown);
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
	        view = li.inflate(R.layout.driver, null);
	    }
	    //we are passed a "position" - this is the index
	    //of the item for this particular row
	    ClientDriver driver = drivers[position];
	 
	    if(driver != null) {
	        //get a reference to this layout's TextView
	        TextView txtView = (TextView)view.findViewById(R.id.driverName);
	        txtView.setText(driver.getName());
	        ImageView image = (ImageView) view.findViewById(R.id.driverImage);
	        if (driver != application.getNoDriver()) {
	        	imageManager.fetchBitmapOnThread(application.getDriverImageURL(driver), image, defaultImage);
	        } else {
	        	image.setImageResource(R.drawable.unknown);
	        }
	    }
	    //return our constructed view to the ListView
	    return view;
	}	

}
