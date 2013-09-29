package com.phoenix.police;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.provider.MediaStore.Video.Thumbnails;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.phoenix.data.Constants;
import com.phoenix.lib.SlidingMenu;
import com.phoenix.lib.SlidingMenu.OnClosedListener;
import com.phoenix.lib.SlidingMenu.OnOpenedListener;
import com.phoenix.setting.PhoenixMethod;

public class MainScene extends Activity implements OnClickListener{
	/** Called when the activity is first created. */
	private static final String LOG_TAG = MainScene.class.getSimpleName();
	private static final boolean LOG_SWITCH = Constants.LOG_SWITCH;
	
	private static final int STATE_IDLE = 0;
	private static final int STATE_RECORDING = 1;
	private int mState = STATE_IDLE;
	CameraSurfaceView mySurface;
	ImageButton mQiezi;
	ImageButton mMainMenu;
//	ImageButton bFlashBtn;
//	Button bSetting;
//	Button bFiles;
	ImageView mPreview;
	private int cFlashMode = CameraSurfaceView.FLASH_MODE_OFF;
	private String cameraPath = Constants.CAMERA_PATH;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	public MediaRecorder mrec;
	private String cPath = null;
	
	private int cSecs =0;
	private TextView timeCount;
	
	private int MODE = Constants.MODE_CAMERA;
	SharedPreferences sharedPreferences;
	
	SlidingMenu mainMenu = null;
	
	private int resolution = 0;
	private int preRes = -1;
	private boolean mVideoKeyLocked = false;
	private boolean mAudioKeyLocked = false;
	private boolean mKeyLockForFrequentClick = false;
	private String police_num;
	private boolean appPaused = false;
	private String[] fold_paths = new String[]{Constants.CAMERA_PATH, Constants.VIDEO_PATH, Constants.VIDEO_THUMBNAIL_PATH, Constants.AUDIO_PATH, Constants.LOG_PATH};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.camera_activity);
		
		sharedPreferences = getSharedPreferences(Constants.SETTING_PREFERENCES, Context.MODE_PRIVATE);
		int flash_mode = sharedPreferences.getInt(Constants.PREFERENCES_FLASH_MODE, -1);
		if(flash_mode == -1){
			sharedPreferences.edit().putInt(Constants.PREFERENCES_FLASH_MODE, 0);
		}
		
		createSurfaceView();
		RelativeLayout cameraLayout = ( RelativeLayout) findViewById(R.id.camera_layout);
		if (LOG_SWITCH) {
			Log.d(LOG_TAG, "child count:" + cameraLayout.getChildCount());
		}
		initPreviewWidget();
		
		mMainMenu = (ImageButton) findViewById(R.id.main_menu);
		mMainMenu.setOnClickListener(this);
		mQiezi = (ImageButton) findViewById(R.id.qiezi);
		mQiezi.setOnClickListener(this);
		
		mPreview = (ImageView)findViewById(R.id.preview);
		mPreview.setOnClickListener(this);
		mainMenu = new SlidingMenu(this);
		mainMenu.setMode(SlidingMenu.LEFT);
		mainMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		mainMenu.setShadowWidthRes(R.dimen.shadow_width);
//        menu.setShadowDrawable(R.drawable.shadow);
		mainMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		mainMenu.setFadeDegree(0.35f);
		mainMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
		mainMenu.setMenu(R.layout.main_menus);
		mainMenu.setOnOpenedListener(new OnOpenedListener() {
			@Override
			public void onOpened() {
				onPause();
			}
		});
		mainMenu.setOnClosedListener(new OnClosedListener() {
			@Override
			public void onClosed() {
				if(null == mySurface){
					onResume();
				}
			}
		});
		RelativeLayout mCameraMenu = (RelativeLayout) mainMenu.getMenu().findViewById(R.id.menu_camera);
		mCameraMenu.setOnClickListener(this);
	}
	
	private void createSurfaceView(){
		mySurface = new CameraSurfaceView(this);
		RelativeLayout cameraLayout = ( RelativeLayout) findViewById(R.id.camera_layout);

		mySurface.setLayoutParams(new LayoutParams(480, (int)(480*Constants.resolutions[mySurface.getRes()][1]/Constants.resolutions[mySurface.getRes()][0])));
		RelativeLayout.LayoutParams containerParams = new RelativeLayout.LayoutParams(480, (int)(480*Constants.resolutions[mySurface.getRes()][1]/Constants.resolutions[mySurface.getRes()][0]));
		containerParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, -1);
		cameraLayout.setLayoutParams(containerParams);
		cameraLayout.addView(mySurface, 0);
