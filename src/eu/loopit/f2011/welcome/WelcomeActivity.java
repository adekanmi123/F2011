package eu.loopit.f2011.welcome;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import eu.loopit.f2011.BaseActivity;
import eu.loopit.f2011.PageView;
import eu.loopit.f2011.Preferences;
import eu.loopit.f2011.R;

public class WelcomeActivity extends BaseActivity {

	public static final String TAG = WelcomeActivity.class.getSimpleName();
	public static final String FORCE_REFRESH = "forceRefresh";
	public ProgressDialog waitingDialog;
	private PageView[] views = new PageView[2];
	private int position;
	private boolean[] initializedPages = {false, false};

	private ViewPager pager;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pager);
		pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(new MainViewPagerAdapter());
		pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			
			public void onPageSelected(int position) {
				Log.i(TAG, "Page selected " + position);
				WelcomeActivity.this.position = position;
				for (int i = 0; i < views.length; i++) {
					views[i].onFocusChange(position == i ? true : false);
				}
				initializedPages[position] = true;
				views[position].initialize();
			}
			
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			
			public void onPageScrollStateChanged(int arg0) {
			}
		});

	}
	
	public void pageLoaded() {
		initializedPages[position] = true;
		if (position + 1 < views.length && initializedPages[position+1] == false) {
			initializedPages[position+1] = true;
			views[position+1].initialize();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (intent.getBooleanExtra(FORCE_REFRESH, false) == true) {
			Log.i(TAG, "Forcing refresh of game data");
			views[0].load();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settings:
			startActivity(new Intent(this, Preferences.class));
			return true;
		case R.id.exit:
			finish();
			return true;
		case R.id.refresh:
			views[position].load();
			return true;
		}
		return false;
	}
	
	public void showLoadingDialog(String message) {
		if (waitingDialog == null || waitingDialog.isShowing() == false) {
			waitingDialog = ProgressDialog.show(this, "", message, true);
		}
	}
	
	public void dismissLoadingDialog() {
		if (waitingDialog != null && waitingDialog.isShowing()) waitingDialog.dismiss();
	}
	
	private class MainViewPagerAdapter extends PagerAdapter {

		@Override
		public void destroyItem(View pager, int position, Object page) {
			((ViewPager)pager).removeView((View)page);
		}

		@Override
		public void finishUpdate(View view) {
			Log.i(TAG, "Finish with the view..." + view.getClass().getName());
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public Object instantiateItem(View pager, int position) {
			LayoutInflater li = (LayoutInflater)WelcomeActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = null;
			PageView pageView = null;
			switch (position) {
			case 1:
				view = li.inflate(R.layout.wbc, null);
				pageView = new WbcView(WelcomeActivity.this, view);
				break;
			default:
				view = li.inflate(R.layout.main, null);
				pageView = new MainView(WelcomeActivity.this, view);
				pageView.initialize();
				break;
			}
			views[position] = pageView;
			Log.i(TAG, "Instatiating view. Position " + position + " Page " + view.getId());
			((ViewPager)pager).addView(view, 0);
			return view;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view==((View)object);
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {
		}
	}
}