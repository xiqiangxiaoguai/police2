package com.phoenix.police;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class UsbDialogActivity extends Activity{
	
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
				cm.setUsbTethering(true);
				finish();
			}
		});
		Button btn2 = (Button) findViewById(R.id.negative);
		btn2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}
}
