package com.phoenix.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class ColorRelativeLayout extends RelativeLayout{

	public ColorRelativeLayout(Context context) {
		super(context);
	}
	public ColorRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public ColorRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:
			setBackgroundColor(Color.argb(100, 0, 255, 255));
			break;
		case MotionEvent.ACTION_UP:
			setBackgroundColor(getResources().getColor(android.R.color.transparent));
			break;
		}
		return super.onTouchEvent(event);
	}
}
