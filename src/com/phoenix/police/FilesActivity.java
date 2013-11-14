package com.phoenix.police;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

import com.phoenix.data.Constants;
import com.phoenix.lib.SlidingMenu;
import com.phoenix.lib.app.SlidingActivity;
import com.phoenix.online.A9TerminalActivity;
import com.phoenix.setting.SettingActivity;

public class FilesActivity extends SlidingActivity implements ActionBar.TabListener, OnClickListener{

	private static String STATE_SELECTED_NAVIGATION_ITEM = "state_selected_navigation_item";
	FragmentManager manager;
	private SlidingMenu mainMenu = null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
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
		setBehindContentView(R.layout.main_menus);
		
		mainMenu = getSlidingMenu();
		
		mainMenu.setMode(SlidingMenu.LEFT);
		mainMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		mainMenu.setShadowWidthRes(R.dimen.shadow_width);
//        menu.setShadowDrawable(R.drawable.shadow);
		mainMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		mainMenu.setDragEnabled(false);
		mainMenu.setFadeDegree(0.35f);
		setSlidingActionBarEnabled(true);
		
		RelativeLayout mCameraMenu = (RelativeLayout) mainMenu.getMenu().findViewById(R.id.menu_camera);
		mCameraMenu.setOnClickListener(this);
		RelativeLayout mAudioMenu = (RelativeLayout) mainMenu.getMenu().findViewById(R.id.menu_audio);
		mAudioMenu.setOnClickListener(this);
		RelativeLayout mFilesMenu = (RelativeLayout) mainMenu.getMenu().findViewById(R.id.menu_files);
		mFilesMenu.setOnClickListener(this);
		RelativeLayout mSettingMenu = (RelativeLayout) mainMenu.getMenu().findViewById(R.id.menu_setting);
		mSettingMenu.setOnClickListener(this);
		RelativeLayout mWirelessMenu = (RelativeLayout) mainMenu.getMenu().findViewById(R.id.menu_wireless);
		mWirelessMenu.setOnClickListener(this);
		RelativeLayout mAvIn = (RelativeLayout) mainMenu.getMenu().findViewById(R.id.menu_av);
		mAvIn.setOnClickListener(this);
		
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

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.menu_camera:
			startActivity(new Intent(this, MainScene.class));
			break;
		case R.id.menu_audio:
			startActivity(new Intent(this, AudioActivity.class));
			break;
		case R.id.menu_files:
			mainMenu.toggle();
			break;
		case R.id.menu_setting:
			startActivity(new Intent(this, SettingActivity.class));
			break;
		case R.id.menu_wireless:
			startActivity(new Intent(this, A9TerminalActivity.class));
			break;
		case R.id.menu_av:
//			startActivity(new Intent(this, AvInActivity.class));
			break;
		}
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Intent intent;
		switch (keyCode) {
		case KeyEvent.KEYCODE_CAMERA:
			intent = new Intent(this, MainScene.class);
			intent.putExtra(Constants.AUTO_VIDEO, false);
			startActivity(intent);
			break;
			
		case KeyEvent.KEYCODE_MEDIA_RECORD:
			intent = new Intent(this, MainScene.class);
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
