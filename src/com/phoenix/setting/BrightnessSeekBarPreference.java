package com.phoenix.setting;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.DialogPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.phoenix.data.Constants;
import com.phoenix.police.R;

public class BrightnessSeekBarPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener{

	private static final String androidns="http://schemas.android.com/apk/res/android";
	private static final String LOG_TAG = BrightnessSeekBarPreference.class.getSimpleName();
	private static final boolean LOG_SWITCH = Constants.LOG_SWITCH;
	private SeekBar mSeekBar;
	private TextView mSplashText,mValueText;
	private Context mContext;
	private Activity mActivity;
	private String mDialogMessage, mSuffix;
	private int mDefault, mMax, mValue =0;
	
	private int pBright = 0;
	private AudioManager am;
	public BrightnessSeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		
		mDialogMessage = attrs.getAttributeValue(androidns, "dialogMessage");
		mSuffix = attrs.getAttributeValue(androidns, "text");
		mDefault = attrs.getAttributeIntValue(androidns, "defaultValue" , 60);
		mMax = attrs.getAttributeIntValue(androidns, "max", 100);
		
		am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
	}
	
	public void pushActivity(Activity activity){
		mActivity = activity;
	}
	@Override
	public void onClick(DialogInterface dialog, int which) {
		super.onClick(dialog, which);
		switch(which){
			case DialogInterface.BUTTON_NEGATIVE:
				Settings.System.putInt(mContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,pBright);
				Settings.System.putInt(mContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
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
		try {
			pBright = Settings.System.getInt(mContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
		int sBrightness = 100*pBright/255;
		layout.addView(mSeekBar, params);
		
//		if(shouldPersist())
//			mValue = getPersistedInt(mDefault);
		mValue = getSharedPreferences().getInt(Constants.SHARED_BRIGHTNESS, sBrightness);
		mSeekBar.setMax(mMax);
		mSeekBar.setProgress(mValue);
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
	    if (shouldPersist())
	      persistInt(value);
	    callChangeListener(new Integer(value));
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		int cur = mSeekBar.getProgress();
		if(cur < 10)
			cur = 10;
		Settings.System.putInt(mContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, (int)(((float)cur / 100.0f) * 255.0f));
		Settings.System.putInt(mContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
		LayoutParams lp=   mActivity.getWindow().getAttributes();
        lp.screenBrightness=((float)cur / 100.0f);
        mActivity.getWindow().setAttributes(lp);
        if (LOG_SWITCH) {
			Log.d(LOG_TAG, "set brightness:" + lp.screenBrightness);
		}
        SharedPreferences.Editor editor = getEditor();
        editor.putInt(Constants.SHARED_BRIGHTNESS, cur);
        editor.commit();
	}

	public void setProgress(int progress) { 
	    mValue = progress;
	    if (mSeekBar != null)
	      mSeekBar.setProgress(progress); 
	  }
	public int getProgress() { return mValue; }
	
	
}
