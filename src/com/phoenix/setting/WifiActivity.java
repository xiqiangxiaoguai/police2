package com.phoenix.setting;

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
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.SwitchPreference;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.phoenix.data.Constants;
import com.phoenix.lib.SlidingMenu;
import com.phoenix.lib.app.SlidingPreferenceActivity;
import com.phoenix.police.AudioActivity;
import com.phoenix.police.FilesActivity;
import com.phoenix.police.MainScene;
import com.phoenix.police.R;

public class WifiActivity extends SlidingPreferenceActivity implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener, View.OnClickListener{

	private static final boolean LOG_SWITCH = Constants.LOG_SWITCH;
	private static final String LOG_TAG = WifiActivity.class.getSimpleName();
	private int[] wifiQuality = new int[]{
			R.string.wifi_1,
			R.string.wifi_2,
			R.string.wifi_3,
			R.string.wifi_4
	};
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
	
	
	private WifiManager mWifiManager;
	SwitchPreference _wifiSwitch;
	PreferenceCategory mWifiSearchCategory;
	private Handler mHandler;
	private ArrayList<AccessPoint> accessPoints = new ArrayList<AccessPoint>();
	private HashMap<String, ScanResult> mResults = new HashMap<String, ScanResult>();
	private SlidingMenu mainMenu;
	ConnectivityManager conn;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preferences_layout);
		addPreferencesFromResource(R.xml.wifi_preferences);
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		conn = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		_wifiSwitch = (SwitchPreference) findPreference("setting_wifi_switch_preference");
		State wifi = conn.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
		_wifiSwitch.setChecked(wifi == State.CONNECTED || wifi == State.CONNECTING);
		_wifiSwitch.setOnPreferenceChangeListener(this);
		mWifiSearchCategory = (PreferenceCategory) findPreference("setting_wifi_search_category");
		
		Button back = (Button) findViewById(R.id.back);
		back.setOnClickListener(this);
		
		HandlerThread hThread = new HandlerThread(WifiActivity.class.getSimpleName());
		hThread.start();
		mHandler = new Handler(hThread.getLooper());
		mHandler.post(scanWifiRun);
		
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
	protected void onStop() {
		super.onStop();
		mHandler.removeCallbacks(scanWifiRun);
	}
	@Override
	public boolean onPreferenceChange(Preference preference, Object arg1) {
		if(preference.getKey().equals("setting_wifi_switch_preference")){
			if(((SwitchPreference)preference).isChecked()){
				if(mWifiManager.isWifiEnabled()){
					mWifiManager.setWifiEnabled(false);
				}
				mWifiSearchCategory.removeAll();
			}else{
				if(!mWifiManager.isWifiEnabled()){
					mWifiManager.setWifiEnabled(true);
					mHandler.post(scanWifiRun);
				}
			}
		}
		return true;
	}
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		if(preference.getClass().getName() == AccessPoint.class.getName()){
			final ScanResult result = mResults.get(preference.getTitle());
			LayoutInflater factory = LayoutInflater.from(this);
			View view = factory.inflate(R.layout.pw_edit, null);
			((TextView)view.findViewById(R.id.leveldetail)).setText(wifiQuality[WifiManager.calculateSignalLevel(result.level, 4)]);
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
             if (existingConfig.SSID.contains(SSID))  
             {  
                 return existingConfig;  
             }  
           }  
        return null;   
    }
	
	//************************** Join network **************************************	
	private void updateAccessPoints(){
		int wifiState = mWifiManager.getWifiState();
		switch(wifiState){
			case WifiManager.WIFI_STATE_ENABLED:
				Collection<AccessPoint> accessPoints = constructAccessPoints();
				mWifiSearchCategory.removeAll();
				for(AccessPoint accessPoint : accessPoints){
					mWifiSearchCategory.addPreference(accessPoint);
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
					if (LOG_SWITCH) {
						Log.d(LOG_TAG, "a.SSID:" + a.SSID + " curSSID" + curSSID);
					}
					if(null != curSSID){
						if(("\"" + a.SSID + "\"").contains(curSSID)){
							return -1;
						}
						if(("\"" + b.SSID + "\"").contains(curSSID)){
							return 1;
						}
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
				int c = WifiManager.calculateSignalLevel(result.level, 4);
				accessPoint.setTitle(result.SSID);
				if(getSecurity(result) == 0){
					accessPoint.setIcon(wifiDrawableUnLock[c]);
				}else{
					accessPoint.setIcon(wifiDrawableLock[c]);
				}
				if(null != curSSID){
					if(("\"" + result.SSID + "\"").contains(curSSID)){
						accessPoint.setSummary(R.string.connected);
					}
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
	Runnable scanWifiRun = new Runnable() {
		@Override
		public void run() {
			if(_wifiSwitch.isChecked())
				updateAccessPoints();
			mHandler.postDelayed(scanWifiRun, 10000);
		}
	};

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
