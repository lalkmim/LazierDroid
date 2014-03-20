package br.com.pnpa.lazierdroid.util;


public class Log {
	private static final String TAG = "lazierDroid";

	public static void d(String msg) {
		android.util.Log.d(TAG, msg);
	}

	public static void e(String msg, Exception e) {
		android.util.Log.e(TAG, msg, e);
	}
	
	public static void e(Exception e) {
		android.util.Log.e(TAG, "", e);
	}

	public static void i(String msg) {
		android.util.Log.i(TAG, msg);		
	}
	
	public static void w(String msg) {
		android.util.Log.w(TAG, msg);		
	}
}
