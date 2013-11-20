package com.phoenix.police;

import com.phoenix.setting.PhoenixMethod;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;


public class UsbReceiver extends  BroadcastReceiver{
	static boolean two_flag = true;
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		final Context mContext = context;
		if (action.equals("android.hardware.usb.action.USB_STATE")) {
		        Bundle extras = intent.getExtras();
		        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		        if( extras.getBoolean("connected")){
		        	if(two_flag){
		        		two_flag = false;
						Intent i = new Intent("com.phoenix.police.UsbDialogActivity");
						i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						context.startActivity(i);
		        	}
//		        	new Thread(new Runnable() {
//						
//						@Override
//						public void run() {
//							try {
//								Thread.sleep(3000);
//							} catch (InterruptedException e) {
//								e.printStackTrace();
//							}
//							two_flag = true;
//						}
//					}).start();
		        }else{
		        	cm.setUsbTethering(false);
		        }
	}
	}
}