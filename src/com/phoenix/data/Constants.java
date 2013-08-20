package com.phoenix.data;

import com.phoenix.police.R;

public class Constants {
	public static final boolean LOG_SWITCH = true;
	
	public static final int resolution_with_1 = 320; 
	public static final int resolution_with_2 = 640;
	public static final int resolution_with_3 = 1280;
	public static final int resolution_with_4 = 1920;
	public static final int resolution_with_5 = 2592;
	
	public static final int resolution_height_1 = 240;
	public static final int resolution_height_2 = 480;
	public static final int resolution_height_3 = 720;
	public static final int resolution_height_4 = 1080;
	public static final int resolution_height_5 = 1944;
	
	public static final int[][] resolutions = 
			new int[][]{{resolution_with_1, resolution_height_1}
						,{resolution_with_2, resolution_height_2}
						,{resolution_with_3, resolution_height_3}
						,{resolution_with_4, resolution_height_4}
						,{resolution_with_5, resolution_height_5}};
	
	public static final String CAMERA_PATH = "/sdcard/police/camera/";
	public static final String VIDEO_PATH = "/sdcard/police/video/";
	public static final String VIDEO_THUMBNAIL_PATH = "/sdcard/police/video/thumbnail/";
	public static final String AUDIO_PATH = "/sdcard/police/audio/";
	
	public static final String DEVICE_NAME = "Phenix ÷≥÷÷’∂À";
	public static final String ANDROID_VERSION = "Android 4.1";
	public static final String VERSION = "V1.0";
	
	public static final int MODE_CAMERA = 0;
	public static final int MODE_VIDEO = 1;
	public static final int MODE_AUDIO = 2;
	
	public static final String SETTING_PREFERENCES = "setting_preferences";
	public static final String PREFERENCES_CAMERA_RESOLUTION = "camera_resolution";
	public static final String PREFERENCES_VIDEO_RESOLUTION = "video_resolution";
	public static final String PREFERENCES_FLASH_MODE = "flash_mode";
	
	public static final int[] flash_resource = new int[]{
			R.drawable.ic_flash_auto_holo_light
			,R.drawable.ic_flash_on_holo_light
			,R.drawable.ic_flash_off_holo_light
			};
	
}
