package com.phoenix.police;

import com.phoenix.setting.PhoenixMethod;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class UsbDialogActivity extends Activity{
        
	private BroadcastReceiver mReceiver;
	Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what){
			case 0:
				UsbReceiver.two_flag = true;
				break;
			}
		};
	};
        @Override
        protected void onCreate(Bundle savedInstanceState) {
                // TODO Auto-generated method stub 
                super.onCreate(savedInstanceState);
                setContentView(R.layout.usb_dialog_activity);
                Button btn = (Button) findViewById(R.id.positive);
                btn.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                        	ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                            WifiManager mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
    						if(mWifiManager.isWifiEnabled()){
    							mWifiManager.setWifiEnabled(false);
    						}
    						PhoenixMethod.set3G(false);
    						cm.setUsbTethering(true);
    						mHandler.sendEmptyMessageDelayed(0, 3000);
                            finish();
                            
                        }
                });
                Button btn2 = (Button) findViewById(R.id.negative);
                btn2.setOnClickListener(new OnClickListener() {
                        
                        @Override
                        public void onClick(View arg0) {
                        	mHandler.sendEmptyMessageDelayed(0, 3000);
                        	finish();
                        }
                });
                
                IntentFilter filter = new IntentFilter();
                filter.addAction("android.hardware.usb.action.USB_STATE");
                mReceiver = new BroadcastReceiver() {
					
					@Override
					public void onReceive(Context context, Intent intent) {
						String action = intent.getAction();
						if (action.equals("android.hardware.usb.action.USB_STATE")) {
							Bundle extras = intent.getExtras();
							if(!extras.getBoolean("connected")){
								UsbReceiver.two_flag = true;
								finish();
							}
						}
					}
				};
                registerReceiver(mReceiver, filter);
        }
        @Override
        protected void onStop() {
        	super.onStop();
        	if(null != mReceiver){
        		try {
        			unregisterReceiver(mReceiver);
				} catch (Exception e) {
				}
        	}
        }
}