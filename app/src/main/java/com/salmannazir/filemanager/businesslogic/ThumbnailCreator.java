/*
    Open Manager, an open source file manager for the Android system
    Copyright (C) 2009, 2010, 2011  Joe Berria <nexesdevelopment@gmail.com>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.salmannazir.filemanager.businesslogic;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Salman Nazir on 06/06/2016.
 */

public class ThumbnailCreator extends Thread {	
	private int mThumbnailWidth;
	private int mThumbnailHeight;
	private SoftReference<Bitmap> mThumbnailRef;
	private static HashMap<String, Bitmap> mCacheHashMap = null;
	private ArrayList<String> mFileList;
	private String mDirectory;
	private Handler mHandler;
	private boolean mStop = false;

	public ThumbnailCreator(int width, int height) {
		mThumbnailHeight = height;
		mThumbnailWidth = width;
		
		if(mCacheHashMap == null)
			mCacheHashMap = new HashMap<String, Bitmap>();
	}
	
	public Bitmap isBitmapCached(String name) {
		return mCacheHashMap.get(name);
	}

	public void setCancelThumbnails(boolean stop) {
		mStop = stop;
	}
	
	public void createNewThumbnail(ArrayList<String> files,  String dir,  Handler handler) {
		this.mFileList = files;
		this.mDirectory = dir;
		this.mHandler = handler;		
	}
	
	@Override
	public void run() {
		int len = mFileList.size();
		
		for (int i = 0; i < len; i++) {	
			if (mStop) {
				mStop = false;
				mFileList = null;
				return;
			}
			final File mFile = new File(mDirectory + "/" + mFileList.get(i));
			
			if (isImageFile(mFile.getName())) {
				long len_kb = mFile.length() / 1024;
				
				BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
				bitmapOptions.outWidth = mThumbnailWidth;
				bitmapOptions.outHeight = mThumbnailHeight;
					
				if (len_kb > 1000 && len_kb < 5000) {
					bitmapOptions.inSampleSize = 32;
					bitmapOptions.inPurgeable = true;
					mThumbnailRef = new SoftReference<Bitmap>(BitmapFactory.decodeFile(mFile.getPath(), bitmapOptions));
										
				} else if (len_kb >= 5000) {
					bitmapOptions.inSampleSize = 32;
					bitmapOptions.inPurgeable = true;
					mThumbnailRef = new SoftReference<Bitmap>(BitmapFactory.decodeFile(mFile.getPath(), bitmapOptions));
									
				} else if (len_kb <= 1000) {
					bitmapOptions.inPurgeable = true;
					mThumbnailRef = new SoftReference<Bitmap>(Bitmap.createScaledBitmap(
							 						   BitmapFactory.decodeFile(mFile.getPath()),
							mThumbnailWidth,
							mThumbnailHeight,
							 						   false));
				}
								
				mCacheHashMap.put(mFile.getPath(), mThumbnailRef.get());
				
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						Message msg = mHandler.obtainMessage();
						msg.obj = (Bitmap) mThumbnailRef.get();
						msg.sendToTarget();
					}
				});
			}
		}
	}
	
	private boolean isImageFile(String file) {
		String ext = file.substring(file.lastIndexOf(".") + 1);
		
		if (ext.equalsIgnoreCase("png") || ext.equalsIgnoreCase("jpg") ||
			ext.equalsIgnoreCase("jpeg")|| ext.equalsIgnoreCase("gif") ||
			ext.equalsIgnoreCase("tiff")|| ext.equalsIgnoreCase("tif"))
			return true;
		
		return false;
	}
}