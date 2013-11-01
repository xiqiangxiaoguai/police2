package com.phoenix.police;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.phoenix.data.Constants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LogReceiver extends BroadcastReceiver{

	private SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd kk:mm:ss");
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		try {
			FileWriter fw = new FileWriter(Constants.LOG_PATH + Constants.LOG_NAME, true);
			fw.write(format.format(new Date()) + " ");
			fw.write(action);
			fw.write("\n");
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
