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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;

import com.salmannazir.filemanager.R;


public class SettingsActivity extends Activity {
	private boolean mHiddenStateChanged = false;
	private boolean mColorChanged = false;
	private boolean mThumbnailChanged = false;
	private boolean mSortingChanged = false;
	private boolean mSpaceChanged = false;
	
	private boolean hiddenState;
	private boolean thumbnailState;
	private int mColorState, mSortState, mSpaceState;
	private Intent mIntent = new Intent();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		
		Intent i = getIntent();
		hiddenState = i.getExtras().getBoolean("HIDDEN");
		thumbnailState = i.getExtras().getBoolean("THUMBNAIL");
		mColorState = i.getExtras().getInt("COLOR");
		mSortState = i.getExtras().getInt("SORT");
		mSpaceState = i.getExtras().getInt("SPACE");
				
		final CheckBox hiddenCheckbox = (CheckBox)findViewById(R.id.setting_hidden_box);
		final CheckBox thumbnailCheckbox = (CheckBox)findViewById(R.id.setting_thumbnail_box);
		final CheckBox spaceCheckbox = (CheckBox)findViewById(R.id.setting_storage_box);
		final ImageButton colorImageButton = (ImageButton)findViewById(R.id.setting_text_color_button);
		final ImageButton sortImageButton = (ImageButton)findViewById(R.id.settings_sort_button);
		
		hiddenCheckbox.setChecked(hiddenState);
		thumbnailCheckbox.setChecked(thumbnailState);
		spaceCheckbox.setChecked(mSpaceState == View.VISIBLE);
		
		colorImageButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
				CharSequence[] options = {"White", "Magenta", "Yellow", "Red", "Cyan",
									      "Blue", "Green"};
				int index = ((mColorState & 0x00ffffff) << 2) % options.length;
				
				builder.setTitle("Change text color");
				builder.setIcon(R.drawable.color);
				builder.setSingleChoiceItems(options, index, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int index) {
						switch(index) {
							case 0:
								mColorState = Color.WHITE;
								mIntent.putExtra("COLOR", mColorState);
								mColorChanged = true;
								
								break;
							case 1:
								mColorState = Color.MAGENTA;
								mIntent.putExtra("COLOR", mColorState);
								mColorChanged = true;
								
								break;
							case 2:
								mColorState = Color.YELLOW;
								mIntent.putExtra("COLOR", mColorState);
								mColorChanged = true;
								
								break;
							case 3:
								mColorState = Color.RED;
								mIntent.putExtra("COLOR", mColorState);
								mColorChanged = true;
								
								break;
							case 4:
								mColorState = Color.CYAN;
								mIntent.putExtra("COLOR", mColorState);
								mColorChanged = true;
								
								break;
							case 5:
								mColorState = Color.BLUE;
								mIntent.putExtra("COLOR", mColorState);
								mColorChanged = true;
								
								break;
							case 6:
								mColorState = Color.GREEN;
								mIntent.putExtra("COLOR", mColorState);
								mColorChanged = true;
								
								break;
						}
					}
				});
				
				builder.create().show();
			}
		});
		
		hiddenCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				hiddenState = isChecked;
				
				mIntent.putExtra("HIDDEN", hiddenState);
				mHiddenStateChanged = true;
			}
		});
		
		thumbnailCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				thumbnailState = isChecked;
				
				mIntent.putExtra("THUMBNAIL", thumbnailState);
				mThumbnailChanged = true;
			}
		});
		
		spaceCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked) 
					mSpaceState = View.VISIBLE;
				else 
					mSpaceState = View.GONE;
				
				mSpaceChanged = true;
				mIntent.putExtra("SPACE", mSpaceState);
			}
		});
		
		sortImageButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
    			CharSequence[] options = {"None", "Alphabetical", "Type", "Size"};
    			
    			builder.setTitle("Sort by...");
    			builder.setIcon(R.drawable.filter);
    			builder.setSingleChoiceItems(options, mSortState, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int index) {
						switch(index) {
						case 0:
							mSortState = 0;
							mSortingChanged = true;
							mIntent.putExtra("SORT", mSortState);
							break;
							
						case 1:
							mSortState = 1;
							mSortingChanged = true;
							mIntent.putExtra("SORT", mSortState);
							break;
							
						case 2:
							mSortState = 2;
							mSortingChanged = true;
							mIntent.putExtra("SORT", mSortState);
							break;
						
						case 3:
							mSortState = 3;
							mSortingChanged = true;
							mIntent.putExtra("SORT", mSortState);
							break;
						}
					}
				});
    			
    			builder.create().show();
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if(!mSpaceChanged)
			mIntent.putExtra("SPACE", mSpaceState);
		
		if(!mHiddenStateChanged)
			mIntent.putExtra("HIDDEN", hiddenState);
		
		if(!mColorChanged)
			mIntent.putExtra("COLOR", mColorState);
		
		if(!mThumbnailChanged)
			mIntent.putExtra("THUMBNAIL", thumbnailState);
		
		if(!mSortingChanged)
			mIntent.putExtra("SORT", mSortState);
			
		setResult(RESULT_CANCELED, mIntent);
	}
}
