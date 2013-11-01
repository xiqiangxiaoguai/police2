package com.phoenix.setting;

import android.os.SystemProperties;

public class PhoenixMethod {
	
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
}
