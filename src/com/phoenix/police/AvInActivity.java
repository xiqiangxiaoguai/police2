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
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.phoenix.data.Constants;
import com.phoenix.online.A9TerminalActivity;
import com.phoenix.setting.PhoenixMethod;
import com.phoenix.setting.SettingActivity;

public class AvInActivity extends Activity implements OnClickListener{
	/** Called when the activity is first created. */
	private static final String LOG_TAG = AvInActivity.class.getSimpleName();
	private static final boolean LOG_SWITCH = Constants.LOG_SWITCH;
	
	private static final int STATE_IDLE = 0;
	private static final int STATE_RECORDING = 1;
	private int mState = STATE_IDLE;
	AvCameraSurfaceView mySurface;
	ImageButton mQiezi;
	ImageButton mMainMenu;
	ImageButton mModeSwitch;
//	ImageButton bFlashBtn;
//	Button bSetting;
//	Button bFiles;
	ImageView mPreview;
	private String cameraPath = Constants.getCameraPath();
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	public MediaRecorder mrec;
	private String cPath = null;
	
	private int cSecs =0;
	private TextView timeCount;
	
	private int MODE = Constants.MODE_CAMERA;
	SharedPreferences sharedPreferences;
	
	LinearLayout bar_timer;
	private int resolution = 0;
	private int preRes = -1;
	private boolean mVideoKeyLocked = false;
	private boolean mKeyLockForFrequentClick = false;
	private String police_num;
	private boolean appPaused = false;
	private String[] fold_paths = new String[]{Constants.getCameraPath(), Constants.getVideoPath(), Constants.getThumbnailPath(), Constants.getAudioPath(), Constants.getLogPath()};
	
	private ImageLoader imageLoader;
	
