package com.salmannazir.filemanager.network;


/**
 * Created by Salman Nazir on 06/06/2016.
 */
public interface CacheOptions {

	public String getKey();
	public boolean shouldCache();
	public int getCacheTimeOut();
	public CacheType getType();
	
	
	public enum CacheType {
		PREFS, TEMP
	}
	
}
