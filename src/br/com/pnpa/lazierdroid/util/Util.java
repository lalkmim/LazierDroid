package br.com.pnpa.lazierdroid.util;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.Toast;

public class Util {
	public static Toast buildToast(Context context, String msg) {
		return Toast.makeText(context, msg, Toast.LENGTH_LONG);
	}
	
	public static AlertDialog buildDialog(String msg, String titulo, Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(msg).setTitle(titulo);

		AlertDialog dialog = builder.create();
		return dialog;
	}
}
