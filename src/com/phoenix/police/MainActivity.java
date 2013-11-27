package com.phoenix.police;

import com.phoenix.online.A9TerminalActivity;
import com.phoenix.setting.SettingActivity;
import com.phoenix.widget.ColorRelativeLayout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;

public class MainActivity extends Activity implements OnClickListener{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main_menus);
		ColorRelativeLayout mCameraView = (ColorRelativeLayout) findViewById(R.id.menu_camera);
		mCameraView.setOnClickListener(this);
		ColorRelativeLayout mAvInView = (ColorRelativeLayout) findViewById(R.id.menu_av);
		mAvInView.setOnClickListener(this);
		ColorRelativeLayout mAudioView = (ColorRelativeLayout) findViewById(R.id.menu_audio);
		mAudioView.setOnClickListener(this);
		ColorRelativeLayout mFilesView = (ColorRelativeLayout) findViewById(R.id.menu_files);
		mFilesView.setOnClickListener(this);
		ColorRelativeLayout mSettingView = (ColorRelativeLayout) findViewById(R.id.menu_setting);
		mSettingView.setOnClickListener(this);
		ColorRelativeLayout mA9View = (ColorRelativeLayout) findViewById(R.id.menu_wireless);
		mA9View.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.menu_camera:
			startActivity(new Intent(this, CameraActivity.class));
			break;
		case R.id.menu_av:
			startActivity(new Intent(this, AvInActivity.class));
			break;
		case R.id.menu_audio:
			startActivity(new Intent(this, AudioActivity.class));
			break;
		case R.id.menu_files:
			startActivity(new Intent(this, FilesActivity.class));
			break;
		case R.id.menu_setting:
			startActivity(new Intent(this, SettingActivity.class));
			break;
		case R.id.menu_wireless:
			startActivity(new Intent(this, A9TerminalActivity.class));
			break;
		}
	}
}
