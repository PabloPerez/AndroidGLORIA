package com.example.gloriaproject;

import java.io.File;
import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.ActionBar.Tab;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;


public class Main extends SherlockFragmentActivity implements MediaScannerConnectionClient
{


	ViewPager mViewPager;
	TabsAdapter mTabsAdapter;
	TextView tabCenter;
	TextView tabText;

	public String[] allFiles;
	private String SCAN_PATH ;
	private static final String FILE_TYPE = "*/*";

	private MediaScannerConnection conn;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		mViewPager = new ViewPager(this){
			public boolean onInterceptTouchEvent(MotionEvent event) {
				return false;
			}
		};
		mViewPager.setId(R.id.pager);
		setContentView(mViewPager);
		mViewPager.requestDisallowInterceptTouchEvent(true);

		ActionBar bar = getSupportActionBar();
		bar.setDisplayUseLogoEnabled(true);
		//bar.setDisplayShowHomeEnabled(false);
		//bar.setDisplayShowTitleEnabled(false);

		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		/*	 ActionBar.Tab tab1 = bar.newTab();
	        ActionBar.Tab tab2 = bar.newTab();
	        tab1.setText("Telescopio");
	        tab2.setText("Telescopio");
	        tab1.setTabListener(new MyTabListener());
	        tab2.setTabListener(new MyTabListener());
	        bar.addTab(tab1);
	        bar.addTab(tab2);

		 */
		mTabsAdapter = new TabsAdapter(this, mViewPager);

		mTabsAdapter.addTab(
				bar.newTab().setText("SUN"),
				Telescope.class, null);
		mTabsAdapter.addTab(
				bar.newTab()
				.setText("ENVIRONMENT"),
				Dome.class, null);

		mViewPager.setOnTouchListener(new OnTouchListener()
		{           
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
				return true;
			}
		});


	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.activity_main, menu);
		return true;
	}



	/*
    private class MyTabListener implements ActionBar.TabListener
    {

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			if(tab.getPosition()==0)
			{
				Telescope frag = new Telescope();
				ft.replace(android.R.id.content, frag);

			}
			else
			{
				Telescope frag = new Telescope();
				ft.replace(android.R.id.content, frag);
			}
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			// TODO Auto-generated method stub

		}

    }*/

	public static class TabsAdapter extends FragmentPagerAdapter implements
	ActionBar.TabListener,	ViewPager.OnPageChangeListener
	{
		private final Context mContext;
		private final ActionBar mActionBar;
		private final ViewPager mViewPager;
		private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

		static final class TabInfo
		{
			private final Class<?> clss;
			private final Bundle args;

			TabInfo(Class<?> _class, Bundle _args)
			{
				clss = _class;
				args = _args;
			}
		}

		public TabsAdapter(SherlockFragmentActivity activity, ViewPager pager)
		{
			super(activity.getSupportFragmentManager());
			mContext = activity;
			mActionBar = activity.getSupportActionBar();
			mViewPager = pager;
			mViewPager.setAdapter(this);
			mViewPager.setOnPageChangeListener(this);
		}

		public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle args)
		{
			TabInfo info = new TabInfo(clss, args);
			tab.setTag(info);
			tab.setTabListener(this);
			mTabs.add(info);
			mActionBar.addTab(tab);
			notifyDataSetChanged();
		}

		@Override
		public int getCount()
		{
			return mTabs.size();
		}

		@Override
		public Fragment getItem(int position)
		{
			TabInfo info = mTabs.get(position);
			return Fragment.instantiate(mContext, info.clss.getName(),
					info.args);
		}

		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels)
		{
		}

		public void onPageSelected(int position)
		{
			mActionBar.setSelectedNavigationItem(position);
		}

		public void onPageScrollStateChanged(int state)
		{
		}

		public void onTabSelected(Tab tab, FragmentTransaction ft)
		{
			Object tag = tab.getTag();
			for (int i = 0; i < mTabs.size(); i++)
			{
				if (mTabs.get(i) == tag)
				{
					mViewPager.setCurrentItem(i);
				}
			}
		}

		public void onTabUnselected(Tab tab, FragmentTransaction ft)
		{
		}

		public void onTabReselected(Tab tab, FragmentTransaction ft)
		{
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.gallery:
			final SharedPreferences prefs =getSharedPreferences("GLORIAPrefs",Context.MODE_PRIVATE);
			String route = prefs.getString("GalleryRoute", "Pictures/GLORIA");

			File folder = new File(Environment.getExternalStorageDirectory().getPath()+"/"+route+"/");
			Log.d("DEBUG", "Open Gallery " + folder.getAbsolutePath());
			allFiles = folder.list();
			if(allFiles.length>0)
			{
				for(int i=0;i<allFiles.length;i++)
				{
					Log.d("all file path"+i, allFiles[i]+allFiles.length);
				}
				SCAN_PATH=Environment.getExternalStorageDirectory().toString()+"/"+route+"/"+allFiles[0];
				Log.d("SCAN PATH", "Scan Path " + SCAN_PATH);
				startScan();
				//sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+ Environment.getExternalStorageDirectory())));
			}
			else
			{
				Toast toast1 = Toast.makeText(getApplicationContext(),"Gallery is empty!!", Toast.LENGTH_SHORT);
				toast1.show();	
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onMediaScannerConnected() {
		Log.d("onMediaScannerConnected","success"+conn);
		conn.scanFile(SCAN_PATH, FILE_TYPE);    	
	}

	@Override
	public void onScanCompleted(String path, Uri uri) {
		try {
			Log.d("onScanCompleted",uri + "success"+conn);
			if (uri != null) 
			{
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(uri);
				startActivity(intent);
			}
		} finally 
		{
			conn.disconnect();
			conn = null;
		}
	}

	private void startScan()
	{
		Log.d("Connected","success"+conn);
		if(conn!=null)
		{
			conn.disconnect();
		}
		conn = new MediaScannerConnection(this,this);
		conn.connect();
	}
}
