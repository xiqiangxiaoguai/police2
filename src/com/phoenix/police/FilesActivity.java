package com.phoenix.police;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.SpinnerAdapter;

import com.phoenix.data.Constants;
import com.phoenix.online.A9TerminalActivity;
import com.phoenix.setting.SettingActivity;

public class FilesActivity extends Activity{

	private static String STATE_SELECTED_NAVIGATION_ITEM = "state_selected_navigation_item";
	FragmentManager manager;
	
	class DropDownListener implements OnNavigationListener{

		String[] listNames = getResources().getStringArray(R.array.file_module);
		
		@Override
		public boolean onNavigationItemSelected(int itemPosition, long itemId) {
			FragmentTransaction transaction = manager.beginTransaction();
			switch (itemPosition) {
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
				default:
					CameraFragment dFragment = new CameraFragment();
					transaction.replace(R.id.forfragment, dFragment);
					transaction.commit();
					break;
			}
			return true;
		}
		
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.files_main);
		manager = getFragmentManager();
		
		ActionBar actionBar = getActionBar();
//		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setIcon(R.drawable.menu_files);
		
		SpinnerAdapter sAdapter = ArrayAdapter.createFromResource(this, R.array.file_module, android.R.layout.simple_spinner_dropdown_item);
		actionBar.setNavigationMode(actionBar.NAVIGATION_MODE_LIST);
		actionBar.setListNavigationCallbacks(sAdapter, new DropDownListener());
		
//		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
//		actionBar.addTab(actionBar.newTab().setText(R.string.camera)
//				.setTabListener(this));
//		actionBar.addTab(actionBar.newTab().setText(R.string.video)
//				.setTabListener(this));
//		actionBar.addTab(actionBar.newTab().setText(R.string.record)
//				.setTabListener(this));
		
		
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
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Intent intent;
		switch (keyCode) {
		case KeyEvent.KEYCODE_CAMERA:
			intent = new Intent(this, CameraActivity.class);
			intent.putExtra(Constants.AUTO_VIDEO, false);
			startActivity(intent);
			break;
			
		case KeyEvent.KEYCODE_MEDIA_RECORD:
			intent = new Intent(this, CameraActivity.class);
			intent.putExtra(Constants.AUTO_VIDEO, true);
			startActivity(intent);
			break;
			
		case KeyEvent.KEYCODE_MUSIC:
			intent = new Intent("com.phoenix.police.AudioActivity");
			intent.putExtra(Constants.AUTO_AUDIO, true);
			startActivity(intent);
			break;
			
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

}
