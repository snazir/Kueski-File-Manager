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
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.salmannazir.filemanager.R;
import com.salmannazir.filemanager.prefs.Constants;


public class HelpManager extends Activity implements OnClickListener {

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.help_layout);
		
		String text = "File Manager: If you have any questions or "
						+"comments, please email me or visit "
						;
		
		TextView helpTopLebel = (TextView)findViewById(R.id.help_top_label);
		helpTopLebel.setText(text);
		
		Button emailButton = (Button)findViewById(R.id.help_email_bt);
		Button web = (Button)findViewById(R.id.help_website_bt);
		emailButton.setOnClickListener(this);
		web.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		Intent i = new Intent();
		
		switch(id) {
			case R.id.help_email_bt:
				i.setAction(Intent.ACTION_SEND);
				i.setType("message/rfc822");
				i.putExtra(Intent.EXTRA_EMAIL, Constants.EMAIL);
				try {
					startActivity(Intent.createChooser(i, "Email using..."));

				} catch(ActivityNotFoundException e) {
					Toast.makeText(this, "Sorry, could not start the email", Toast.LENGTH_SHORT).show();
				}
			break;

			case R.id.help_website_bt:
				i.setAction(Intent.ACTION_VIEW);
				i.setData(Uri.parse(Constants.WEB));
				try {
					startActivity(i);
					
				} catch(ActivityNotFoundException e) {
					Toast.makeText(this, "Sorry, could not open the website", Toast.LENGTH_SHORT).show();
				}
				break;
		}
	}

}
