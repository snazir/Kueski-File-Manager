package com.salmannazir.filemanager.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.salmannazir.filemanager.R;
import com.salmannazir.filemanager.Utils.FontManager;


/**
 * Created by Salman Nazir on 06/04/16.
 */
public class SplashActivity extends Activity {
    TextView mKueskiText,mChallengeText;

    private static int SPLASH_TIME_OUT = 1000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mKueskiText = (TextView)findViewById(R.id.kueski_text) ;
        mChallengeText = (TextView)findViewById(R.id.challenge_text) ;
        mKueskiText.setTypeface(FontManager.getBoldFontTypeFace(this));
        mChallengeText.setTypeface(FontManager.getBoldFontTypeFace(this));

        new Handler().postDelayed(new Runnable() {

			/*
             * Showing splash screen with a timer. This will be useful when you
			 * want to show case your app logo / company
			 */

            @Override
            public void run() {
                // This method will be executed once the timer is over
                Intent intent  =new Intent(SplashActivity.this,MainActivity.class);
                startActivity(intent);
                finish();

                }
            
        }, SPLASH_TIME_OUT);

    }
}
