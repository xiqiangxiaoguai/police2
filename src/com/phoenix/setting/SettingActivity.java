package com.phoenix.setting;

import java.io.File;
import java.text.DecimalFormat;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.StatFs;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;

import com.phoenix.data.Constants;
import com.phoenix.lib.SlidingMenu;
import com.phoenix.lib.app.SlidingPreferenceActivity;
import com.phoenix.online.A9TerminalActivity;
import com.phoenix.police.AudioActivity;
import com.phoenix.police.AvInActivity;
import com.phoenix.police.FilesActivity;
import com.phoenix.police.MainScene;
import com.phoenix.police.R;

public class SettingActivity extends SlidingPreferenceActivity implements Preference.OnPreferenceChangeListener, android.view.View.OnClickListener{
	private static final boolean LOG_SWITCH = Constants.LOG_SWITCH;
	private static final String LOG_TAG = SettingActivity.class.getSimpleName();
	
	PreferenceScreen wifiScreen;
	Preference storageScreen;
	PreferenceScreen aboutScreen;
	BrightnessSeekBarPreference brightnessPreference;
	ListPreference resolutionList ;
	
	SwitchPreference wifiSwitch;
	Handler mHandler;
	
	String mTotalStor = "";
	String mCameraStor = "";
	String mVideoStor = "";
	String mAudioStor = "";
	
	ConnectivityManager conn;
	
	private SlidingMenu mainMenu = null;
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (LOG_SWITCH) {
			Log.d(LOG_TAG, "preference.getkey:" + preference.getKey());
		}

		if(preference.getKey().equals("setting_3g_switch_preference")){
			PhoenixMethod.set3G(!((SwitchPreference)preference).isChecked());
		}
		return true;
	}
	
	//***********************************Runnable for get storage detail*****************************************
	Runnable scanStorageRun = new Runnable() {
		@Override
		public void run() {
//			storageScreen.removeAll();
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
				File path = Environment.getExternalStorageDirectory();
//				Preference storPreference = new Preference(SettingActivity.this);
//				storPreference.setTitle(R.string.storage_describe);
//				storPreference.setSummary(getAvailaStor(path.getPath()) + "/" + getTotalStor(path.getPath()));
//				storageScreen.addPreference(storPreference);
				mTotalStor = getAvailaStor(path.getPath()) + "/" + getTotalStor(path.getPath());
				
//				storPreference = new Preference(SettingActivity.this);
//				storPreference.setTitle(R.string.camera_file);
//				storPreference.setSummary(getFolderStor(Constants.CAMERA_PATH));
//				storageScreen.addPreference(storPreference);
				mCameraStor = getFolderStor(Constants.CAMERA_PATH);
				
//				storPreference = new Preference(SettingActivity.this);
//				storPreference.setTitle(R.string.video_file);
//				storPreference.setSummary(getFolderStor(Constants.VIDEO_PATH));
//				storageScreen.addPreference(storPreference);
				mVideoStor = getFolderStor(Constants.VIDEO_PATH);
				
//				storPreference = new Preference(SettingActivity.this);
//				storPreference.setTitle(R.string.audio_file);
//				storPreference.setSummary(getFolderStor(Constants.AUDIO_PATH));
//				storageScreen.addPreference(storPreference);
				mAudioStor = getFolderStor(Constants.AUDIO_PATH);
				
			}
		}
	};

	private String getTotalStor(String path){
		StatFs statfs = new StatFs(path);
		long blockSize = statfs.getBlockSize();
		long availaBlock = statfs.getBlockCount(); 
		return new DecimalFormat("0.00").format(blockSize* availaBlock/1024/1024d/1024d) + "G";
		
	}
	
	private String getAvailaStor(String path){
		StatFs statfs = new StatFs(path);
		long blockSize = statfs.getBlockSize();
		long totalBlocks = statfs.getAvailableBlocks(); 
		return new DecimalFormat("0.00").format(blockSize* totalBlocks/1024/1024d/1024d) + "G";
	}
	
	private long getFolderStor(File path){
		long size = 0;
		File f = path;
		File flist[] = f.listFiles();
		for (int i = 0; i < flist.length; i++) {
			if (flist[i].isDirectory()) {
				size = size + getFolderStor(flist[i]);
			} else {
				size = size + flist[i].length();
			}
		}
		return size;
	}
	
	private String getFolderStor(String path){
		return new DecimalFormat("0.00").format(getFolderStor(new File(path))/1024/1024d/1024d) + "G";
	}
	
	//***********************************Runnable for get storage detail*****************************************
	//***********************************Runnable for about*********************************************
	//***********************************Runnable for about*********************************************
	//*****************************Runnable for scan network***************************
	
	//*****************************Runnable for scan network***************************
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		wifiScreen = (PreferenceScreen) findPreference("setting_wifi_preference");
		storageScreen = (Preference) findPreference("setting_storage_preference");
		aboutScreen = (PreferenceScreen) findPreference("setting_about_preference");
		resolutionList = (ListPreference)findPreference("setting_function_resolution");
		brightnessPreference = (BrightnessSeekBarPreference) findPreference("setting_function_brightness");
		brightnessPreference.pushActivity(SettingActivity.this);
		conn  = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		
		HandlerThread hThread = new HandlerThread(SettingActivity.class.getSimpleName());
		hThread.start();
		mHandler = new Handler(hThread.getLooper());
		mHandler.post(scanStorageRun);
		wifiScreen.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent (SettingActivity.this,WifiActivity.class);
				startActivity(intent);
				return false;
			}
		});
		storageScreen.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				if (LOG_SWITCH) {
					Log.d(LOG_TAG, "storageScreen clicked");
				}
				Intent intent = new Intent(SettingActivity.this, StorPreActivity.class);
				Bundle data = new Bundle();
				data.putString(StorPreActivity.KEY_STOR_TOTAL, mTotalStor);
				data.putString(StorPreActivity.KEY_STOR_CAMERA, mCameraStor);
				data.putString(StorPreActivity.KEY_STOR_VIDEO, mVideoStor);
				data.putString(StorPreActivity.KEY_STOR_AUDIO, mAudioStor);
				intent.putExtras(data);
				startActivity(intent);
				return false;
			}
		});
		aboutScreen.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				Intent intent = new Intent (SettingActivity.this,AboutPreActivity.class);
				startActivity(intent);
				return false;
			}
		});
		
		SwitchPreference _3gSwitch = (SwitchPreference) findPreference("setting_3g_switch_preference");
		_3gSwitch.setOnPreferenceChangeListener(this);
		
		
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
		RelativeLayout mWirelessMenu = (RelativeLayout) mainMenu.getMenu().findViewById(R.id.menu_wireless);
		mWirelessMenu.setOnClickListener(this);
		RelativeLayout mAvIn = (RelativeLayout) mainMenu.getMenu().findViewById(R.id.menu_av);
		mAvIn.setOnClickListener(this);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		mHandler.removeCallbacks(scanStorageRun);
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
