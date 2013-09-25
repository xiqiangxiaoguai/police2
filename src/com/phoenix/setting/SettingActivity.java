package com.phoenix.setting;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.StatFs;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.phoenix.data.Constants;
import com.phoenix.police.R;

public class SettingActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener{
	private static final boolean LOG_SWITCH = Constants.LOG_SWITCH;
	private static final String LOG_TAG = SettingActivity.class.getSimpleName();
	
	private WifiManager mWifiManager;
	
	PreferenceScreen wifiScreen;
	Preference storageScreen;
	PreferenceScreen aboutScreen;
	BrightnessSeekBarPreference brightnessPreference;
	ListPreference resolutionList ;
	
	ConnectivityManager mConnect ;
	
	Handler mHandler;
	
	String mTotalStor = "";
	String mCameraStor = "";
	String mVideoStor = "";
	String mAudioStor = "";
	
	static final int SECURITY_NONE = 0;
	static final int SECURITY_WEP = 1;
	static final int SECURITY_WPA = 2;
	
	private int wifiDrawableLock[] = new int[]{
			R.drawable.ic_wifi_lock_signal_1,
			R.drawable.ic_wifi_lock_signal_2,
			R.drawable.ic_wifi_lock_signal_3,
			R.drawable.ic_wifi_lock_signal_4,
	};
	
	private int wifiDrawableUnLock[] = new int[]{
			R.drawable.ic_wifi_signal_1,
			R.drawable.ic_wifi_signal_2,
			R.drawable.ic_wifi_signal_3,
			R.drawable.ic_wifi_signal_4
	};
	
	private int[] wifiQuality = new int[]{
			R.string.wifi_1,
			R.string.wifi_2,
			R.string.wifi_3,
			R.string.wifi_4
	};
	ArrayList<AccessPoint> accessPoints = new ArrayList<AccessPoint>();
	HashMap<String, ScanResult> mResults = new HashMap<String, ScanResult>();
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		if(preference.getClass().getName() == AccessPoint.class.getName()){
			final ScanResult result = mResults.get(preference.getTitle());
			LayoutInflater factory = LayoutInflater.from(this);
			View view = factory.inflate(R.layout.pw_edit, null);
			((TextView)view.findViewById(R.id.leveldetail)).setText(wifiQuality[mWifiManager.calculateSignalLevel(result.level, 4)]);
			final EditText edit =  (EditText) view.findViewById(R.id.passworddetail);
			AlertDialog dialog = new AlertDialog.Builder(this).setTitle(result.SSID).setView(view)
					.setPositiveButton(R.string.connect, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String pw = edit.getText().toString();
							addNetwork(CreateWifiInfo(result.SSID, pw , getSecurity(result)));
						}
					})
					.setNegativeButton(R.string.cancel, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					}).create();
			dialog.show();
		}
		
