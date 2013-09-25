package com.phoenix.setting;

import com.phoenix.data.Constants;
import com.phoenix.police.R;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class VolumeSeekBarPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener{

	private static final String androidns="http://schemas.android.com/apk/res/android";
	private static final String LOG_TAG = VolumeSeekBarPreference.class.getSimpleName();
	private static final boolean LOG_SWITCH = Constants.LOG_SWITCH;
	
	private SeekBar mSeekBar;
	private TextView mSplashText,mValueText;
	private Context mContext;
	
	private String mDialogMessage, mSuffix;
	private int mDefault, mMax, mValue =0;
	
	private int pVolume = 0;
	private AudioManager am;
	public VolumeSeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		
		mDialogMessage = attrs.getAttributeValue(androidns, "dialogMessage");
		mSuffix = attrs.getAttributeValue(androidns, "text");
		mDefault = attrs.getAttributeIntValue(androidns, "defaultValue" , 0);
		mMax = attrs.getAttributeIntValue(androidns, "max", 100);
		
		am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
	}
	@Override
	public void onClick(DialogInterface dialog, int which) {
		super.onClick(dialog, which);
		switch(which){
			case DialogInterface.BUTTON_NEGATIVE:
				if (LOG_SWITCH) {
					Log.d(LOG_TAG, "Negative button pressed.Set volume back to " + pVolume);
				}
				for(int i : new int[]{AudioManager.STREAM_SYSTEM, AudioManager.STREAM_MUSIC, AudioManager.STREAM_NOTIFICATION})
				{
//					int max = am.getStreamMaxVolume(i);
					am.setStreamVolume(i, pVolume, 0);
				}
				break;
		}
	}
	@Override
	protected View onCreateDialogView() {

		LinearLayout.LayoutParams params;
		LinearLayout layout = new LinearLayout(mContext);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(6, 6, 6, 6);
		
//		mSplashText = new TextView(mContext);
//		if(mDialogMessage != null){
//			mSplashText.setText(mDialogMessage);
//		}
//		layout.addView(mSplashText);
		
		mValueText = new TextView(mContext);
		mValueText.setGravity(Gravity.CENTER_HORIZONTAL);
		mValueText.setTextSize(32);
		params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		layout.addView(mValueText, params);
		
		mSeekBar = new SeekBar(mContext);
		mSeekBar.setOnSeekBarChangeListener(this);
		pVolume = am.getStreamVolume(AudioManager.STREAM_SYSTEM);
		int sVolume = 100*pVolume/7;
		if (LOG_SWITCH) {
			Log.d(LOG_TAG, "getStreamVolume(AudioManager.STREAM_SYSTEM):" + pVolume);
		}
		layout.addView(mSeekBar, params);
		
//		if(shouldPersist())
//			mValue = getPersistedInt(mDefault);
		
		mSeekBar.setMax(mMax);
		mValue = getSharedPreferences().getInt(Constants.SHARED_VOLUME, sVolume);
		mSeekBar.setProgress(mValue);
		
		
		if (LOG_SWITCH) {
			Log.d(LOG_TAG, "get saved value in preference:" + mValue);
		}
		return layout;
	}
	
	@Override
	protected void onBindDialogView(View view) {
		// TODO Auto-generated method stub
		super.onBindDialogView(view);
		mSeekBar.setMax(mMax);
		mSeekBar.setProgress(mValue);
	}
	
	@Override
	protected void onSetInitialValue(boolean restorePersistedValue,
			Object defaultValue) {
		super.onSetInitialValue(restorePersistedValue, defaultValue);
		if (restorePersistedValue) 
	      mValue = shouldPersist() ? getPersistedInt(mDefault) : 0;
	    else 
	      mValue = (Integer)defaultValue;
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int value, boolean fromTouch) {
		String t = String.valueOf(value);
	    mValueText.setText(t + mContext.getResources().getString(R.string.baifenbi));
//	    if (shouldPersist())
//	      persistInt(value);
	    callChangeListener(new Integer(value));
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		int cur = mSeekBar.getProgress();
		for(int i : new int[]{AudioManager.STREAM_SYSTEM, AudioManager.STREAM_MUSIC, AudioManager.STREAM_NOTIFICATION})
		{
			int max = am.getStreamMaxVolume(i);
			am.setStreamVolume(i, (int)(max * ((double)cur/(double)100)), 0);
		}
		
		Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);//系统自带提示音
		Ringtone rt = RingtoneManager.getRingtone(mContext, uri);
		rt.play();
		SharedPreferences.Editor editor = getEditor();
		editor.putInt(Constants.SHARED_VOLUME, cur);
		editor.commit();
		if (LOG_SWITCH) {
			Log.d(LOG_TAG, "SharedPreference set volume:" + cur);
		}
	}

	public void setProgress(int progress) { 
	    mValue = progress;
	    if (mSeekBar != null)
	      mSeekBar.setProgress(progress); 
	  }
	public int getProgress() { return mValue; }
	
	
}