	private String previewImagePath;
	DisplayImageOptions options = new DisplayImageOptions.Builder()
	.showImageForEmptyUri(R.drawable.image_loading)
	.imageScaleType(ImageScaleType.EXACTLY)
	.showStubImage(R.drawable.image_loading)
	.cacheInMemory().cacheOnDisc().build(); 
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.camera_activity);
		
		sharedPreferences = getSharedPreferences(Constants.SETTING_PREFERENCES, Context.MODE_PRIVATE);
		
		createSurfaceView();
		RelativeLayout cameraLayout = ( RelativeLayout) findViewById(R.id.camera_layout);
		if (LOG_SWITCH) {
			Log.d(LOG_TAG, "child count:" + cameraLayout.getChildCount());
		}
		initPreviewWidget();
		
		imageLoader = ImageLoader.getInstance();
		mMainMenu = (ImageButton) findViewById(R.id.main_menu);
		mMainMenu.setOnClickListener(this);
		mModeSwitch = (ImageButton) findViewById(R.id.mode);
		mModeSwitch.setOnClickListener(this);
		mQiezi = (ImageButton) findViewById(R.id.qiezi);
		mQiezi.setOnClickListener(this);
		
		mPreview = (ImageView)findViewById(R.id.preview);
		mPreview.setOnClickListener(this);
		bar_timer = (LinearLayout)findViewById(R.id.bar_timer);
		timeCount = (TextView) findViewById(R.id.timeCount);
		bar_timer.setVisibility(View.GONE);
		
	}
	@Override
	protected void onStop() {
		super.onStop();
		imageLoader.stop();
	}
	private void createSurfaceView(){
		mySurface = new AvCameraSurfaceView(this);
		RelativeLayout cameraLayout = ( RelativeLayout) findViewById(R.id.camera_layout);
		mySurface.setSize(0, 0);
		mySurface.setLayoutParams(new LayoutParams(480, (int)(480*Constants.resolutions[0][1]/Constants.resolutions[0][0])));
		RelativeLayout.LayoutParams containerParams = new RelativeLayout.LayoutParams(480, (int)(480*Constants.resolutions[0][1]/Constants.resolutions[0][0]));
		containerParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, -1);
		cameraLayout.setLayoutParams(containerParams);
		cameraLayout.addView(mySurface, 0);
	}
	private void destoySurfaceView(){
		RelativeLayout cameraLayout = ( RelativeLayout) findViewById(R.id.camera_layout);
		if(cameraLayout != null & cameraLayout.getChildCount() == 2)
			cameraLayout.removeViewAt(0);
		if(null != mySurface){
			new Thread(new Runnable() {
				@Override
				public void run() {
					mySurface.stopPreview();
					mySurface = null;
				}
			}).start();
		}
	}
	@Override
	protected void onPause() {
		super.onPause();
		if (LOG_SWITCH) {
			Log.d(LOG_TAG, "onPause()");
		}
		appPaused = true;
		destoySurfaceView();
		
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
		previewImagePath = sharedPreferences.getString(Constants.SHARED_PREVIEW_PATH, "");
		if(!previewImagePath.contains(PhoenixMethod.getPoliceId())){
			previewImagePath = "";
		}
		mHandler.sendEmptyMessage(2);
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
		case R.id.main_menu:
			finish();
			break;
		case  R.id.preview:
			if(null != previewImagePath){
				if (LOG_SWITCH) {
					Log.d(LOG_TAG, "previewImagePath:" + previewImagePath.split("\\.")[1]);
				}
				if(previewImagePath.contains("camera")){
					Intent intent = new Intent("com.phoenix.police.CameraBrowseActivity");
					Bundle bundle = new Bundle();
					FileHelper helper = new FileHelper();
					helper.query(0);
					bundle.putStringArrayList("cameraPaths", helper.getUrls());
					bundle.putInt("currentPic", 0);
					intent.putExtras(bundle);
					startActivity(intent);
				}else{
					Intent intent = new Intent("com.phoenix.police.VideoPlayer");
					Bundle bundle = new Bundle();
					FileHelper helper = new FileHelper();
					helper.query(1);
					String cUrl = helper.getUrls().get(0);
					
					bundle.putString("url", cUrl);
					bundle.putString("name", cUrl.substring(cUrl.lastIndexOf('/'), cUrl.lastIndexOf('.')));
					intent.putExtras(bundle);
					startActivity(intent);
				}
			}
			break;
		case R.id.qiezi:
			if(MODE == Constants.MODE_CAMERA){
				cameraEvent();
			}else{
				videoEvent();
			}
			break;
		case R.id.mode:
			if(mVideoKeyLocked)
				break;
			if(MODE == Constants.MODE_CAMERA){
				setMode(Constants.MODE_VIDEO);
			}else{
				setMode(Constants.MODE_CAMERA);
			}
			break;
		}
	}
	
	private void initPreviewWidget(){

	}
	
	//***********************************************************Camera**************************************************
	//���� �ص�����
	private PictureCallback jpegCallback = new PictureCallback(){
		public void onPictureTaken(byte[] data, Camera camera) {
			final byte[] mData =  data;
			
			new Thread(new Runnable() {
				@Override
				public void run() {
					previewImagePath = save(mData);
					sharedPreferences.edit().putString(Constants.SHARED_PREVIEW_PATH, previewImagePath).commit();
					mHandler.sendEmptyMessage(2);
				}
				
			}).start();
			mySurface.resumePreview();
			mKeyLockForFrequentClick = false;
		}
	};
	//���� ����ͼƬ
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
	class StopRecordingRun implements Runnable{
		public String path;
		@Override
		public void run() {
			File myCaptureFile = new File( Constants.getThumbnailPath() + path.substring(path.lastIndexOf('/'),path.lastIndexOf('.')) + ".jpg");
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
//					Toast.makeText(this, R.string.video_fail, Toast.LENGTH_SHORT).show();
				}
//				Toast.makeText(this, R.string.video_success, Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(Constants.ACTION_VIDEO_END);
				sendBroadcast(intent);
				previewImagePath = myCaptureFile.getAbsolutePath();
				sharedPreferences.edit().putString(Constants.SHARED_PREVIEW_PATH, previewImagePath).commit();
				mHandler.sendEmptyMessage(4);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
//				Toast.makeText(this, R.string.video_fail, Toast.LENGTH_SHORT).show();
			}
		}
		
	}
	private void stopRecording(String path){
		StopRecordingRun mStopRecordingRun = new StopRecordingRun();
		mStopRecordingRun.path = path;
		new Thread(mStopRecordingRun).start();

	}
	private void startRecording() throws IOException 
    {
		mrec = new MediaRecorder();
		File folderFile = new File(Constants.getVideoPath());
		if(!folderFile.exists()){
			folderFile.mkdirs();
		}
		File thumbnailFile = new File(Constants.getThumbnailPath());
		if(!thumbnailFile.exists()){
			thumbnailFile.mkdirs();
		}
		resolution = Integer.parseInt(sharedPreferences.getString("setting_function_resolution", "0"));
		police_num = sharedPreferences.getString(Constants.SHARED_POL_NUM, Constants.SHARED_POL_NUM_DEF);
		cPath = Constants.getVideoPath() + Constants.VIDEO_NAME_HEAD + PhoenixMethod.getDeviceID() + "_" + PhoenixMethod.getPoliceId() + "_" + dateFormat.format(new Date()) +".mp4";
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
        mrec.setVideoEncodingBitRate(12000000);
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
			case 2:
				imageLoader.displayImage("file:/" + previewImagePath, mPreview ,options,null);
				break;
			case 3:
				videoEvent();
				break;
			case 4:
				imageLoader.displayImage("file:/" + previewImagePath, mPreview ,options,null);
				Toast.makeText(AvInActivity.this, R.string.video_success, Toast.LENGTH_SHORT).show();
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
		if(mode == Constants.MODE_CAMERA){
			mModeSwitch.setImageResource(R.drawable.mode_video);
		}else if (mode == Constants.MODE_VIDEO){
			mModeSwitch.setImageResource(R.drawable.mode_camera);
		}
		initPreviewWidget();
		updateResForMode();
	}
	private void updateResForMode(){
		if (LOG_SWITCH) {
			Log.d(LOG_TAG, "set size 480*" + (int)(480*Constants.resolutions[0][1]/Constants.resolutions[0][0]));
		}
		
		if(MODE == Constants.MODE_CAMERA || MODE == Constants.MODE_AUDIO){
			mySurface.setSize(0, 0);
		}else if( MODE == Constants.MODE_VIDEO){
			int i = Integer.valueOf(sharedPreferences.getString(Constants.PREFERENCES_RESOLUTION, "0"));
			if(preRes == i){
				mySurface.setSize(0,0);	
			}else{
				preRes = i;
				mySurface.setSize(0, 1);
			}
		}
		RelativeLayout cameraLayout = ( RelativeLayout) findViewById(R.id.camera_layout);
		mySurface.setLayoutParams(new RelativeLayout.LayoutParams(480, (int)(480*Constants.resolutions[0][1]/Constants.resolutions[0][0])));
		RelativeLayout.LayoutParams containerParams = new RelativeLayout.LayoutParams(480, (int)(480*Constants.resolutions[0][1]/Constants.resolutions[0][0]));
		containerParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, -1);
		cameraLayout.setLayoutParams(containerParams);
	}
	private void cameraEvent(){
		if(mKeyLockForFrequentClick)
			return;
		if(mVideoKeyLocked){
			Camera camera = mySurface.getCamera();
			camera.takePicture(null, null, jpegCallback);
			return;
		}
		setMode(Constants.MODE_CAMERA);
		//Camera
		mKeyLockForFrequentClick = true;
		Camera camera = mySurface.getCamera();
		camera.takePicture(null, null, jpegCallback);
	}
	private void videoEvent(){
		//Video
		if(mKeyLockForFrequentClick)
			return;
		mKeyLockForFrequentClick = true;
		if(mState == STATE_IDLE){ 
			if (LOG_SWITCH) {
				Log.d(LOG_TAG, "set mode...");
			}
			setMode(Constants.MODE_VIDEO);
			if(mVideoKeyLocked)
				return;
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
			bar_timer.setVisibility(View.VISIBLE);
			mState = STATE_RECORDING;
			PhoenixMethod.setVideoLed(true);
		}else if(mState == STATE_RECORDING){
			mrec.stop();
            mrec.release();
            mrec = null;
            //Add into runnable.
//            mHandler.post(new Runnable() {
//				@Override
//				public void run() {
//					stopRecording(cPath);
//				}
//			});
            new Thread(new Runnable() {
				
				@Override
				public void run() {
					stopRecording(cPath);
				}
			}).start();
            bar_timer.setVisibility(View.GONE);
			stopTimer();
			mVideoKeyLocked = false;
			mState = STATE_IDLE;
			PhoenixMethod.setVideoLed(false);
		}
		mKeyLockForFrequentClick = false;
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_CAMERA:
			cameraEvent();
			break;
			
		case KeyEvent.KEYCODE_MEDIA_RECORD:
			mHandler.removeMessages(3);
			videoEvent();
			break;
			
		case KeyEvent.KEYCODE_MUSIC:
			if(mVideoKeyLocked)
				break;
			Intent intent = new Intent("com.phoenix.police.AudioActivity");
			intent.putExtra(Constants.AUTO_AUDIO, true);
			startActivity(intent);
			break;
			
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}
}
