package com.phoenix.setting;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.phoenix.online.A9TerminalActivity;
import com.phoenix.police.AudioActivity;
import com.phoenix.police.FilesActivity;
import com.phoenix.police.CameraActivity;
import com.phoenix.police.R;

public class StorPreActivity extends PreferenceActivity {

	public static final String KEY_STOR_TOTAL = "stor_total";
	public static final String KEY_STOR_CAMERA = "stor_camera";
	public static final String KEY_STOR_VIDEO = "stor_video";
	public static final String KEY_STOR_AUDIO = "stor_audio";
	@Override
	public void onCreate(Bundle savedInstanceState) {
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
		Button back = (Button) findViewById(R.id.back);
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}
	
}
