package com.phoenix.online;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;

import a9.terminal.PeerConnection;
import a9.terminal.PeerConnection.I420Frame;

public class VideoRendererView extends GLSurfaceView
implements GLSurfaceView.Renderer
//, PeerConnection.IVideoRenderer
{
	public VideoRendererView(Context c, Point screenDimensions)
	{
		super(c);
		setEGLContextClientVersion(2);
	    setRenderer(this);
	    setRenderMode(RENDERMODE_WHEN_DIRTY);
	}

	@Override
	public void onDrawFrame(GL10 arg0) 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSurfaceChanged(GL10 arg0, int arg1, int arg2)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) 
	{
		// TODO Auto-generated method stub
		
	}
/*
	@Override
	public void RenderFrame(I420Frame frame) 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void SetSize(int width, int height) 
	{
		// TODO Auto-generated method stub
		
	}
	*/
	
}
