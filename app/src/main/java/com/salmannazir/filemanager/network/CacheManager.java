package com.salmannazir.filemanager.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Salman Nazir on 06/06/2016.
 */

public class CacheManager {

	public static final String CACHE_KEY = "kueski_cache_key";
	
	private static CacheManager instance = null;
	
	HashMap<String, String> map;
	Context context;
	SharedPreferences prefs;
	Editor editor;
	
	private CacheManager(Context context) {
		this.context = context.getApplicationContext();
		map = new HashMap<String, String>();
		prefs = context.getSharedPreferences(CACHE_KEY, Context.MODE_PRIVATE);
		editor = prefs.edit();
		
		init();
	}
	
	private void init() {
		HashMap<String, ?> temp = (HashMap<String, ?>) prefs.getAll();
		Iterator<String> iterator = temp.keySet().iterator();
		while(iterator.hasNext()) {
			String key = iterator.next();
			if(temp.get(key) instanceof String) {
				map.put(key, (String) temp.get(key));
			}
		}
	}

	public static CacheManager initialize(Context context) {
		if(instance == null) {
			instance = new CacheManager(context);
		}
		return instance;
	}
	
	public static CacheManager getInstance() {
		return instance;
	}
	
	public void addToCache(String key, String value) {
		map.put(key, value);
		editor.putString(key, value);
		editor.commit();
	}
	
	public String obtain(String key) {
		return map.get(key);
	}
	
	public void remove(String key) {
		map.remove(key);
		editor.remove(key).commit();
	}
	
}
