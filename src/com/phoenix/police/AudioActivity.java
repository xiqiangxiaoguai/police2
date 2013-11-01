package com.phoenix.police;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
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
import com.phoenix.lib.SlidingMenu;
import com.phoenix.setting.PhoenixMethod;
import com.phoenix.setting.SettingActivity;

public class AudioActivity extends Activity implements OnClickListener {
	/** Called when the activity is first created. */

	private static final int STATE_IDLE = 0;
	private static final int STATE_RECORDING = 1;
	private int mState = STATE_IDLE;
	private ImageButton btnRecord;
	private int cSecs =0;
	private TextView timeCount;
	private SlidingMenu mainMenu = null;
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
				startRecord();
				mState = STATE_RECORDING;
				startTimer();
				timeBar.setVisibility(View.VISIBLE);
				mHandler.sendEmptyMessage(0);
				PhoenixMethod.setAudioLed(true);
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
			mainMenu.setSlidingEnabled(false);
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
			mainMenu.setSlidingEnabled(true);
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
		
		mainMenu = new SlidingMenu(this);
		mainMenu.setMode(SlidingMenu.LEFT);
		mainMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		mainMenu.setShadowWidthRes(R.dimen.shadow_width);
//        menu.setShadowDrawable(R.drawable.shadow);
		mainMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		mainMenu.setFadeDegree(0.35f);
		mainMenu.setDragEnabled(false);
		mainMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
		mainMenu.setMenu(R.layout.main_menus);
		RelativeLayout mCameraMenu = (RelativeLayout) mainMenu.getMenu().findViewById(R.id.menu_camera);
		mCameraMenu.setOnClickListener(this);
		RelativeLayout mAudioMenu = (RelativeLayout) mainMenu.getMenu().findViewById(R.id.menu_audio);
		mAudioMenu.setOnClickListener(this);
		RelativeLayout mFilesMenu = (RelativeLayout) mainMenu.getMenu().findViewById(R.id.menu_files);
		mFilesMenu.setOnClickListener(this);
		RelativeLayout mSettingMenu = (RelativeLayout) mainMenu.getMenu().findViewById(R.id.menu_setting);
		mSettingMenu.setOnClickListener(this);
		RelativeLayout mWirelessMenu = (RelativeLayout) mainMenu.getMenu().findViewById(R.id.menu_wireless);
		mWirelessMenu.setOnClickListener(this);
		RelativeLayout mAvIn = (RelativeLayout) mainMenu.getMenu().findViewById(R.id.menu_av);
		mAvIn.setOnClickListener(this);
		if(getIntent() != null){
			if(getIntent().getExtras()!= null){
				if(getIntent().getExtras().getBoolean(Constants.AUTO_AUDIO, false)){
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
	
	private void startRecord(){
		AudioRecordFunc func = AudioRecordFunc.getInstance(this);
		func.startRecordAndFile();
	}
	private void stopRecord(){
		AudioRecordFunc func = AudioRecordFunc.getInstance(this);
		func.stopRecordAndFile();
		Toast.makeText(this, R.string.audio_success, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onClick(View view) {
		switch(view.getId()){
			case R.id.main_menu:
				if (mAudioLocked)
					break;
				mainMenu.toggle();
				break;
			case R.id.menu_camera:
				startActivity(new Intent(this, MainScene.class));
				break;
			case R.id.menu_audio:
				mainMenu.toggle();
				break;
			case R.id.menu_files:
				startActivity(new Intent(this, FilesActivity.class));
				break;
			case R.id.menu_setting:
				startActivity(new Intent(this, SettingActivity.class));
				break;
			case R.id.menu_wireless:
				break;
			case R.id.menu_av:
//				startActivity(new Intent(this, AvInActivity.class));
				break;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Intent intent;
		switch (keyCode) {
		case KeyEvent.KEYCODE_CAMERA:
			if(mState == STATE_RECORDING)
				break;
			intent = new Intent(this, MainScene.class);
			intent.putExtra(Constants.AUTO_VIDEO, false);
			startActivity(intent);
			break;
			
		case KeyEvent.KEYCODE_MEDIA_RECORD:
			if(mState == STATE_RECORDING)
				break;
			intent = new Intent(this, MainScene.class);
			intent.putExtra(Constants.AUTO_VIDEO, true);
			startActivity(intent);
			break;
			
		case KeyEvent.KEYCODE_MUSIC:
			if(mainMenu.isMenuShowing()){
				mainMenu.toggle();
			}
			audioEvent();
			break;
			
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}
	
}