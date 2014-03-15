package br.com.pnpa.lazierdroid.util;


public class Log {
	private static final String TAG = "lazierDroid";

	public static void d(String msg) {
		android.util.Log.d(TAG, msg);
	}

	public static void e(String msg, Exception e) {
		android.util.Log.e(TAG, msg, e);
	}

	public static void i(String msg) {
		android.util.Log.i(TAG, msg);		
	}
}
