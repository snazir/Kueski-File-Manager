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

package com.salmannazir.filemanager.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;


import com.salmannazir.filemanager.R;
import com.salmannazir.filemanager.businesslogic.FileManager;
import com.salmannazir.filemanager.prefs.Constants;

import java.io.File;
import java.util.Date;

public class DirectoryInformationActivity extends Activity {

	private String mDirectoryPathName;
	private TextView mNameTextView, mPathTextView, mDirectoryTextView,
			mFileTextView, mTimeTextView, mTotalTextView;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.info_layout);
		
		Intent i = getIntent();
		if(i != null) {
			if(i.getAction() != null && i.getAction().equals(Intent.ACTION_VIEW)) {
				mDirectoryPathName = i.getData().getPath();
				
				if(mDirectoryPathName == null)
					mDirectoryPathName = "";
			} else {
				mDirectoryPathName = i.getExtras().getString("PATH_NAME");
			}
		}
		
		mNameTextView = (TextView)findViewById(R.id.name_label);
		mPathTextView = (TextView)findViewById(R.id.path_label);
		mDirectoryTextView = (TextView)findViewById(R.id.dirs_label);
		mFileTextView = (TextView)findViewById(R.id.files_label);
		mTimeTextView = (TextView)findViewById(R.id.time_stamp);
		mTotalTextView = (TextView)findViewById(R.id.total_size);

		Button back = (Button)findViewById(R.id.back_button);
		back.setOnClickListener(new ButtonClickHandler());
		
		new BackgroundTask().execute(mDirectoryPathName);
		
	}
	


	private class BackgroundTask extends AsyncTask<String, Void, Long> {
		private ProgressDialog mProgressDialog;
		private String mDisplaySize;
		private int mTotalFileCount = 0;
		private int mDirectoryCount = 0;
		
		protected void onPreExecute(){
			mProgressDialog = ProgressDialog.show(DirectoryInformationActivity.this, "", "Calculating information...", true, true);
		}
		
		protected Long doInBackground(String... vals) {
			FileManager mFileManager = new FileManager();
			File file = new File(vals[0]);
			long size = 0;
			int len = 0;

			File[] list = file.listFiles();
			if(list != null)
				len = list.length;
			
			for (int i = 0; i < len; i++){
				if(list[i].isFile())
					mTotalFileCount++;
				else if(list[i].isDirectory())
					mDirectoryCount++;
			}
			
			if(vals[0].equals(Constants.HOME_PATH)) {
				StatFs fss = new StatFs(Environment.getRootDirectory().getPath());
				size = fss.getAvailableBlocks() * (fss.getBlockSize() / Constants.KB);
				
				mDisplaySize = (size > Constants.GB) ?
						String.format("%.2f Gb ", (double)size / Constants.MG) :
						String.format("%.2f Mb ", (double)size / Constants.KB);
				
			}else if(vals[0].equals("/sdcard")) {
				StatFs fs = new StatFs(Environment.getExternalStorageDirectory()
										.getPath());
				size = fs.getBlockCount() * (fs.getBlockSize() / Constants.KB);
				
				mDisplaySize = (size > Constants.GB) ?
					String.format("%.2f Gb ", (double)size / Constants.GB) :
					String.format("%.2f Gb ", (double)size / Constants.MG);
				
			} else {
				size = mFileManager.getDirectorySize(vals[0]);
						
				if (size > Constants.GB)
					mDisplaySize = String.format("%.2f Gb ", (double)size / Constants.GB);
				else if (size < Constants.GB && size > Constants.MG)
					mDisplaySize = String.format("%.2f Mb ", (double)size / Constants.MG);
				else if (size < Constants.MG && size > Constants.KB)
					mDisplaySize = String.format("%.2f Kb ", (double)size/ Constants.KB);
				else
					mDisplaySize = String.format("%.2f bytes ", (double)size);
			}
			
			return size;
		}
		
		protected void onPostExecute(Long result) {
			File file = new File(mDirectoryPathName);
			
			mNameTextView.setText(file.getName());
			mPathTextView.setText(file.getAbsolutePath());
			mDirectoryTextView.setText(mDirectoryCount + " folders ");
			mFileTextView.setText(mTotalFileCount + " files ");
			mTotalTextView.setText(mDisplaySize);
			mTimeTextView.setText(new Date(file.lastModified()) + " ");
			
			mProgressDialog.cancel();
		}	
	}
	
	private class ButtonClickHandler implements OnClickListener {
		
		@Override
		public void onClick(View v) {
			if(v.getId() == R.id.back_button)
				finish();
		}
	}
}
