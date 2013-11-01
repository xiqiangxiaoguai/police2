package com.phoenix.police;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.phoenix.data.Constants;

public class CameraBrowseActivity extends Activity{

	private static final String LOG_TAG = CameraBrowseActivity.class.getSimpleName();
	private static final boolean LOG_SWITCH = Constants.LOG_SWITCH;
	ArrayList<String> imageUrls = new ArrayList<String>();
	DisplayImageOptions options = new DisplayImageOptions.Builder()
    .showImageForEmptyUri(R.drawable.image_loading)
//    .resetViewBeforeLoading()
    .cacheOnDisc()
    .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
    .bitmapConfig(Bitmap.Config.RGB_565)
    .displayer(new FadeInBitmapDisplayer(300))
    .build();
	
	
	ImageLoader imageloader;
	int curPic;
	
	Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch(msg.what){
				case 0:
					ViewPager imagePager = (ViewPager) findViewById(R.id.imagePager);
					imagePager.setAdapter(new ImageAdapter(CameraBrowseActivity.this, imageUrls));
					imagePager.setCurrentItem(curPic);
					break;
			}
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		imageUrls = getIntent().getExtras().getStringArrayList("cameraPaths");
		curPic = getIntent().getExtras().getInt("currentPic");
		setContentView(R.layout.camera_browse);
		mHandler.sendEmptyMessageDelayed(0,500);
		Button button = (Button) findViewById(R.id.back);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	class ImageAdapter extends PagerAdapter{
		Context mContext;
		ArrayList<String> mImageUrls;
		LayoutInflater inflater;
		public ImageAdapter(Context context, ArrayList<String> imageUrls) {
			mContext = context;
			mImageUrls = imageUrls;
			inflater = getLayoutInflater();
			imageloader = ImageLoader.getInstance();
			imageloader.destroy();
			imageloader.init(ImageLoaderConfiguration.createDefault(mContext));
		}
		@Override
		public int getCount() {
			return mImageUrls.size();
		}
		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0.equals(arg1);
		}
		@Override
		public void destroyItem(View container, int position, Object object) {
			((ViewPager) container).removeView((View)object);
		}
		@Override
		public Object instantiateItem(View view, int position) {
			if (LOG_SWITCH)
				Log.d(LOG_TAG, "instantiate item:" + position);
			RelativeLayout relative = (RelativeLayout) inflater.inflate(R.layout.browse_item, null);
			ImageView imageView = (ImageView) relative.findViewById(R.id.image);
 			imageloader.displayImage("file:/" + mImageUrls.get(position), imageView);
 			((ViewPager) view).addView(relative, 0);
			return relative;
		}
		
	}
}

