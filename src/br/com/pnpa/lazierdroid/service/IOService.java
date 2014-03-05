package br.com.pnpa.lazierdroid.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;

import jcifs.smb.SmbFile;

import org.apache.http.client.ClientProtocolException;

import android.util.Log;
import br.com.pnpa.lazierdroid.entities.DownloadFile;
import br.com.pnpa.lazierdroid.util.Util;

public class IOService extends BaseService {
	public static SmbFile salvarArquivo(String linkTorrent, String caminhoArquivo) throws ClientProtocolException, IOException {
		DownloadFile file = downloadFile(linkTorrent);
		if(file == null) {
			return null;
		}
		
		SmbFile arquivo = new SmbFile(caminhoArquivo);
		OutputStream os = arquivo.getOutputStream();
		InputStream is = file.getIs();
		
		try {
			transferirDados(is, os);
		} finally {
			os.close();
			is.close();
		}
		
		if(Util.isGZipped(arquivo.getInputStream())) {
			SmbFile arquivoTemporario = new SmbFile(arquivo.getCanonicalPath() + ".gz");
			arquivo.renameTo(arquivoTemporario);
			GZIPInputStream gis = new GZIPInputStream(arquivo.getInputStream());
			OutputStream novoOS = null;
			try {
				novoOS = new SmbFile(caminhoArquivo).getOutputStream();
				transferirDados(gis, novoOS);
			} finally {
				novoOS.close();
				gis.close();
			}
			
			arquivoTemporario.delete();
		}
		
		return arquivo;
	}
	
	private static void transferirDados(InputStream is, OutputStream os) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(is);
		BufferedOutputStream bos = new BufferedOutputStream(os);
		
		byte[] buffer = new byte[2048];
		int len = 0;
		
		try {
			while ((len = bis.read(buffer)) > 0) {
				bos.write(buffer, 0, len);
			}
		} finally {
			bos.close();
			bis.close();
		}
	}
}
