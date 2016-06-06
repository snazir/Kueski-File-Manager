package com.salmannazir.filemanager.network;

import java.util.HashMap;


/**
 * Created by Salman Nazir on 06/06/2016.
 */
public class TempCacheManager {

	private static TempCacheManager instance = new TempCacheManager();

	HashMap<String, LFLCache> map;
	
	private TempCacheManager() {
		map = new HashMap<String, LFLCache>();
	}

	public static TempCacheManager getInstance() {
		if(instance == null) {
			instance = new TempCacheManager();
		}
		return instance;
	}
	
	public String get(CacheOptions config) {
		LFLCache cache = map.get(config.getKey());
		if(cache == null) {
			return null;
		}
		long dt = System.currentTimeMillis() - cache.cacheTime;
		if(dt >= config.getCacheTimeOut()) {
			map.remove(config.getKey());
			return null;
		}
		return cache.data;
	}
	
	public void addToCache(String key, String data) {
		LFLCache cache = new LFLCache(data, System.currentTimeMillis());
		map.put(key, cache);
	}

	public static class LFLCache {
		public String data;
		public long cacheTime;
		public LFLCache(String data, long time) {
			this.data = data;
			this.cacheTime = time;
		}
	}

}
