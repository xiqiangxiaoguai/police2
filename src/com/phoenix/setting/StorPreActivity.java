package com.phoenix.setting;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.phoenix.police.R;

public class StorPreActivity extends PreferenceActivity{

	public static final String KEY_STOR_TOTAL = "stor_total";
	public static final String KEY_STOR_CAMERA = "stor_camera";
	public static final String KEY_STOR_VIDEO = "stor_video";
	public static final String KEY_STOR_AUDIO = "stor_audio";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preferences_layout);
		
		Bundle data = getIntent().getExtras();
		
		addPreferencesFromResource(R.xml.stor_preferences);
		PreferenceScreen totalScreen = (PreferenceScreen) findPreference("preference_storage_total");
		PreferenceScreen cameraScreen = (PreferenceScreen) findPreference("preference_storage_camera");
		PreferenceScreen videoScreen = (PreferenceScreen) findPreference("preference_storage_video");
		PreferenceScreen audioScreen = (PreferenceScreen) findPreference("preference_storage_audio");
		
		totalScreen.setSummary(data.getString(KEY_STOR_TOTAL));
		cameraScreen.setSummary(data.getString(KEY_STOR_CAMERA));
		videoScreen.setSummary(data.getString(KEY_STOR_VIDEO));
		audioScreen.setSummary(data.getString(KEY_STOR_AUDIO));
	}
}
