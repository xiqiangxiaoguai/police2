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

public class AvCameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
	
	private static final String LOG_TAG = AvCameraSurfaceView.class.getSimpleName();
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
	public AvCameraSurfaceView(Context context) {
		super(context);
		init();
	}
	public AvCameraSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	public AvCameraSurfaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init(){
		holder = getHolder();// ���surfaceHolder����
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
				if(Camera.getNumberOfCameras() > 1){
					if (LOG_SWITCH) {
						Log.d(LOG_TAG, "getNumberOfCameras:"+ Camera.getNumberOfCameras());
					}
					myCamera = Camera.open(1);// �������,���ܷ��ڹ��캯���У���Ȼ������ʾ����.
				}
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
				parameters.setPictureFormat(PixelFormat.JPEG);// Sets the image format for picture �趨��Ƭ��ʽΪJPEG��Ĭ��ΪNV21
				parameters.setPreviewFormat(PixelFormat.YCbCr_420_SP);// Sets the image format for preview picture��Ĭ��ΪNV21
				/*
				 * ��ImageFormat��JPEG/NV16(YCrCb format��used for
				 * Video)/NV21(YCrCb format��used for Image)/RGB_565/YUY2/YU12
				 */

				// �����ԡ���ȡcaera֧�ֵ�PictrueSize�������ܷ����ã���
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
				// �������պ�Ԥ��ͼƬ��С
//				parameters.setPictureSize(2592, 1944); // ָ������ͼƬ�Ĵ�С
				parameters.setPictureSize(Constants.resolutions[mRes][0], Constants.resolutions[mRes][1]);
				if(mRes == 3)
				{
					parameters.setPreviewSize(640, 480); // ָ��preview�Ĵ�С
				}else{
					parameters.setPreviewSize(Constants.resolutions[mRes][0], Constants.resolutions[mRes][1]); // ָ��preview�Ĵ�С
				}
				
				// ���������� ����������������õĺ���ʵ�ֻ�Ĳ�һ��ʱ���ͻᱨ��

//				// ��������ͷ�Զ�����
//				if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
//					parameters.set("orientation", "landscape"); //
//					parameters.set("rotation", -90); // ��ͷ�Ƕ�ת90�ȣ�Ĭ������ͷ�Ǻ��ģ�
//					myCamera.setDisplayOrientation(180); // ��2.2���Ͽ���ʹ��
//				} else// ����Ǻ���
//				{
//					parameters.set("orientation", "landscape"); //
//					myCamera.setDisplayOrientation(0); // ��2.2���Ͽ���ʹ��
//				}

				/* ��Ƶ�����봦�� */
				// ��Ӷ���Ƶ�����?��
				// �趨���ò�����Ԥ��
				myCamera.setParameters(parameters); // ��Camera.Parameters�趨��Camera
				myCamera.startPreview(); // ��Ԥ������
				bIfPreview = true;

				// �����ԡ����ú��ͼƬ��С��Ԥ����С�Լ�֡��
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
//			parameters.setPreviewSize(Constants.resolutions[size][0], Constants.resolutions[size][1]); // ָ��preview�Ĵ�С
//			myCamera.setParameters(parameters);
//		}else if(flag == 1){
			mRes = 0;
			initCamera();
//		}
	}
	
	public int getRes(){
		return mRes;
	}
}