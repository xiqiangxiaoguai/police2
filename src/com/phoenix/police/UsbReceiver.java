package com.phoenix.police;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.Toast;

import com.phoenix.setting.PhoenixMethod;


public class UsbReceiver extends  BroadcastReceiver{
	static boolean two_flag = true;
	static int j = 0;
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals("android.hardware.usb.action.USB_STATE")) {
		        Bundle extras = intent.getExtras();
		        if( extras.getBoolean("connected")){
		        	if(two_flag){
			        	WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
						if(mWifiManager.isWifiEnabled()){
							mWifiManager.setWifiEnabled(false);
						}
						PhoenixMethod.set3G(false);
						ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
						cm.setUsbTethering(true);
			        	j++;
			        	Toast.makeText(context, R.string.usb_connected, Toast.LENGTH_SHORT).show();
			        	two_flag = false;
		        	}
		        	new Thread(new Runnable() {
						
						@Override
						public void run() {
							try {
								Thread.sleep(3000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							two_flag = true;
						}
					}).start();
		        }
	}
	}
}