package com.phoenix.police;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class FilesActivity extends Activity implements ActionBar.TabListener{

	private static String STATE_SELECTED_NAVIGATION_ITEM = "state_selected_navigation_item";
	FragmentManager manager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.files_main);
		manager = getFragmentManager();
		
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.addTab(actionBar.newTab().setText(R.string.camera)
				.setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText(R.string.video)
				.setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText(R.string.record)
				.setTabListener(this));
		
		Button button = (Button) findViewById(R.id.back);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		if(savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)){
			getActionBar().setSelectedNavigationItem(
					savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		}
		
	}
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar().getSelectedNavigationIndex());
	}
	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		FragmentTransaction transaction = manager.beginTransaction();
		switch(tab.getPosition()){
		case 0:
			
			CameraFragment cFragment = new CameraFragment();
			transaction.replace(R.id.forfragment, cFragment);
			transaction.commit();
			break;
		case 1:
			VideoFragment vFragment = new VideoFragment();
			transaction.replace(R.id.forfragment, vFragment);
			transaction.commit();
			break;
		case 2:
			AudioFragment aFragment = new AudioFragment();
			transaction.replace(R.id.forfragment, aFragment);
			transaction.commit();
			break;
		}
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			onBackPressed();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
