package com.phoenix.data;

import com.phoenix.police.R;
import com.phoenix.setting.PhoenixMethod;

public class Constants {
	public static final boolean LOG_SWITCH = true;
	
	public static final int resolution_with_1 = 720;
	public static final int resolution_with_2 = 1280;
	public static final int resolution_with_3 = 1920;
	public static final int resolution_with_4 = 2592;
	
	public static final int resolution_height_1 = 480;
	public static final int resolution_height_2 = 720;
	public static final int resolution_height_3 = 1080;
	public static final int resolution_height_4 = 1944;
	
	public static final int[][] resolutions = 
			new int[][]{
						{resolution_with_1, resolution_height_1}
						,{resolution_with_2, resolution_height_2}
						,{resolution_with_3, resolution_height_3}
						,{resolution_with_4, resolution_height_4}};
	
	public static final String CAMERA_PATH = "/mnt/sdcard/police/"+ PhoenixMethod.getPoliceId() + "/camera/";
	public static final String VIDEO_PATH = "/mnt/sdcard/police/"+ PhoenixMethod.getPoliceId() + "/video/";
	public static final String VIDEO_THUMBNAIL_PATH = "/mnt/sdcard/police/"+ PhoenixMethod.getPoliceId() + "/video/thumbnail/";
	public static final String AUDIO_PATH = "/mnt/sdcard/police/"+ PhoenixMethod.getPoliceId() + "/audio/";
	
	public static final String CAMERA_NAME_HEAD = "IMG_";
	public static final String VIDEO_NAME_HEAD = "VID_";
	public static final String AUDIO_NAME_HEAD = "AUD_";
	
	public static final String LOG_PATH = "/mnt/sdcard/police/"+ PhoenixMethod.getPoliceId() + "/log/";
	public static final String LOG_NAME = "log.txt";
	
	public static final String DEVICE_NAME = "Phenix手持终端";
	public static final String ANDROID_VERSION = "Android 4.1";
	public static final String VERSION = "V0.3.0";
	
	public static final int MODE_CAMERA = 0;
	public static final int MODE_VIDEO = 1;
	public static final int MODE_AUDIO = 2;
	
	public static final String SETTING_PREFERENCES = "com.phoenix.police_preferences";
	public static final String PREFERENCES_RESOLUTION = "setting_function_resolution";
	public static final String PREFERENCES_FLASH_MODE = "flash_mode";
	
	public static final int[] flash_resource = new int[]{
			R.drawable.ic_flash_on_holo_light
			,R.drawable.ic_flash_off_holo_light
			};
	
	public static final String ACTION_CAMERA_START = "com.phoenix.police.START_CAMERA";
	public static final String ACTION_VIDEO_START = "com.phoenix.police.START_VIDEO";
	public static final String ACTION_VIDEO_END = "com.phoenix.police.END_VIDEO";
	public static final String ACTION_AUDIO_START = "com.phoenix.police.START_AUDIO";
	public static final String ACTION_AUDIO_END = "com.phoenix.police.END_AUDIO";
	
	public static final String SHARED_VOLUME = "shared_volume";
	public static final String SHARED_BRIGHTNESS = "shared_brightness";
	
	public static final String SHARED_DEV_NUM = "device_num";
	public static final String SHARED_DEV_NUM_DEF = "DSJA900001";
	public static final String SHARED_POL_NUM = "police_num";
	public static final String SHARED_POL_NUM_DEF = "XXXX0001";
	public static final String SHARED_POL_PS_DEF ="123456";
	public static final String SHARED_PREVIEW_PATH = "preview";
	public static final int TIME_AUTO_SAVED = 10;//min
	
	public static final String AUTO_AUDIO ="auto_audio";
	public static final String AUTO_VIDEO = "auto_video";
	
}
