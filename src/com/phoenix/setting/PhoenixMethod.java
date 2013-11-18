package com.phoenix.setting;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.phoenix.data.Constants;

import android.os.SystemProperties;

public class PhoenixMethod {
	static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	public static void set3G(boolean val){
		SystemProperties.set("sys.usi3gpower.config", val ? "on" : "off");
	}
	
	public static void setFlashLed(boolean val){
		SystemProperties.set("sys.supled.config", val ? "on" : "off");
	}
	
	public static void setVideoLed(boolean val){
		SystemProperties.set("sys.led.config", val ? "video_led_on" : "video_led_off");
	}
	
	public static void setAudioLed(boolean val){
		SystemProperties.set("sys.led.config", val ? "audio_led_on" : "audio_led_off");
	}
	public static String getPicTime(){
		return SystemProperties.get("media.PicTimeStamp", dateFormat.format(new Date()));
	}
	public static String getDeviceID(){
		return SystemProperties.get("sys.DeviceID.config", Constants.SHARED_DEV_NUM_DEF);
	}
	public static String getPoliceId(){
		return SystemProperties.get("sys.PoliceID.config", Constants.SHARED_POL_NUM_DEF);
	}
	public static String getPolicePS(){
		return SystemProperties.get("sys.PoliceIDPasswd.config", Constants.SHARED_POL_PS_DEF);
	}
}
