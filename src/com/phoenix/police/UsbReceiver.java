package com.phoenix.police;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.phoenix.setting.PhoenixMethod;


public class UsbReceiver extends  BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Log.d("qiqi:", action);
		if (action.equals("android.hardware.usb.action.USB_STATE")) {
			//¹Ø±ÕwifiºÍ3g
			WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			if(mWifiManager.isWifiEnabled()){
				mWifiManager.setWifiEnabled(false);
			}
			PhoenixMethod.set3G(false);
//		        Bundle extras = intent.getExtras();
//		        if( extras.getBoolean("connected")){
//		        	Intent i = new Intent("com.phoenix.police.UsbDialogActivity");
//		        	i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		        	context.startActivity(i);
//		        }
	}
	}
}