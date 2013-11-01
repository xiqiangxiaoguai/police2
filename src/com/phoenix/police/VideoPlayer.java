package com.phoenix.police;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class VideoPlayer extends Activity{

	private String url;
	private String name;
	PlayerView player;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Bundle bundle = getIntent().getExtras();
		url = bundle.getString("url");
		name = bundle.getString("name");
		player = new PlayerView(this, url, name);
		setContentView(player.getPlayerView());
	}
	
	@Override
	protected void onStart() {
		super.onStart();
//		player.stop();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(null != player & player.STATE == PlayerView.PLAYING)
			player.pause();
	}
}
