package com.salmannazir.filemanager.network;

public class TaskResult {

	public static final int CODE_FAILURE = 1;
	public static final int CODE_SUCCESS = 2;
	public static final int CODE_INVALID_KEY = 3;
	public static final int CODE_NO_INTERNET_CONNECTION = 4;

	public static final String MSG_NO_INTERNET_CONNECTION = "No internet connection";

	public int code = CODE_FAILURE;
	public String message;
	public Object data;

	public boolean isSuccess() {
		if(code == CODE_SUCCESS) {
			return true;
		}
		return false;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public void success(boolean b) {
		if(b) {
			code = CODE_SUCCESS;
		}
		else {
			code = CODE_FAILURE;
		}
	}

	public Object getData() {
		return data;
	}
}
