package com.salmannazir.filemanager.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.salmannazir.filemanager.parsers.BaseParser;

import java.io.File;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.message.BasicHeader;

public class HttpAsyncRequest {

	public static AsyncHttpClient client = new AsyncHttpClient();

	AsyncTaskListener mTaskListener;
	BaseParser mParser;
	CacheOptions mCacheOptions;
	String mUrl;
	Context mContext;
	RequestType type;
	RequestParams params;
	ArrayList<KeyValue> headers;

	public HttpAsyncRequest(Context mContext, String url, RequestType type,
							BaseParser parser, AsyncTaskListener listener) {
		this.mContext = mContext;
		this.mUrl = url;
		this.type = type;
		this.mParser = parser;
		this.mTaskListener = listener;
		this.params = new RequestParams();
		this.headers = new ArrayList<KeyValue>();
	}

	public HttpAsyncRequest(Context mContext, String url, RequestType type,
			BaseParser parser, CacheOptions cacheOptions, AsyncTaskListener listener) {
		this(mContext, url, type, parser, listener);
		this.mCacheOptions = cacheOptions;
		this.headers = new ArrayList<KeyValue>();
	}
	
	public HttpAsyncRequest(Context mContext, String url, RequestType type, AsyncTaskListener listener) {
		this.mContext = mContext;
		this.mUrl = url;
		this.type = type;
		this.mTaskListener = listener;
	}

	public void execute() {
		String data = null;
		if(mCacheOptions != null && mCacheOptions.shouldCache()) {
			data = lookIntoCache();
		}
		if (!isNetworkConnected(mContext)) {
			if(data == null) {
				noInternetConnection();
				return;
			}
		}
		if(data != null) {
			L.d("HTTP_TAG-> URL: " + mUrl);
			L.d("HTTP_TAG-> **FOUND IN CACHE**");
			L.d("HTTP_TAG-> " + data);
			TaskResult result = mParser.parse(200, data);
			mTaskListener.onComplete(result);
		} 
		else {
			if (type == RequestType.GET) {
				client.get(mUrl, params, responseHandler);

			} else if (type == RequestType.POST) {
				Header[] h = new Header[headers.size()];
				for(int i = 0; i < headers.size(); i++) {
					h[i] = new BasicHeader(headers.get(i).key, headers.get(i).value);
				}
				client.post(mContext, mUrl, h, params, null, responseHandler);
//				client.post(mContext, mUrl, params, responseHandler);
			}
		}
	}

	private String lookIntoCache() {
		if(mCacheOptions.getType() == CacheOptions.CacheType.PREFS) {
			return CacheManager.getInstance().obtain(mCacheOptions.getKey());
		} else if(mCacheOptions.getType() == CacheOptions.CacheType.TEMP) {
			return TempCacheManager.getInstance().get(mCacheOptions);
		}
		return null;
	}

	

	private AsyncHttpResponseHandler responseHandler = new AsyncHttpResponseHandler() {
		public void onSuccess(int code, Header[] headers, byte[] data) {
			if(mTaskListener == null) {
				return;
			}
			String serverResponse = new String(data);
			L.d("HTTP_TAG-> URL: " + mUrl);
			L.d("HTTP_TAG-> " + serverResponse);
			TaskResult result = mParser.parse(code, serverResponse);
			mTaskListener.onComplete(result);
			if(mCacheOptions != null && mCacheOptions.shouldCache() && result.isSuccess()) {
				if(mCacheOptions.getType() == CacheOptions.CacheType.PREFS) {
					CacheManager.getInstance().addToCache(mCacheOptions.getKey(), serverResponse);
				} else if(mCacheOptions.getType() == CacheOptions.CacheType.TEMP) {
					TempCacheManager.getInstance().addToCache(mCacheOptions.getKey(), serverResponse);
				}
			}
		}

		public void onFailure(int arg0, Header[] arg1, byte[] data, Throwable arg3) {
			if(mTaskListener != null) {		
				if(data == null) {
					TaskResult result = new TaskResult();
					mTaskListener.onComplete(result);
					return;
				}
				TaskResult result = mParser.parse(arg0, new String(data));
//				result.message = new String(data);
				L.fb("* "+mUrl + "*\n" + result.message);
				mTaskListener.onComplete(result);
			}
		}
		public void onProgress(int bytesWritten, int totalSize) {

		}
	};

    public void addHeader(String key, String value) {
        headers.add(new KeyValue(key, value));
    }

	public void addParam(String key, String value) {
		params.add(key, value);
	}
	
	public void addFile(String key, String filePath) {
        try {
            params.put(key, new File(filePath));
        } catch (Exception e) {
        }
    }

	protected void onPostExecute(TaskResult result) {
		mTaskListener.onComplete(result);
	}

	public enum RequestType {
		GET, POST
	}

	public void cancel(boolean b) {
		client.cancelAllRequests(b);
	}

	private void noInternetConnection() {
		TaskResult result = new TaskResult();
		result.code = TaskResult.CODE_NO_INTERNET_CONNECTION;
		result.message = TaskResult.MSG_NO_INTERNET_CONNECTION;
		mTaskListener.onComplete(result);
		return;
	}
	
	public  boolean isNetworkConnected(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni == null) {
			// There are no active networks.
			return false;
		} else
			return true;
	}

    public class KeyValue {
        public String key, value;
        public KeyValue(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }


}