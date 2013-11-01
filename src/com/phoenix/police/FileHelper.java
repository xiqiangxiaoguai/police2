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

import android.content.Context;
import android.util.Log;

import com.phoenix.data.Constants;

public class FileHelper {
	private static final boolean LOG_SWITCH = Constants.LOG_SWITCH;
	private static final String LOG_TAG = FileHelper.class.getSimpleName(); 
	
	public FileHelper() {
	}
	
	private static final SimpleDateFormat[] mDateFormats = new SimpleDateFormat[]{
		new SimpleDateFormat("yyMMdd"),
		new SimpleDateFormat("yyMM"),
		new SimpleDateFormat("dd"),
		new SimpleDateFormat("yyyyMMddhhmmss")
	};
	private String[] mDirPaths = new String[]{Constants.CAMERA_PATH,
			Constants.VIDEO_PATH};
	
	class Info{
		public ArrayList<String> info_imageUrls;
		public ArrayList<Long> info_createdTime;
		public Info() {
			info_imageUrls = new ArrayList<String>();
			info_createdTime = new ArrayList<Long>();
		}
	}
	private Info info = new Info();
	public ArrayList<String> getUrls(){
		return info.info_imageUrls;
	}
	public int query(int path){
		File[] files = new File(mDirPaths[path]).listFiles();
		if(files.length == 0){
			return files.length;
		}
		Arrays.sort(files, new Comparator<File>(){
		    public int compare(File f1, File f2)
		    {
		        return -((Long)getTimeFromFileName(f1.getName())).compareTo(getTimeFromFileName(f2.getName()));
		    } });
		for(int i=0; i <files.length; i++){
			if(files[i].isFile()){
				info.info_imageUrls.add(files[i].getAbsolutePath());
				info.info_createdTime.add(getTimeFromFileName(files[i].getName()));
			}
		}
		return files.length;
	}
	private Long getTimeFromFileName(String str){
		long l = 0;
		try {
			if(str.split("\\.").length < 2){
				return l;
			}else{
				l =  mDateFormats[3].parse(str.split("\\.")[0].split("\\_")[2]).getTime();
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return l;
	}
}
