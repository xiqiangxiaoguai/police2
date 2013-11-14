package com.phoenix.online;

import android.media.AudioManager;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.util.Log;

import a9.terminal.Login;
import a9.terminal.Login.EXmppState;
import a9.terminal.Presence;
import a9.terminal.Presence.EPresStatus;
import a9.terminal.PeerConnection;
import a9.terminal.PeerConnection.I420Frame;

import com.phoenix.lib.SlidingMenu;
import com.phoenix.lib.app.SlidingActivity;
import com.phoenix.police.AudioActivity;
import com.phoenix.police.FilesActivity;
import com.phoenix.police.MainScene;
import com.phoenix.police.R;
import com.phoenix.setting.SettingActivity;
public class A9TerminalActivity extends SlidingActivity 
implements Login.IXmppStateObserver
, Presence.IPresenceStatusObserver 
, PeerConnection.IPeerConnectionObserver
, PeerConnection.IVideoRenderer
, OnClickListener
{
	private static String TAG = "A9TerminalActivity";
	
	private final Login          mLogin          = new Login();
	private final Presence       mPresence       = new Presence();
	private final PeerConnection mPeerConnection = new PeerConnection();
	private A9TerminalActivity   Observer        = this;
	private boolean              mBIsStarted     = false;
	private VideoRendererView    mVideoRenderer;
//	private VideoStreamsView     mVideoStreamView;
	private SlidingMenu mainMenu = null;
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
	    Thread.setDefaultUncaughtExceptionHandler(
	            new UnhandledExceptionHandler(this));
			    
//	    Point displaySize = new Point();
//	    getWindowManager().getDefaultDisplay().getSize(displaySize);
//	    mVideoStreamView = new VideoStreamsView(this, displaySize);
		setContentView(R.layout.a9_activity);
		//PeerConnection.InitAndroidGlobals(this);
	    abortUnless(PeerConnection.InitAndroidGlobals(this), "Failed to initializeAndroidGlobals");

	    AudioManager audioManager = ((AudioManager)getSystemService(AUDIO_SERVICE));
	    @SuppressWarnings("deprecation")
	    boolean isWiredHeadsetOn = audioManager.isWiredHeadsetOn();
	    audioManager.setMode(isWiredHeadsetOn ? AudioManager.MODE_IN_CALL : AudioManager.MODE_IN_COMMUNICATION);
	    audioManager.setSpeakerphoneOn(!isWiredHeadsetOn);

	    final Intent intent = getIntent();
	    if ("android.intent.action.VIEW".equals(intent.getAction())) 
	    {
	      return;
	    }
	    
	    setBehindContentView(R.layout.main_menus);
		mainMenu = getSlidingMenu();
		mainMenu.setMode(SlidingMenu.LEFT);
		mainMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		mainMenu.setShadowWidthRes(R.dimen.shadow_width);
//        menu.setShadowDrawable(R.drawable.shadow);
		mainMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		mainMenu.setDragEnabled(false);
		mainMenu.setFadeDegree(0.35f);
		setSlidingActionBarEnabled(true);
		RelativeLayout mCameraMenu = (RelativeLayout) mainMenu.getMenu().findViewById(R.id.menu_camera);
		mCameraMenu.setOnClickListener(this);
		RelativeLayout mAudioMenu = (RelativeLayout) mainMenu.getMenu().findViewById(R.id.menu_audio);
		mAudioMenu.setOnClickListener(this);
		RelativeLayout mFilesMenu = (RelativeLayout) mainMenu.getMenu().findViewById(R.id.menu_files);
		mFilesMenu.setOnClickListener(this);
		RelativeLayout mSettingMenu = (RelativeLayout) mainMenu.getMenu().findViewById(R.id.menu_setting);
		mSettingMenu.setOnClickListener(this);
		RelativeLayout mWirelessMenu = (RelativeLayout) mainMenu.getMenu().findViewById(R.id.menu_wireless);
		mWirelessMenu.setOnClickListener(this);
		RelativeLayout mAvIn = (RelativeLayout) mainMenu.getMenu().findViewById(R.id.menu_av);
		mAvIn.setOnClickListener(this);
		
	    showLoginUI();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
			case R.id.menu_camera:
				startActivity(new Intent(this, MainScene.class));
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
				mainMenu.toggle();
				break;
			case R.id.menu_av:
	//			startActivity(new Intent(this, AvInActivity.class));
				break;
		}
	}
	  private void showLoginUI() 
	  {
		    AlertDialog.Builder builder = new AlertDialog.Builder(this);
		    LayoutInflater inflater = getLayoutInflater();
		    View loginView = inflater.inflate(R.layout.activity_a9_terminal, null);

			final EditText etUsrName  = (EditText) loginView.findViewById(R.id.tf_userName);
			final EditText etPassword = (EditText) loginView.findViewById(R.id.tf_password);
			final EditText etServerIP = (EditText) loginView.findViewById(R.id.tf_serverIP);
			etUsrName.setText("ter2");
			etPassword.setText("ter2");
			etServerIP.setText("120.236.21.179");
		    DialogInterface.OnClickListener listener =
		            new DialogInterface.OnClickListener()
		    {
		              @Override public void onClick(DialogInterface dialog, int which) 
		              {
		                abortUnless(which == DialogInterface.BUTTON_POSITIVE, "lolwat?");
		                dialog.dismiss();
		                mLogin.DoLogin(Observer, etUsrName.getText().toString(), 
								etPassword.getText().toString(),
								etServerIP.getText().toString(), 
								"A9Terminal");
		              }
		     };

		    builder.setTitle("用户登录")
		           .setView(loginView)
		           .setPositiveButton(R.string.login, listener)
		           .setCancelable(false)
		           .show();
		  }
	  
	private static void abortUnless(boolean condition, String msg) 
	{
	    if (!condition) 
	    {
	      throw new RuntimeException(msg);
	    }
	}
	
	@Override
	protected void onPause() 
	{
		// TODO Auto-generated method stub
		super.onPause();
	}


	@Override
	public void XmppStateChange(EXmppState newState) 
	{
		// TODO Auto-generated method stub
		switch (newState) 
		{						
		case E_XMPP_STATE_START:
			break;
			
		case E_XMPP_STATE_OPENING:
			break;

		case E_XMPP_STATE_OPEN:
			// 从登录界面退出，广播自己presence状态, 切换到视频模式预备界面
            Log.d(TAG, "OnXmppStateChange");
            mPresence.InitPres(Observer);
			mPresence.SetPresStatus(true, EPresStatus.E_PRES_SHOW_ONLINE.ordinal());
			mPeerConnection.InitPeerConnection(Observer);
			mPeerConnection.AddIceServer("stun:120.236.21.179:19302", "", "");
			mPeerConnection.AddIceServer("turn:user@120.236.21.179:3478", "user", "password");
			break;
			
		case E_XMPP_STATE_CLOSED:
			break;

		default:
			break;
		}
	}

	
	/*
	private class PresStatusObserver implements Presence.IPresenceStatusObserver
	{
		@Override
		public void PresUpdate() 
		{
		}	
	}
	*/
	
	@Override
	public void PresUpdate() 
	{
		// TODO Auto-generated method stub
	}
	
	private void PrepareVideoRenderer()
	{
		Point displaySize = new Point();
	    getWindowManager().getDefaultDisplay().getSize(displaySize);
		mVideoRenderer = new VideoRendererView(this, displaySize);
		setContentView(mVideoRenderer);
	}

	@Override
	public void RecvPeerConnectionMsg(String strFrom, String strTo, String strMsg) 
	{
		// TODO Auto-generated method stub
		Log.i(TAG, strFrom);
		Log.i(TAG, strTo);
		Log.i(TAG, strMsg);
		if (!mBIsStarted)
		{
			//PrepareVideoRenderer();
			mPeerConnection.StartPeerConnection(strFrom, this, 0);
			mBIsStarted = true;
		}
	}

	@Override
	public void RecvPeerConnectionSdp(String strFrom, String strTo, String strSdp, String strType) 
	{
		// TODO Auto-generated method stub
		Log.i(TAG, strFrom);
		Log.i(TAG, strTo);
		Log.i(TAG, strSdp);
		Log.i(TAG, strType);
		if (!mBIsStarted)
		{
			//PrepareVideoRenderer();
			mPeerConnection.StartPeerConnection(strFrom, this, 0);
			mBIsStarted = true;
		}
	}
	
	@Override
	public void RecvPeerConnectionCandidate(String strFrom, String strTo,
			String strCandidate, int nSdpMLineIdx, String strSdpMid) 
	{
		// TODO Auto-generated method stub
		Log.i(TAG, strFrom);
		Log.i(TAG, strTo);
		Log.i(TAG, strCandidate);
		Log.i(TAG, String.valueOf(nSdpMLineIdx));
		Log.i(TAG, strSdpMid);
		if (!mBIsStarted)
		{
			//PrepareVideoRenderer();
			mPeerConnection.StartPeerConnection(strFrom, this, 0);
			mBIsStarted = true;
		}
	}

	@Override
	public void RenderFrame(I420Frame frame) 
	{
		// TODO Auto-generated method stub
		Log.i(TAG, "RenderFrame");	
	}

	@Override
	public void SetSize(int width, int height) 
	{
		// TODO Auto-generated method stub
		Log.i(TAG, "SetSize");
	}
}
