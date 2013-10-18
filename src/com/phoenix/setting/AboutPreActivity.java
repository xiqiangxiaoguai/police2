package com.phoenix.setting;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.phoenix.data.Constants;
import com.phoenix.lib.SlidingMenu;
import com.phoenix.lib.app.SlidingPreferenceActivity;
import com.phoenix.police.AudioActivity;
import com.phoenix.police.FilesActivity;
import com.phoenix.police.MainScene;
import com.phoenix.police.R;

public class AboutPreActivity extends SlidingPreferenceActivity implements View.OnClickListener{

	private SlidingMenu mainMenu;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preferences_layout);
		addPreferencesFromResource(R.xml.about_preferences);
		PreferenceScreen deviceName = (PreferenceScreen) findPreference("preference_about_device_name");
		PreferenceScreen platformName = (PreferenceScreen) findPreference("preference_android_version");
		PreferenceScreen versionName = (PreferenceScreen) findPreference("preference_version");
		
		deviceName.setSummary(R.string.device_name_detail);
		platformName.setSummary(Constants.ANDROID_VERSION);
		versionName.setSummary(Constants.VERSION);
		
		Button back = (Button) findViewById(R.id.back);
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		
		setBehindContentView(R.layout.main_menus);
		mainMenu = getSlidingMenu();
		mainMenu.setMode(SlidingMenu.LEFT);
		mainMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		mainMenu.setShadowWidthRes(R.dimen.shadow_width);
//        menu.setShadowDrawable(R.drawable.shadow);
		mainMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		mainMenu.setFadeDegree(0.35f);
		mainMenu.setSlidingEnabled(true);
		mainMenu.setDragEnabled(false);
		RelativeLayout mCameraMenu = (RelativeLayout) mainMenu.getMenu().findViewById(R.id.menu_camera);
		mCameraMenu.setOnClickListener(this);
		RelativeLayout mAudioMenu = (RelativeLayout) mainMenu.getMenu().findViewById(R.id.menu_audio);
		mAudioMenu.setOnClickListener(this);
		RelativeLayout mFilesMenu = (RelativeLayout) mainMenu.getMenu().findViewById(R.id.menu_files);
		mFilesMenu.setOnClickListener(this);
		RelativeLayout mSettingMenu = (RelativeLayout) mainMenu.getMenu().findViewById(R.id.menu_setting);
		mSettingMenu.setOnClickListener(this);
		mSettingMenu.setBackgroundColor(Color.argb(100, 0, 255, 255));
		RelativeLayout mWirelessMenu = (RelativeLayout) mainMenu.getMenu().findViewById(R.id.menu_wireless);
		mWirelessMenu.setOnClickListener(this);
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
			startActivity(new Intent(this, FilesActivity.class));
			break;
		case R.id.menu_setting:
			mainMenu.toggle();
			break;
		case R.id.menu_wireless:
			break;
		case R.id.back:
			finish();
			break;
		}
	}
	
}