//		mySurface = new CameraSurfaceView(this);
//		RelativeLayout cameraLayout = ( RelativeLayout) findViewById(R.id.camera_layout);
//		cameraLayout.setGravity(Gravity.CENTER);
//		mySurface.setLayoutParams(new LayoutParams(480, (int)(480*((double)Constants.resolution_height_4/Constants.resolution_with_4))));
//		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(480, (int)(480*((double)Constants.resolution_height_4/Constants.resolution_with_4)));
//		params.addRule(RelativeLayout.CENTER_IN_PARENT, -1);
//		cameraLayout.setLayoutParams(params);
//		cameraLayout.addView(mySurface, 0);
	}
	private void destoySurfaceView(){
		RelativeLayout cameraLayout = ( RelativeLayout) findViewById(R.id.camera_layout);
		cameraLayout.removeViewAt(0);
		if(null != mySurface){
			mySurface = null;
		}
	}
	@Override
	protected void onPause() {
		super.onPause();
		if (LOG_SWITCH) {
			Log.d(LOG_TAG, "onPause()");
		}
		appPaused = true;
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				if(null != mySurface)
					mySurface.stopPreview();
				destoySurfaceView();
				RelativeLayout cameraLayout = ( RelativeLayout) findViewById(R.id.camera_layout);
				if (LOG_SWITCH) {
					Log.d(LOG_TAG, "child count:" + cameraLayout.getChildCount());
				}
			}
		});
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(null == mySurface && appPaused){
//			mySurface.resumePreview();
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					createSurfaceView();
					RelativeLayout cameraLayout = ( RelativeLayout) findViewById(R.id.camera_layout);
					if (LOG_SWITCH) {
						Log.d(LOG_TAG, "child count:" + cameraLayout.getChildCount());
					}
				}
			});
			appPaused = false;
		}
		mHandler.postDelayed(dirRun, 3000);
		
	}
	
	Runnable dirRun = new Runnable() {
		@Override
		public void run() {
			for(String str : fold_paths){
				File floderPath = new File(str);
				if(!floderPath.exists()){
					floderPath.mkdirs();
				}
			}
			if(!new File(fold_paths[fold_paths.length -1]).exists()){
				mHandler.postDelayed(dirRun, 3000);
			}
		}
	};
	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case  R.id.preview:
			if (LOG_SWITCH) {
				Log.d(LOG_TAG, "preview clicked!");
			}
			if(MODE == Constants.MODE_CAMERA){
				setMode(Constants.MODE_VIDEO);
			}else{
				setMode(Constants.MODE_CAMERA);
			}
			break;
		case R.id.main_menu:
			if (LOG_SWITCH) {
				Log.d(LOG_TAG, "main_menu clicked!");
			}
			mainMenu.toggle();
			break;
		case R.id.menu_camera:
			mainMenu.toggle();
			break;
		case R.id.qiezi:
			if (LOG_SWITCH) {
				Log.d(LOG_TAG, "qiezi clicked!");
			}
			break;
		}
	}
	
	private void initPreviewWidget(){
		LinearLayout bar_timer = (LinearLayout)findViewById(R.id.bar_timer);
		resolution = Integer.valueOf(sharedPreferences.getString(Constants.PREFERENCES_RESOLUTION, "0"));
		int flash_mode = 1;
//		TextView resoWidget = (TextView) findViewById(R.id.resolution);
//		ImageButton flashWidget = (ImageButton) findViewById(R.id.flashmode);
		TextView recordTypeWidget = (TextView) findViewById(R.id.recordType);
		timeCount = (TextView) findViewById(R.id.timeCount);
		switch(MODE){
		case Constants.MODE_CAMERA:
			bar_timer.setVisibility(View.GONE);
//			resoWidget.setText(Constants.resolutions[3][0] + getResources().getString(R.string.plus) + Constants.resolutions[3][1]);
//			flashWidget.setImageResource(Constants.flash_resource[flash_mode]);
			break;
		case Constants.MODE_VIDEO:
			bar_timer.setVisibility(View.VISIBLE);
			
			recordTypeWidget.setText(R.string.video_recording);
//			resoWidget.setText(Constants.resolutions[resolution][0] + getResources().getString(R.string.plus) + Constants.resolutions[resolution ][1]);
//			flashWidget.setImageResource(Constants.flash_resource[flash_mode]);
			break;
		case Constants.MODE_AUDIO:
			bar_timer.setVisibility(View.VISIBLE);
			
			recordTypeWidget.setText(R.string.audio_recording);
			break;
		}
	}
	
	//***********************************************************Camera**************************************************
	//ÅÄÕÕ »Øµ÷º¯Êý
	private PictureCallback jpegCallback = new PictureCallback(){
		public void onPictureTaken(byte[] data, Camera camera) {
			final byte[] mData =  data;
			new Thread(new Runnable() {
				@Override
				public void run() {
					String path = save(mData);
				}
			}).start();
			mySurface.resumePreview();
			mKeyLockForFrequentClick = false;
		}
	};
	//ÅÄÕÕ ±£´æÍ¼Æ¬
	private String save(byte[] data){
		if (LOG_SWITCH)
			Log.d(LOG_TAG, "Start to save the bitmap.");
		police_num = sharedPreferences.getString(Constants.SHARED_POL_NUM, Constants.SHARED_POL_NUM_DEF);
		String path = cameraPath +Constants.CAMERA_NAME_HEAD + police_num + "_" + dateFormat.format(new Date())+".jpg";
		
		try {
			//if there is a sdcard
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
				if (LOG_SWITCH)
					Log.d(LOG_TAG, "There is a sdcard.");
				//if there is enough storage in the sdcard
				String storage = Environment.getExternalStorageDirectory().toString();
				StatFs fs = new StatFs(storage);
				long available = Math.abs(fs.getAvailableBlocks()*fs.getBlockSize());
				if(available<data.length){
					if (LOG_SWITCH)
						Log.d(LOG_TAG, "The available storage is not enough. Available:" + available + "( " + data.length + " required)");
					return null;
				}
				
				File file = new File(path);
				if(!file.exists())
					file.createNewFile();
				FileOutputStream fos = new FileOutputStream(file);
				Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
				fos.flush();
				fos.close();
//				Toast.makeText(this, R.string.camera_succcess, Toast.LENGTH_SHORT).show();
				if (LOG_SWITCH)
					Log.d(LOG_TAG, "Image captured successfully!");
				//Send broadcast for record in log.
				Intent intent = new Intent(Constants.ACTION_CAMERA_START);
				intent.putExtra("name", path );
				sendBroadcast(intent);
			}else{
//				Toast.makeText(this, R.string.storage_no_enough, 500).show();
				if (LOG_SWITCH)
					Log.d(LOG_TAG, "Image captured failed.Cause:Storage not enough.");
			}
		} catch (Exception e) {
			e.printStackTrace();
//			Toast.makeText(this, R.string.camera_fail, Toast.LENGTH_SHORT).show();
			if (LOG_SWITCH)
				Log.d(LOG_TAG, "Image capture failed.Cause:" + e);
			return null;
		}
		
		return path;
	}
	//***********************************************************Camera**************************************************
	//***********************************************************Video**************************************************
	private void stopRecording(String path){
		File myCaptureFile = new File( Constants.VIDEO_THUMBNAIL_PATH + path.substring(path.lastIndexOf('/'),path.lastIndexOf('.')) + ".jpg");
		BufferedOutputStream bos;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(
					myCaptureFile));
			ThumbnailUtils.createVideoThumbnail(path,
					Thumbnails.MINI_KIND).compress(
					Bitmap.CompressFormat.JPEG, 80, bos);
			try {
				bos.flush();
				bos.close();
			} catch (IOException e) {
				e.printStackTrace();
//				Toast.makeText(this, R.string.video_fail, Toast.LENGTH_SHORT).show();
			}
