package com.phoenix.police;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.sax.StartElementListener;
import android.util.Log;
import android.widget.DialerFilter;


public class UsbReceiver extends  BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		 String action = intent.getAction();
		 Log.d("qiqi:", action);
		 
//		    if (action.equals("android.hardware.usb.action.USB_STATE")) {
//		        Bundle extras = intent.getExtras();
//		        if( extras.getBoolean("connected")){
//		        	Intent i = new Intent("com.phoenix.police.UsbDialogActivity");
//		        	i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		        	context.startActivity(i);
//		        }
//	}
	}
}