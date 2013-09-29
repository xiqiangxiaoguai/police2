package com.phoenix.police;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.phoenix.data.Constants;

public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
	
	private static final String LOG_TAG = CameraSurfaceView.class.getSimpleName();
	private static final boolean LOG_SWITCH = Constants.LOG_SWITCH;
	
	public final static int FLASH_MODE_AUTO = 0;
	public final static int FLASH_MODE_ON = 1;
	public final static int FLASH_MODE_OFF = 2;
	public final static int FLASH_MODE_TORCH = 3;
	
	Handler mHandler = new Handler(){
		
	};
	
	private thread startPreviewRun = new thread();
	
	SurfaceHolder holder;
	Camera myCamera;
	boolean bIfPreview = false;
	int mRes = 3;
	public CameraSurfaceView(Context context) {
		super(context);
		init();
	}
	public CameraSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	public CameraSurfaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init(){
		holder = getHolder();// 获得surfaceHolder引用
		holder.addCallback(this); 
//		holder.setFixedSize(176, 144);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);// set display device typeb
	}
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mHandler.post(startPreviewRun);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}
	
	private class thread implements Runnable{

		@Override
		public void run() {
			if (myCamera == null) {
				myCamera = Camera.open();// 开启相机,不能放在构造函数中，不然不会显示画面.
				if(null == myCamera){
					if (LOG_SWITCH)
						Log.d(LOG_TAG, "ERROR: Camera == null!");
				}
				try {
					myCamera.setPreviewDisplay(holder);//set the surface to used for live preview
				} catch (IOException e) {
					e.printStackTrace();
					if(null != myCamera){
						myCamera.release();
						myCamera = null;
					}
				}
			}
			initCamera();
		}
		
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		stopPreview();
	}
	private void initCamera(){
		if(bIfPreview){
			myCamera.stopPreview();
		}
		if(null != myCamera){
			try {
				Camera.Parameters parameters = myCamera.getParameters();
				// parameters.setFlashMode("off");
				parameters.setPictureFormat(PixelFormat.JPEG);// Sets the image format for picture 设定相片格式为JPEG，默认为NV21
				parameters.setPreviewFormat(PixelFormat.YCbCr_420_SP);// Sets the image format for preview picture，默认为NV21
				/*
				 * 【ImageFormat】JPEG/NV16(YCrCb format，used for
				 * Video)/NV21(YCrCb format，used for Image)/RGB_565/YUY2/YU12
				 */

				// 【调试】获取caera支持的PictrueSize，看看能否设置？？
				List<Size> pictureSizes = myCamera.getParameters()
						.getSupportedPictureSizes();
				List<Size> previewSizes = myCamera.getParameters()
						.getSupportedPreviewSizes();
				List<Integer> previewFormats = myCamera.getParameters()
						.getSupportedPreviewFormats();
				List<Integer> previewFrameRates = myCamera.getParameters()
						.getSupportedPreviewFrameRates();
				Log.i(LOG_TAG + "initCamera", "cyy support parameters is ");
				Size psize = null;
				for (int i = 0; i < pictureSizes.size(); i++) {
					psize = pictureSizes.get(i);
					Log.i(LOG_TAG + "initCamera", "PictrueSize,width: "
							+ psize.width + " height" + psize.height);
				}
				for (int i = 0; i < previewSizes.size(); i++) {
					psize = previewSizes.get(i);
					Log.i(LOG_TAG + "initCamera", "PreviewSize,width: "
							+ psize.width + " height" + psize.height);
				}
				Integer pf = null;
				for (int i = 0; i < previewFormats.size(); i++) {
					pf = previewFormats.get(i);
					Log.i(LOG_TAG + "initCamera", "previewformates:" + pf);
				}
				//Set camera flash mode.
				parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
				//Set zoom 
				if(parameters.isSmoothZoomSupported()){
					Log.d("qiqi", "" + parameters.getMaxZoom());
					
				}
				// 设置拍照和预览图片大小
//				parameters.setPictureSize(2592, 1944); // 指定拍照图片的大小
				parameters.setPictureSize(Constants.resolutions[mRes][0], Constants.resolutions[mRes][1]);
				if(mRes == 3)
				{
					parameters.setPreviewSize(640, 480); // 指定preview的大小
				}else{
					parameters.setPreviewSize(Constants.resolutions[mRes][0], Constants.resolutions[mRes][1]); // 指定preview的大小
				}
				
				// 这两个属性 如果这两个属性设置的和真实手机的不一样时，就会报错

//				// 横竖屏镜头自动调整
//				if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
//					parameters.set("orientation", "landscape"); //
//					parameters.set("rotation", -90); // 镜头角度转90度（默认摄像头是横拍）
//					myCamera.setDisplayOrientation(180); // 在2.2以上可以使用
//				} else// 如果是横屏
//				{
//					parameters.set("orientation", "landscape"); //
//					myCamera.setDisplayOrientation(0); // 在2.2以上可以使用
//				}

				/* 视频流编码处理 */
				// 添加对视频流处理函数
				// 设定配置参数并开启预览
				myCamera.setParameters(parameters); // 将Camera.Parameters设定予Camera
				myCamera.startPreview(); // 打开预览画面
				bIfPreview = true;

				// 【调试】设置后的图片大小和预览大小以及帧率
				Camera.Size csize = myCamera.getParameters().getPreviewSize();
				Log.i(LOG_TAG + "initCamera", "after setting, previewSize:width: "
						+ csize.width + " height: " + csize.height);
				csize = myCamera.getParameters().getPictureSize();
				Log.i(LOG_TAG + "initCamera", "after setting, pictruesize:width: "
						+ csize.width + " height: " + csize.height);
				Log.i(LOG_TAG + "initCamera", "after setting, previewformate is "
						+ myCamera.getParameters().getPreviewFormat());
				Log.i(LOG_TAG + "initCamera", "after setting, previewframetate is "
						+ myCamera.getParameters().getPreviewFrameRate());
			} catch (Exception e)
			    { 
			     e.printStackTrace();
			    }
		}
	}
	public Camera getCamera(){
		return myCamera;
	}
	public void resumePreview(){
		if(myCamera != null)
			myCamera.startPreview();
		
	}
	public void startPreview(){
		if(myCamera == null)
			mHandler.post(startPreviewRun);
	}
	
	public void stopPreview(){
		mHandler.removeCallbacks(startPreviewRun);
		if(null != myCamera){
			myCamera.setPreviewCallback(null);
			myCamera.stopPreview();
			bIfPreview = false;
			myCamera.release();
			myCamera = null;
		}
	}
	public void setSize(int size, int flag){
		if (LOG_SWITCH) {
			Log.d(LOG_TAG, "setSize() size:" + size + " flag:" + flag );
		}
//		if(flag == 0 ){
//			mRes = size;
//			Parameters parameters = myCamera.getParameters();
//			parameters.setPictureSize(Constants.resolutions[size][0], Constants.resolutions[size][1]);
//			if(size == 3)
//				size = 0;
//			parameters.setPreviewSize(Constants.resolutions[size][0], Constants.resolutions[size][1]); // 指定preview的大小
//			myCamera.setParameters(parameters);
//		}else if(flag == 1){
			mRes = size;
			initCamera();
//		}
	}
	
	public int getRes(){
		return mRes;
	}
}