//			Toast.makeText(this, R.string.video_success, Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(Constants.ACTION_VIDEO_END);
			sendBroadcast(intent);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
//			Toast.makeText(this, R.string.video_fail, Toast.LENGTH_SHORT).show();
		}
	}
	private void startRecording() throws IOException 
    {
		mrec = new MediaRecorder();
		File folderFile = new File(Constants.VIDEO_PATH);
		if(!folderFile.exists()){
			folderFile.mkdirs();
		}
		File thumbnailFile = new File(Constants.VIDEO_THUMBNAIL_PATH);
		if(!thumbnailFile.exists()){
			thumbnailFile.mkdirs();
		}

		police_num = sharedPreferences.getString(Constants.SHARED_POL_NUM, Constants.SHARED_POL_NUM_DEF);
		cPath = Constants.VIDEO_PATH + Constants.VIDEO_NAME_HEAD + police_num + "_" + dateFormat.format(new Date()) +".mp4";

		Camera mCamera = mySurface.getCamera();
		SurfaceHolder surfaceHolder = mySurface.getHolder();
        mrec = new MediaRecorder();  // Works well
        mCamera.unlock();
        mrec.setCamera(mCamera);
        mrec.setPreviewDisplay(surfaceHolder.getSurface());
        mrec.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mrec.setAudioSource(MediaRecorder.AudioSource.MIC); 
//        mrec.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        mrec.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mrec.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mrec.setVideoSize(Constants.resolutions[resolution][0], Constants.resolutions[resolution][1]);
        if (LOG_SWITCH) {
			Log.d(LOG_TAG, "setVideoSize :" + resolution);
		}
        mrec.setVideoFrameRate(30);
        mrec.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
//        mrec.setAudioEncodingBitRate(96000);
//        mrec.setAudioSamplingRate(44100);
        mrec.setAudioChannels(1);
        mrec.setPreviewDisplay(surfaceHolder.getSurface());
        mrec.setOutputFile(cPath); 

        mrec.prepare();
        mrec.start();
        
        Intent intent = new Intent(Constants.ACTION_VIDEO_START);
        intent.putExtra("name", cPath);
        sendBroadcast(intent);
    }
	//***********************************************************Video**************************************************
	//***********************************************************Audio**************************************************
	private Timer timer = null;
	private TimerTask task = null;
	Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch(msg.what){
			case 1:
				cSecs ++;
				int hour = cSecs/3600;
				int min = (cSecs%3600)/60;
				int sec = cSecs%60;
				timeCount.setText(String.format("%1$02d:%2$02d:%3$02d",hour, min, sec));
				if(min == 10){
					mrec.stop();
		            mrec.release();
		            mrec = null;
					stopRecording(cPath);
					stopTimer();
					try {
		                startRecording();
						startTimer();
		            } catch (Exception e) {
		                mrec.release();
						stopTimer();
		            }
					
				}
				break;
			}
		};
	};
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
		Intent intent = new Intent(Constants.ACTION_AUDIO_START);
		intent.putExtra("name", func.NewAudioName);
		sendBroadcast(intent);
	}
	private void stopRecord(){
		AudioRecordFunc func = AudioRecordFunc.getInstance(this);
		func.stopRecordAndFile();
		Intent intent = new Intent(Constants.ACTION_AUDIO_END);
		sendBroadcast(intent);
	}
	//***********************************************************Audio**************************************************
	private void setMode(int mode){
//		if(MODE != mode){
//			MODE = mode;
//			if(null != mySurface){
//				destoySurfaceView();
//				RelativeLayout cameraLayout = ( RelativeLayout) findViewById(R.id.camera_layout);
//				if (LOG_SWITCH) {
//					Log.d(LOG_TAG, "create SurfaceView...");
//				}
//				createSurfaceView();
//			}
//			if (LOG_SWITCH) {
//				Log.d(LOG_TAG, "init preview widget...");
//			}
//			initPreviewWidget();
//		}else{
//			MODE = mode;
//			initPreviewWidget();
//		}
//		updateResForMode();
		MODE = mode;
		initPreviewWidget();
		
		updateResForMode();
	}
	private void updateResForMode(){
		if (LOG_SWITCH) {
			Log.d(LOG_TAG, "set size 480*" + (int)(480*Constants.resolutions[mySurface.getRes()][1]/Constants.resolutions[mySurface.getRes()][0]));
		}
		
		if(MODE == Constants.MODE_CAMERA || MODE == Constants.MODE_AUDIO){
			mySurface.setSize(3, 0);
		}else if( MODE == Constants.MODE_VIDEO){
			int i = Integer.valueOf(sharedPreferences.getString(Constants.PREFERENCES_RESOLUTION, "0"));
			if(preRes == i){
				mySurface.setSize(preRes,0);	
			}else{
				preRes = i;
				mySurface.setSize(preRes, 1);
			}
		}
		RelativeLayout cameraLayout = ( RelativeLayout) findViewById(R.id.camera_layout);
		mySurface.setLayoutParams(new RelativeLayout.LayoutParams(480, (int)(480*Constants.resolutions[mySurface.getRes()][1]/Constants.resolutions[mySurface.getRes()][0])));
		RelativeLayout.LayoutParams containerParams = new RelativeLayout.LayoutParams(480, (int)(480*Constants.resolutions[mySurface.getRes()][1]/Constants.resolutions[mySurface.getRes()][0]));
		containerParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, -1);
		cameraLayout.setLayoutParams(containerParams);
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_CAMERA:
			if(mKeyLockForFrequentClick)
				break;
			if(mAudioKeyLocked)
				break;
			if(mVideoKeyLocked){
				Camera camera = mySurface.getCamera();
				camera.takePicture(null, null, jpegCallback);
				break;
			}
			setMode(Constants.MODE_CAMERA);
			//Camera
			mKeyLockForFrequentClick = true;
			Camera camera = mySurface.getCamera();
			camera.takePicture(null, null, jpegCallback);
			break;
			
		case KeyEvent.KEYCODE_MEDIA_RECORD:
			//Video
			if(mKeyLockForFrequentClick)
				break;
			if(mAudioKeyLocked)
				break;
			mKeyLockForFrequentClick = true;
			if(mState == STATE_IDLE){ 
				if (LOG_SWITCH) {
					Log.d(LOG_TAG, "set mode...");
				}
				setMode(Constants.MODE_VIDEO);
				if(mVideoKeyLocked)
					break;
				try {
					if (LOG_SWITCH) {
						Log.d(LOG_TAG, "start recording...");
					}
	                startRecording();
					startTimer();
					mVideoKeyLocked = true;
	            } catch (Exception e) {
	                mrec.release();
					stopTimer();
	            }
				mState = STATE_RECORDING;
				PhoenixMethod.setVideoLed(true);
			}else if(mState == STATE_RECORDING){
				mrec.stop();
	            mrec.release();
	            mrec = null;
	            //Add into runnable.
//	            mHandler.post(new Runnable() {
//					@Override
//					public void run() {
//						stopRecording(cPath);
//					}
//				});
	            new Thread(new Runnable() {
					
					@Override
					public void run() {
						stopRecording(cPath);
					}
				}).start();
	            
				stopTimer();
				setMode(Constants.MODE_CAMERA);
				mVideoKeyLocked = false;
				mState = STATE_IDLE;
				PhoenixMethod.setVideoLed(false);
			}
			mKeyLockForFrequentClick = false;
			break;
			
		case KeyEvent.KEYCODE_MUSIC:
			if(mKeyLockForFrequentClick)
				break;
			if(mVideoKeyLocked)
				break;
			mKeyLockForFrequentClick = true;
			//Audio
			if(mState == STATE_IDLE){
				if(mAudioKeyLocked)
					break;
				setMode(Constants.MODE_AUDIO);
				startRecord();
				mState = STATE_RECORDING;
				startTimer();
				mHandler.sendEmptyMessage(0);
				mAudioKeyLocked = true;
				PhoenixMethod.setAudioLed(true);
			}else if(mState == STATE_RECORDING){ 
				stopRecord();
				mState = STATE_IDLE;
				mHandler.sendEmptyMessage(0);
				stopTimer();
				setMode(Constants.MODE_CAMERA);
				mAudioKeyLocked = false;
				PhoenixMethod.setAudioLed(false);
			}
			mKeyLockForFrequentClick = false;
			break;
			
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}
}
