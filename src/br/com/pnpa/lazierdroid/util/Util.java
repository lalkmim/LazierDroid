package br.com.pnpa.lazierdroid.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
	
	public static Bitmap loadImageBitmap(String url) throws IOException {
		URL newurl = new URL(url);
		return BitmapFactory.decodeStream(newurl.openConnection().getInputStream());
	}

	public static String ajustarLink(String link) throws UnsupportedEncodingException {
//		String base = link.substring(0, link.lastIndexOf("/") + 1);
//		String nomeArquivo = link.substring(base.length(), link.length());
		
//		return base + URLEncoder.encode(nomeArquivo, "UTF-8");
		
		link = link.replaceAll("\\[", "%5B");
		link = link.replaceAll("\\]", "%5D");
		
		return link;
	}
}
