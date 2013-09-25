package com.phoenix.setting;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.phoenix.data.Constants;
import com.phoenix.police.R;

public class AboutPreActivity extends PreferenceActivity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preferences_layout);
		addPreferencesFromResource(R.xml.about_preferences);
		PreferenceScreen deviceName = (PreferenceScreen) findPreference("preference_about_device_name");
		PreferenceScreen platformName = (PreferenceScreen) findPreference("preference_android_version");
		PreferenceScreen versionName = (PreferenceScreen) findPreference("preference_version");
		
		deviceName.setSummary(R.string.device_name_detail);
		platformName.setSummary(Constants.ANDROID_VERSION);
		versionName.setSummary(Constants.VERSION);
		
		Button button = (Button) findViewById(R.id.back);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}
}