//		if(preference.getKey() == "setting_storage_preference"){
//			
//		}
		
		return true;
	}
	

	//************************** Join network **************************************
    public void addNetwork(WifiConfiguration wcg) { 
		 int wcgID = mWifiManager.addNetwork(wcg); 
	     boolean b =  mWifiManager.enableNetwork(wcgID, true); 
	     Log.d(LOG_TAG, "add Network returned " + wcgID );
	     Log.d(LOG_TAG, "enableNetwork returned " + b );  
    }
    
	public WifiConfiguration CreateWifiInfo(String SSID, String Password, int Type) 
    { 
          WifiConfiguration config = new WifiConfiguration();   
           config.allowedAuthAlgorithms.clear(); 
           config.allowedGroupCiphers.clear(); 
           config.allowedKeyManagement.clear(); 
           config.allowedPairwiseCiphers.clear(); 
           config.allowedProtocols.clear(); 
           config.SSID = "\"" + SSID + "\"";   
          
          WifiConfiguration tempConfig = this.IsExsits(SSID);  
          
          if(tempConfig != null) {  
        	  mWifiManager.removeNetwork(tempConfig.networkId); 
          }
          
          if(Type == SECURITY_NONE) //WIFICIPHER_NOPASS
          { 
               config.wepKeys[0] = ""; 
               config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE); 
               config.wepTxKeyIndex = 0; 
          } 
          if(Type == SECURITY_WEP) //WIFICIPHER_WEP
          { 
              config.hiddenSSID = true;
              config.wepKeys[0]= "\""+Password+"\""; 
              config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED); 
              config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP); 
              config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP); 
              config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40); 
              config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104); 
              config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE); 
              config.wepTxKeyIndex = 0; 
          } 
          if(Type == SECURITY_WPA) //WIFICIPHER_WPA
          { 
          config.preSharedKey = "\""+Password+"\""; 
          config.hiddenSSID = true;   
          config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);   
          config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);                         
          config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);                         
          config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);                    
          //config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);  
          config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
          config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
          config.status = WifiConfiguration.Status.ENABLED;   
          }
           return config; 
    } 
	
	private WifiConfiguration IsExsits(String SSID)  
    {  
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();  
           for (WifiConfiguration existingConfig : existingConfigs)   
           {  
             if (existingConfig.SSID.equals("\""+SSID+"\""))  
             {  
                 return existingConfig;  
             }  
           }  
        return null;   
    }
	
	//************************** Join network **************************************
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if(preference.getKey().equals("setting_wifi_switch_preference")){
			if(((SwitchPreference)preference).isChecked()){
				if(mWifiManager.isWifiEnabled()){
					mWifiManager.setWifiEnabled(false);
				}
			}else{
				if(!mWifiManager.isWifiEnabled()){
					mWifiManager.setWifiEnabled(true);
				}
			}
		}
		if(preference.getKey().equals("setting_3g_switch_preference")){
			PhoenixMethod.set3G(((SwitchPreference)preference).isChecked());
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
	Runnable scanWifiRun = new Runnable() {
		@Override
		public void run() {
			updateAccessPoints();
			mHandler.postDelayed(scanWifiRun, 5000);
		}
	};
	//*****************************Runnable for scan network***************************
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preferences_layout);
		addPreferencesFromResource(R.xml.preferences);
		wifiScreen = (PreferenceScreen) findPreference("setting_wifi_preference");
		storageScreen = (Preference) findPreference("setting_storage_preference");
		aboutScreen = (PreferenceScreen) findPreference("setting_about_preference");
		resolutionList = (ListPreference)findPreference("setting_function_resolution");
		brightnessPreference = (BrightnessSeekBarPreference) findPreference("setting_function_brightness");
		brightnessPreference.pushActivity(SettingActivity.this);
		resolutionList.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Log.d("qiqi", "" + newValue.toString());
				return true;
			}
		});
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		HandlerThread hThread = new HandlerThread(SettingActivity.class.getSimpleName());
		hThread.start();
		mHandler = new Handler(hThread.getLooper());
		mHandler.post(scanStorageRun);
		mHandler.post(scanWifiRun);
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
				return true;
			}
		});
		aboutScreen.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				Intent intent = new Intent (SettingActivity.this,AboutPreActivity.class);
				startActivity(intent);
				return true;
			}
		});
		SwitchPreference wifiSwitch = (SwitchPreference) findPreference("setting_wifi_switch_preference");
		wifiSwitch.setChecked(mWifiManager.isWifiEnabled());
		wifiSwitch.setOnPreferenceChangeListener(this);
		
		mConnect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		SwitchPreference _3gSwitch = (SwitchPreference) findPreference("setting_3g_switch_preference");
		_3gSwitch.setOnPreferenceChangeListener(this);
		
		Button button = (Button) findViewById(R.id.back);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		mHandler.removeCallbacks(scanWifiRun);
		mHandler.removeCallbacks(scanStorageRun);
	}
	
	private void updateAccessPoints(){
		int wifiState = mWifiManager.getWifiState();
		switch(wifiState){
			case WifiManager.WIFI_STATE_ENABLED:
				Collection<AccessPoint> accessPoints = constructAccessPoints();
				wifiScreen.removeAll();
				for(AccessPoint accessPoint : accessPoints){
					wifiScreen.addPreference(accessPoint);
				}
				break;
			case WifiManager.WIFI_STATE_ENABLING:
				break;
			case WifiManager.WIFI_STATE_DISABLED:
				break;
			case WifiManager.WIFI_STATE_DISABLING:
				break;
		}
	}
	
	private List<AccessPoint> constructAccessPoints(){
		accessPoints.clear();
		mResults.clear();
		mWifiManager.startScan();
		final String curSSID = mWifiManager.getConnectionInfo().getSSID();
		List<ScanResult> results = mWifiManager.getScanResults();
		if(results != null && results.size() != 0){
			
			Collections.sort(results, new Comparator<ScanResult>() {
				@Override
				public int compare(ScanResult a, ScanResult b) {
					if(("\"" + a.SSID + "\"").equals(curSSID)){
						return -1;
					}
					if(("\"" + b.SSID + "\"").equals(curSSID)){
						return 1;
					}
					if(a.level > b.level){
						return -1;
					}else{
						return 1;
					}
				}
			});
			
			for(ScanResult result : results){
				if(result.SSID == null || result.SSID.length() == 0 || result.capabilities.contains("[IBSS]"))
					continue;
				AccessPoint accessPoint = new AccessPoint(this);
				int c = mWifiManager.calculateSignalLevel(result.level, 4);
				accessPoint.setTitle(result.SSID);
				if(getSecurity(result) == 0){
					accessPoint.setIcon(wifiDrawableUnLock[c]);
				}else{
					accessPoint.setIcon(wifiDrawableLock[c]);
				}
				if(("\"" + result.SSID + "\"").equals(curSSID)){
					accessPoint.setSummary(R.string.connected);
				}
				accessPoint.setOnPreferenceClickListener(this);
				mResults.put(result.SSID, result);
				accessPoints.add(accessPoint);
			}
		}
		return accessPoints;
	} 
	private static int getSecurity(ScanResult result) {
        if (result.capabilities.contains("WEP")) {
            return SECURITY_WEP;
        } else if (result.capabilities.contains("PSK")) {
            return SECURITY_WPA;
        } 
        return SECURITY_NONE;
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
