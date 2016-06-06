package com.salmannazir.filemanager.network;

import android.util.Log;


public class L {

	public static final String TAG = "KUESKI";
	
	public static final int DEBUG_MODE = 0;
	public static final int PRODUCTION_MODE = 1;
	public static final int DEVELOPMENT_MODE = 2;
	
	public static int MODE = DEVELOPMENT_MODE;
	public static boolean FB = false;
	
	public static void d(String msg) {
		if(MODE == DEBUG_MODE || MODE == DEVELOPMENT_MODE) {
			Log.d(TAG, msg);
		}
	}

	public static void fb(String msg) {
		if(!FB) {
			return;
		}
		try {
			throw new Exception(msg);
		} catch (Exception e) {
//			Crashlytics.logException(e);
		}
	}
}
