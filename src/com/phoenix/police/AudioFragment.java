package com.phoenix.police;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Fragment;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.SeekBar;
import android.widget.TextView;

import com.phoenix.data.Constants;
import com.phoenix.police.PinnedHeaderListView.PinnedHeaderAdapter;

public class AudioFragment extends Fragment{

	private static final boolean LOG_SWITCH = Constants.LOG_SWITCH;
	private static final String LOG_TAG = AudioFragment.class.getSimpleName();
	class Info{
		public ArrayList<String> info_audioUrls;
		public ArrayList<Long> info_createdTime;
		public Info() {
			info_audioUrls = new ArrayList<String>();
			info_createdTime = new ArrayList<Long>();
		}
	}
	
	Info info = new Info();
	private String[] mTimeRange = null;
	private static final SimpleDateFormat[] mDateFormats = new SimpleDateFormat[]{
		new SimpleDateFormat("yyMMdd"),
		new SimpleDateFormat("yyMM"),
		new SimpleDateFormat("dd"),
		new SimpleDateFormat("yyyyMMddHHmmss")
	};
	
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what){
				case 0:
					((ImageView)grid.getChildAt(mCurrentPlayPos - grid.getFirstVisiblePosition() + grid.getHeaderViewsCount()).findViewById(R.id.audio_control)).setImageResource(R.drawable.end);
					mCurrentPlayPos = -1;
					break;
			}
		}
	};
	
	private List<String> mSections;
	private List<Integer> mPositions;
	private Map<String, Integer> mIndexer;
	private Map<String, List<Long>> mMap;
	
	MediaPlayer mediaPlayer;
	SeekBar mSeekBar;
	TextView mPastedTime;
	TextView mRemaingTime;
		
	PinnedHeaderListView grid;
	int mCurrentPlayPos = -1;
    public static final int PLAYING=1;//������
    public static final int PAUSE=2;//��ͣ
    public static final int STOP=3;//ֹͣ
    public int STATE=STOP;
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		CameraActivity.checkAndMkdirs();
		HandlerThread hThread = new HandlerThread(AudioFragment.class.getSimpleName());
		hThread.start();
	}
	private Long getTimeFromFileName(String str){
		long l = 0;
		try {
			l =  mDateFormats[3].parse(str.split("\\.")[0].split("\\_")[3]).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return l;
	}
	private int getImages(){
		File[] files = new File(Constants.getAudioPath()).listFiles();
		Arrays.sort(files, new Comparator<File>(){
		    public int compare(File f1, File f2)
		    {
		    	return -((Long)f1.lastModified()).compareTo(f2.lastModified());
		    } });
		if(files.length == 0){
			return files.length;
		}
		for(int i=0; i <files.length; i++){
			info.info_audioUrls.add(files[i].getAbsolutePath());
			info.info_createdTime.add(getTimeFromFileName(files[i].getName()));
		}
		
		mSections = new ArrayList<String>();
		for(String str : mTimeRange){
			mSections.add(str);
		}
		mMap = new HashMap<String, List<Long>>();
		for(String str : mTimeRange){
			List<Long> list = new ArrayList<Long>();
			mMap.put(str, list);
		}
		mPositions = new ArrayList<Integer>();
		mIndexer = new HashMap<String, Integer>();
		Long currentDate = System.currentTimeMillis();
		for(int i =0; i <info.info_createdTime.size(); i++){
			if(mDateFormats[0].format(currentDate).equals(mDateFormats[0].format(info.info_createdTime.get(i)))){
				if (LOG_SWITCH) {
					Log.d(LOG_TAG, "0:" + mDateFormats[0].format(currentDate) + " 0:" + mDateFormats[0].format(info.info_createdTime.get(i)));
				}
				mMap.get(mTimeRange[0]).add(info.info_createdTime.get(i));
				continue;
			}
			
			if(Integer.parseInt(mDateFormats[0].format(currentDate)) - Integer.parseInt((mDateFormats[0].format(info.info_createdTime.get(i)))) < 7){
				if (LOG_SWITCH) {
					Log.d(LOG_TAG, "1:" + mDateFormats[0].format(currentDate) + " 1:" + mDateFormats[0].format(info.info_createdTime.get(i)));
				}
				mMap.get(mTimeRange[1]).add(info.info_createdTime.get(i));
				continue;
			}
			
			if((Integer.parseInt(mDateFormats[1].format(currentDate)) - Integer.parseInt((mDateFormats[1].format(info.info_createdTime.get(i)))) == 0 &&
				Integer.parseInt(mDateFormats[2].format(currentDate)) - Integer.parseInt((mDateFormats[2].format(info.info_createdTime.get(i)))) >7) ||
				(Integer.parseInt(mDateFormats[1].format(currentDate)) - Integer.parseInt((mDateFormats[1].format(info.info_createdTime.get(i)))) == 1 &&
				Integer.parseInt(mDateFormats[2].format(currentDate)) - Integer.parseInt((mDateFormats[2].format(info.info_createdTime.get(i)))) <= 0)){
				if (LOG_SWITCH) {
					Log.d(LOG_TAG, "2:" + mDateFormats[0].format(currentDate) + " 2:" + mDateFormats[0].format(info.info_createdTime.get(i)));
				}
				mMap.get(mTimeRange[2]).add(info.info_createdTime.get(i));
				continue;
			}
			
			if((Integer.parseInt(mDateFormats[1].format(currentDate)) - Integer.parseInt((mDateFormats[1].format(info.info_createdTime.get(i)))) < 3) ||
				(Integer.parseInt(mDateFormats[1].format(currentDate)) - Integer.parseInt((mDateFormats[1].format(info.info_createdTime.get(i)))) ==3 &&
				Integer.parseInt(mDateFormats[2].format(currentDate)) - Integer.parseInt((mDateFormats[2].format(info.info_createdTime.get(i)))) <= 0)){
				if (LOG_SWITCH) {
					Log.d(LOG_TAG, "3:" + mDateFormats[0].format(currentDate) + " 3:" + mDateFormats[0].format(info.info_createdTime.get(i)));
				}
				mMap.get(mTimeRange[3]).add(info.info_createdTime.get(i));
				continue;
			}

			if (LOG_SWITCH) {
				Log.d(LOG_TAG, "4:" + mDateFormats[0].format(currentDate) + " 4:" + mDateFormats[0].format(info.info_createdTime.get(i)));
			mMap.get(mTimeRange[4]).add(info.info_createdTime.get(i));
			continue;
			}
		}
		for(int i = (mSections.size() - 1); i >= 0 ;i --){
			if(mMap.get(mSections.get(i)).size() == 0){
				mMap.remove(mSections.get(i));
				mSections.remove(i);
			}
		}
        int position = 0;  
        for (int i = 0; i < mSections.size(); i++) {  
            mIndexer.put(mSections.get(i), position);  
            mPositions.add(position); 
            position += mMap.get(mSections.get(i)).size();  
        }  
        return files.length;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mTimeRange = new String[]{getResources().getString(R.string.time_range_1),
				getResources().getString(R.string.time_range_2),
				getResources().getString(R.string.time_range_3),
				getResources().getString(R.string.time_range_4),
				getResources().getString(R.string.time_range_5)};
		int count = getImages();
		View view = inflater.inflate(R.layout.audio_fragment, container,false);
		if(count == 0 ){
			return view;
		}
		grid = (PinnedHeaderListView) view.findViewById(R.id.images);
		mSeekBar = (SeekBar) view.findViewById(R.id.seekbar);
		mPastedTime = (TextView) view.findViewById(R.id.pasted_time);
		mRemaingTime = (TextView) view.findViewById(R.id.remaining_time);
		ImageAdapter mAdapter = new ImageAdapter(getActivity(), info, mSections, mPositions);
		grid.setAdapter(mAdapter);
		grid.setOnScrollListener(mAdapter);
		grid.setPinnedHeaderView(LayoutInflater.from(getActivity()).inflate(  
                R.layout.listview_head, grid, false));
		grid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				mHandler.removeCallbacks(updateSeekbar);
				if(mCurrentPlayPos == arg2){
					if(STATE == PLAYING){
						if(mediaPlayer != null){
							mediaPlayer.pause();
							STATE = PAUSE;
							((ImageView)grid.getChildAt(mCurrentPlayPos- grid.getFirstVisiblePosition() + grid.getHeaderViewsCount()).findViewById(R.id.audio_control)).setImageResource(R.drawable.end);
						}
					}else if(STATE == PAUSE){
						if(mediaPlayer != null){
							mediaPlayer.start();
							STATE = PLAYING;
							mHandler.post(updateSeekbar);
							((ImageView)grid.getChildAt(mCurrentPlayPos- grid.getFirstVisiblePosition() + grid.getHeaderViewsCount()).findViewById(R.id.audio_control)).setImageResource(R.drawable.pause);
						}
					}
				}else{
					if(mediaPlayer != null){
						mediaPlayer.stop();
						mediaPlayer.release();
					}
					mediaPlayer = new MediaPlayer();
					mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
						@Override
						public void onCompletion(MediaPlayer mp) {
							STATE = STOP;
							mHandler.sendEmptyMessage(0);
						}
					});
					try {
						((ImageView)grid.getChildAt(mCurrentPlayPos- grid.getFirstVisiblePosition() + grid.getHeaderViewsCount()).findViewById(R.id.audio_control)).setImageResource(R.drawable.start);
						ImageView audioControl = (ImageView) arg1.findViewById(R.id.audio_control);
						audioControl.setImageDrawable(getResources().getDrawable(R.drawable.pause));
						mediaPlayer.setDataSource( info.info_audioUrls.get(arg2));
						mCurrentPlayPos = arg2;
						mediaPlayer.prepare();
						mediaPlayer.start();
						mSeekBar.setMax(mediaPlayer.getDuration()/1000);
						mHandler.post(updateSeekbar);
						STATE = PLAYING;
					} catch (Exception e) {
						e.printStackTrace();
					} 
				}
			}
		});
		return view;
	}
	
	Runnable updateSeekbar = new Runnable(){
		public void run() {
			int curPosition = mediaPlayer.getCurrentPosition()/1000;
			int remaining = mSeekBar.getMax() - curPosition;
			if(curPosition == mSeekBar.getMax()){
				mSeekBar.setProgress(0);
				mPastedTime.setText(String.format("%1$02d:%2$02d", 0/60,0%60));
				mRemaingTime.setText(String.format("%1$02d:%2$02d", mSeekBar.getMax()/60,mSeekBar.getMax()%60));
				return;
			}
			mSeekBar.setProgress(curPosition);
			mPastedTime.setText(String.format("%1$02d:%2$02d", curPosition/60,curPosition%60));
			mRemaingTime.setText(String.format("%1$02d:%2$02d", mSeekBar.getMax()/60,mSeekBar.getMax()%60));
			mHandler.postDelayed(updateSeekbar,1000);
		};
	};
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		if(mediaPlayer!= null && mediaPlayer.isPlaying()){
			mediaPlayer.stop();
			mediaPlayer.release();
		}
		mHandler.removeCallbacks(updateSeekbar);
	}
	
	class ImageAdapter extends BaseAdapter implements SectionIndexer, PinnedHeaderAdapter, OnScrollListener{

		private SimpleDateFormat sDateFormat = new SimpleDateFormat("yy-MM-dd HH:mm");
		private Context mContext;
		private int mLocationPosition = -1;
		private Long[] mDatas;
		private List<String> urls;
		private List<String> mFriendsSections;
		private List<Integer> mFriendsPositions;
		private LayoutInflater inflater;
		public ImageAdapter(Context context, Info info, List<String> friendsSections, List<Integer> friendsPositions) {
			mContext = context;
			inflater = LayoutInflater.from(mContext);
			mDatas = info.info_createdTime.toArray(new Long[info.info_createdTime.size()]);
			urls = info.info_audioUrls;
			mFriendsSections = friendsSections;
			mFriendsPositions = friendsPositions;
		}
		@Override
		public int getCount() {
			return mDatas.length;
		}

		@Override
		public Object getItem(int position) {
			return mDatas[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			int section = getSectionForPosition(position);  
	        if (convertView == null) {  
	            convertView = inflater.inflate(R.layout.listview_item3, null);  
	        }  
	        LinearLayout mHeaderParent = (LinearLayout) convertView  
	                .findViewById(R.id.friends_item_header_parent);  
	        TextView mHeaderText = (TextView) convertView  
	                .findViewById(R.id.friends_item_header_text);
	        ImageView mAudioControl = (ImageView) convertView
	        		.findViewById(R.id.audio_control);
	        if (getPositionForSection(section) == position) {  
	            mHeaderParent.setVisibility(View.VISIBLE);  
	            mHeaderText.setText(mFriendsSections.get(section));  
	        } else {  
	            mHeaderParent.setVisibility(View.GONE);  
	        }  
	        TextView textView = (TextView) convertView  
	                .findViewById(R.id.friends_item);  
	        textView.setText("" + sDateFormat.format(mDatas[position]));  
	        if(position == mCurrentPlayPos){
	        	mAudioControl.setImageResource(R.drawable.pause);
	        }else{
	        	mAudioControl.setImageResource(R.drawable.end);
	        }
	        return convertView; 
	        
//			imageView.setImageResource(R.drawable.image_loading);
//			imageView.setAdjustViewBounds(true);
//			imageView.setMaxHeight(LayoutParams.MATCH_PARENT);
//			imageView.setMaxWidth(LayoutParams.MATCH_PARENT);
//			LayoutParams p = new LayoutParams(LayoutParams.MATCH_PARENT, 300);
//			imageView.setLayoutParams(p);
//				imageloader.displayImage("file:/" + imageUrls.get(arg0), imageView,options,new SimpleImageLoadingListener()  
//	            {  
//					@Override
//					public void onLoadingComplete(String imageUri, View view,
//							Bitmap loadedImage) {
//								Animation anim = AnimationUtils.loadAnimation(
//										mContext, android.R.anim.fade_in);
//								imageView.setAnimation(anim);
//								anim.start();  
//					}
//	            });
//			return imageView;
		}
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			if (view instanceof PinnedHeaderListView) {  
	            ((PinnedHeaderListView) view).configureHeaderView(firstVisibleItem);  
	        } 
		}
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			
		}
		@Override
		public int getPinnedHeaderState(int position) {
			int realPosition = position;  
	        if (realPosition < 0  
	                || (mLocationPosition != -1 && mLocationPosition == realPosition)) {  
	            return PINNED_HEADER_GONE;  
	        }  
	        mLocationPosition = -1;  
	        int section = getSectionForPosition(realPosition);  
	        int nextSectionPosition = getPositionForSection(section + 1);  
	        if (nextSectionPosition != -1  
	                && realPosition == nextSectionPosition - 1) {  
	            return PINNED_HEADER_PUSHED_UP;  
	        }  
	        return PINNED_HEADER_VISIBLE;  
		}
		@Override
		public void configurePinnedHeader(View header, int position, int alpha) {
			int realPosition = position;  
	        int section = getSectionForPosition(realPosition);  
	        String title = (String) getSections()[section];  
	        ((TextView) header.findViewById(R.id.friends_list_header_text))  
	                .setText(title); 
		}
		@Override
		public int getPositionForSection(int section) {
			if (section < 0 || section >= mFriendsSections.size()) {  
	            return -1;  
	        }  
	        return mFriendsPositions.get(section);
		}
		@Override
		public int getSectionForPosition(int position) {
			if (position < 0 || position >= getCount()) {  
	            return -1;  
	        }  
	        int index = Arrays.binarySearch(mFriendsPositions.toArray(), position);  
	        return index >= 0 ? index : -index - 2;
		}
		@Override
		public Object[] getSections() {
			// TODO Auto-generated method stub
			return mFriendsSections.toArray();
		}
	}


}
