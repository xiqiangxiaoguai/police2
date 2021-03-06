package com.phoenix.police;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.phoenix.data.Constants;
import com.phoenix.online.A9TerminalActivity;
import com.phoenix.setting.PhoenixMethod;
import com.phoenix.setting.SettingActivity;

public class AudioActivity extends Activity implements OnClickListener{
	/** Called when the activity is first created. */

	private static final int STATE_IDLE = 0;
	private static final int STATE_RECORDING = 1;
	private int mState = STATE_IDLE;
	private ImageButton btnRecord;
	private int cSecs =0;
	private TextView timeCount;
	private boolean mKeyLockForFrequentClick =false;
	private boolean mAudioLocked =false;
	LinearLayout timeBar ;
	Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch(msg.what){
			case 0:
				if(null != btnRecord){
					if(mState == STATE_IDLE){
						btnRecord.setBackgroundResource(R.drawable.start);
					}else if( mState == STATE_RECORDING){
						btnRecord.setBackgroundResource(R.drawable.stop);
					}
				}
				break;
			case 1:
				cSecs ++;
				int hour = cSecs/3600;
				int min = (cSecs%3600)/60;
				int sec = cSecs%60;
				timeCount.setText(String.format("%1$02d:%2$02d:%3$02d",hour, min, sec));
				break;
			case 2:
				mKeyLockForFrequentClick = false;
//				startRecord();
//				mState = STATE_RECORDING;
//				startTimer();
//				timeBar.setVisibility(View.VISIBLE);
//				mHandler.sendEmptyMessage(0);
//				PhoenixMethod.setAudioLed(true);
				audioEvent();
				break;
			}
		};
	};
	
	private Timer timer = null;
	private TimerTask task = null;

	private void audioEvent(){
		if(mKeyLockForFrequentClick)
			return;
		mKeyLockForFrequentClick = true;
		//Audio
		if(mState == STATE_IDLE){
			mAudioLocked = true;
			startRecord();
			mState = STATE_RECORDING;
			startTimer();
			timeBar.setVisibility(View.VISIBLE);
			mHandler.sendEmptyMessage(0);
			PhoenixMethod.setAudioLed(true);
		}else if(mState == STATE_RECORDING){ 
			stopRecord();
			mState = STATE_IDLE;
			mHandler.sendEmptyMessage(0);
			stopTimer();
			PhoenixMethod.setAudioLed(false);
			timeBar.setVisibility(View.GONE);
			mAudioLocked = false;
		}
		mKeyLockForFrequentClick = false;
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.audio_activity);
		timeBar = (LinearLayout) findViewById(R.id.bar_timer);
		btnRecord = (ImageButton) findViewById(R.id.record);
		btnRecord.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				audioEvent();
			}
		});
		timeCount = (TextView) findViewById(R.id.timeCount);
		
		ImageButton mMainMenu = (ImageButton) findViewById(R.id.main_menu);
		mMainMenu.setOnClickListener(this);
		
		if(getIntent() != null){
			if(getIntent().getExtras()!= null){
				if(getIntent().getExtras().getBoolean(Constants.AUTO_AUDIO, false)){
					mKeyLockForFrequentClick= true;
					mHandler.sendEmptyMessageDelayed(2, 1000);
				}}}
	}
	
	private void startTimer(){
		cSecs = 0;
		if(null == timer){
			if(null == task){
				task = new TimerTask() {
					@Override
					public void run() {
						mHandler.sendEmptyMessage(1);
					}
				};
			}
		}
		timer = new Timer(true);
		timer.schedule(task, 1000, 1000);
	}
	private void stopTimer(){
		if(null != timer){
			task.cancel();
			task = null;
			timer.cancel();
			timer.purge();
			timer = null;
			mHandler.removeMessages(1);
			timeCount.setText(String.format("%1$02d:%2$02d:%3$02d",0, 0, 0));
		}
	}
	ExtAudioRecorder extAudioRecorder = null;
	class Run implements Runnable{

		@Override
		public void run() {
			extAudioRecorder = ExtAudioRecorder.getInstanse(false);
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
			String fileName = Constants.AUDIO_NAME_HEAD + PhoenixMethod.getDeviceID() + "_" + PhoenixMethod.getPoliceId() + "_" + dateFormat.format(new Date()) +".wav";
			File dir = new File(Constants.getAudioPath());
			if(dir.list() == null){
				dir.mkdirs();
			}
			File file=new File(Constants.getAudioPath()+fileName);
			extAudioRecorder.setOutputFile(Constants.getAudioPath()+fileName);
			extAudioRecorder.prepare();
			extAudioRecorder.start();
		}
		
	}
	
	private void startRecord(){
//		AudioRecordFunc func = AudioRecordFunc.getInstance(this);
//		func.startRecordAndFile();
		CameraActivity.checkAndMkdirs();
		new Thread(new Run()).start();
	}
	private void stopRecord(){
//		AudioRecordFunc func = AudioRecordFunc.getInstance(this);
//		func.stopRecordAndFile();
		if(null != extAudioRecorder){
			extAudioRecorder.stop();
			extAudioRecorder.release();
		}
		Toast.makeText(this, R.string.audio_success, Toast.LENGTH_SHORT).show();
	}

	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Intent intent;
		switch (keyCode) {
		case KeyEvent.KEYCODE_CAMERA:
			if(mState == STATE_RECORDING)
				break;
			intent = new Intent(this, CameraActivity.class);
			intent.putExtra(Constants.AUTO_VIDEO, false);
			startActivity(intent);
			break;
			
		case KeyEvent.KEYCODE_MEDIA_RECORD:
			if(mState == STATE_RECORDING)
				break;
			intent = new Intent(this, CameraActivity.class);
			intent.putExtra(Constants.AUTO_VIDEO, true);
			startActivity(intent);
			break;
			
		case KeyEvent.KEYCODE_MUSIC:
			audioEvent();
			break;
			
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.main_menu:
			if(!mAudioLocked)
				finish();
			break;
		}
	}
	
}