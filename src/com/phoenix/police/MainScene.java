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
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.provider.MediaStore.Video.Thumbnails;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.phoenix.data.Constants;

public class MainScene extends Activity implements OnClickListener{
	/** Called when the activity is first created. */
	private static final String LOG_TAG = MainScene.class.getSimpleName();
	private static final boolean LOG_SWITCH = Constants.LOG_SWITCH;
	
	private static final int STATE_IDLE = 0;
	private static final int STATE_RECORDING = 1;
	private int mState = STATE_IDLE;
	CameraSurfaceView mySurface;
	ImageButton bQiezi;
	ImageButton bFlashBtn;
	Button bSetting;
	Button bFiles;
	private boolean cameraBusy = false;
	private int cFlashMode = CameraSurfaceView.FLASH_MODE_AUTO;
	private String cameraPath = Constants.CAMERA_PATH;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
	public MediaRecorder mrec;
	private String cPath = null;
	
	private int cSecs =0;
	private TextView timeCount;
	
	private int MODE = Constants.MODE_CAMERA;
	SharedPreferences preferences;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_activity);
		createDirs();
		
		preferences = getSharedPreferences(Constants.SETTING_PREFERENCES, Context.MODE_PRIVATE);
		int resolution = preferences.getInt(Constants.PREFERENCES_CAMERA_RESOLUTION, -1);
		if(resolution == -1){
			preferences.edit().putInt(Constants.PREFERENCES_CAMERA_RESOLUTION, 5);
		}
		int flash_mode = preferences.getInt(Constants.PREFERENCES_FLASH_MODE, -1);
		if(flash_mode == -1){
			preferences.edit().putInt(Constants.PREFERENCES_FLASH_MODE, 0);
		}
		
		mySurface = new CameraSurfaceView(this);
		RelativeLayout cameraLayout = ( RelativeLayout) findViewById(R.id.camera);
		cameraLayout.setGravity(Gravity.CENTER);
		mySurface.setLayoutParams(new LayoutParams(480, (int)(480*((double)Constants.resolution_height_5/Constants.resolution_with_5))));
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(480, (int)(480*((double)Constants.resolution_height_5/Constants.resolution_with_5)));
		params.addRule(RelativeLayout.CENTER_IN_PARENT, -1);
		cameraLayout.setLayoutParams(params);
		cameraLayout.addView(mySurface, 0);
		
		initPreviewWidget();
		
		bQiezi = (ImageButton) findViewById(R.id.qiezi);
		bQiezi.setOnClickListener(this);
		bFlashBtn = (ImageButton) findViewById(R.id.flashmode);
		bFlashBtn.setOnClickListener(this);
		bSetting = (Button) findViewById(R.id.setting);
		bSetting.setOnClickListener(this);
		bFiles = (Button) findViewById(R.id.files);
		bFiles.setOnClickListener(this);
		
	}
	private void createDirs(){
		for(String str : new String[]{Constants.CAMERA_PATH, Constants.VIDEO_PATH, Constants.VIDEO_THUMBNAIL_PATH, Constants.AUDIO_PATH}){
			File floderPath = new File(str);
			if(!floderPath.exists()){
				floderPath.mkdirs();
			}
		}
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case R.id.qiezi:
			
			MODE = Constants.MODE_AUDIO;
			initPreviewWidget();
			
			
//			//Camera
//			if(cameraBusy == false){
//				cameraBusy = true;
//				Camera camera = mySurface.getCamera();
//				camera.takePicture(null, null, jpegCallback);
//			}
			
//			//Video
//			if(cameraBusy == false){
//				cameraBusy = true;
//				try {
//	                startRecording();
//	            } catch (Exception e) {
//	                mrec.release();
//	            }
//			}else{
//				cameraBusy = false;
//				mrec.stop();
//	            mrec.release();
//	            mrec = null;
//	            stopRecording();
//			}
			
			//Audio
			if(mState == STATE_IDLE){
				startRecord();
				mState = STATE_RECORDING;
				startTimer();
				mHandler.sendEmptyMessage(0);
			}else if(mState == STATE_RECORDING){ 
				stopRecord();
				mState = STATE_IDLE;
				mHandler.sendEmptyMessage(0);
				stopTimer();
			}
			
			break;
		case R.id.flashmode:
			switch (cFlashMode) {
			case CameraSurfaceView.FLASH_MODE_AUTO:
				cFlashMode = CameraSurfaceView.FLASH_MODE_ON;
				bFlashBtn.setImageResource(R.drawable.ic_flash_on_holo_light);
				mySurface.setFlashMode(cFlashMode);
				break;
			case CameraSurfaceView.FLASH_MODE_ON:
				cFlashMode = CameraSurfaceView.FLASH_MODE_OFF;
				bFlashBtn.setImageResource(R.drawable.ic_flash_off_holo_light);
				mySurface.setFlashMode(cFlashMode);
				break;
			case CameraSurfaceView.FLASH_MODE_OFF:
				cFlashMode = CameraSurfaceView.FLASH_MODE_AUTO;
				bFlashBtn.setImageResource(R.drawable.ic_flash_auto_holo_light);
				mySurface.setFlashMode(cFlashMode);
				break;
			default:
				break;
			}
			break;
		case R.id.files:
			startActivity(new Intent("com.phoenix.police.FilesActivity"));
			break;
		}
	}
	
	private void initPreviewWidget(){
		LinearLayout bar_widget = (LinearLayout)findViewById(R.id.bar_widget);
		LinearLayout bar_timer = (LinearLayout)findViewById(R.id.bar_timer);
		int resolution = preferences.getInt(Constants.PREFERENCES_CAMERA_RESOLUTION, 5);
		int flash_mode = preferences.getInt(Constants.PREFERENCES_FLASH_MODE, 0);
		TextView resoWidget = (TextView) findViewById(R.id.resolution);
		ImageButton flashWidget = (ImageButton) findViewById(R.id.flashmode);
		TextView recordTypeWidget = (TextView) findViewById(R.id.recordType);
		timeCount = (TextView) findViewById(R.id.timeCount);
		switch(MODE){
		case Constants.MODE_CAMERA:
			bar_widget.setVisibility(View.VISIBLE);
			bar_timer.setVisibility(View.GONE);
			resoWidget.setText(Constants.resolutions[resolution -1][0] + "¡Á" + Constants.resolutions[resolution -1][1]);
			flashWidget.setImageResource(Constants.flash_resource[flash_mode]);
			break;
		case Constants.MODE_VIDEO:
			bar_widget.setVisibility(View.VISIBLE);
			bar_timer.setVisibility(View.VISIBLE);
			
			recordTypeWidget.setText(R.string.video_recording);
			resoWidget.setText(Constants.resolutions[resolution -1][0] + "¡Á" + Constants.resolutions[resolution -1][1]);
			flashWidget.setImageResource(Constants.flash_resource[flash_mode]);
			break;
		case Constants.MODE_AUDIO:
			bar_widget.setVisibility(View.GONE);
			bar_timer.setVisibility(View.VISIBLE);
			
			recordTypeWidget.setText(R.string.audio_recording);
			break;
		}
	}
	
	//***********************************************************Camera**************************************************
	//ÅÄÕÕ »Øµ÷º¯Êý
	private PictureCallback jpegCallback = new PictureCallback(){
		public void onPictureTaken(byte[] data, Camera camera) {
			String path = save(data);
			mySurface.resumePreview();
			cameraBusy = false;
		}
	};
	//ÅÄÕÕ ±£´æÍ¼Æ¬
	private String save(byte[] data){
		if (LOG_SWITCH)
			Log.d(LOG_TAG, "Start to save the bitmap.");
		
		String path = cameraPath + dateFormat.format(new Date())+".jpg";
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
				if (LOG_SWITCH)
					Log.d(LOG_TAG, "Image captured successfully!");
			}else{
				Toast.makeText(this, R.string.storage_no_enough, 500).show();
				if (LOG_SWITCH)
					Log.d(LOG_TAG, "Image captured failed.Cause:Storage not enough.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (LOG_SWITCH)
				Log.d(LOG_TAG, "Image capture failed.Cause:" + e);
			return null;
		}
		return path;
	}
	//***********************************************************Camera**************************************************
	//***********************************************************Video**************************************************
	private void stopRecording(){
		File myCaptureFile = new File( Constants.VIDEO_THUMBNAIL_PATH + cPath.substring(cPath.lastIndexOf('/'),cPath.lastIndexOf('.')) + ".jpg");
		BufferedOutputStream bos;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(
					myCaptureFile));
			ThumbnailUtils.createVideoThumbnail(cPath,
					Thumbnails.MINI_KIND).compress(
					Bitmap.CompressFormat.JPEG, 80, bos);
			try {
				bos.flush();
				bos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
		cPath = Constants.VIDEO_PATH + format.format(new Date())+".3gp";
		
		Camera mCamera = mySurface.getCamera();
		SurfaceHolder surfaceHolder = mySurface.getHolder();
        mrec = new MediaRecorder();  // Works well
        mCamera.unlock();
        mrec.setOrientationHint(90);
        mrec.setCamera(mCamera);
        mrec.setPreviewDisplay(surfaceHolder.getSurface());
        mrec.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mrec.setAudioSource(MediaRecorder.AudioSource.MIC); 
        mrec.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        mrec.setPreviewDisplay(surfaceHolder.getSurface());
        mrec.setOutputFile(cPath); 

        mrec.prepare();
        mrec.start();
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
		}
	}
	
	private void startRecord(){
		AudioRecordFunc func = AudioRecordFunc.getInstance();
		func.startRecordAndFile();
	}
	private void stopRecord(){
		AudioRecordFunc func = AudioRecordFunc.getInstance();
		func.stopRecordAndFile();
	}
	//***********************************************************Audio**************************************************
}
