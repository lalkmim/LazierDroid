package br.com.pnpa.lazierdroid.services;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;

import br.com.pnpa.lazierdroid.entities.DownloadFile;
import br.com.pnpa.lazierdroid.entities.LazierFile;
import br.com.pnpa.lazierdroid.util.Log;

public class IOService extends BaseService {
	public static LazierFile salvarArquivo(String linkTorrent, String caminhoArquivo) throws Exception {
		DownloadFile file = downloadFile(linkTorrent);
		if(file == null) {
			return null;
		}
		
		LazierFile arquivo = new LazierFile(caminhoArquivo);
		InputStream is = file.getIs();
		OutputStream os = arquivo.getOutputStream();
		
		try {
			transferirDados(is, os);
		} finally {
			os.close();
			is.close();
		}
		
		if(isGZipped(arquivo)) {
			arquivo = extrairGZip(caminhoArquivo, arquivo);
		}
		
		return arquivo;
	}

	private static LazierFile extrairGZip(String caminhoArquivo, LazierFile arquivo) throws Exception {
		String caminhoArquivoTemporario = caminhoArquivo + ".gz";
		
		LazierFile arquivoTemporario = new LazierFile(caminhoArquivoTemporario);
		Log.d("arquivo.caminhoArquivo (antes): " + arquivo.getCaminhoArquivo());
		Log.d("arquivoTemporario.caminhoArquivo (antes): " + arquivoTemporario.getCaminhoArquivo());
		boolean renameRealizado = arquivo.renameTo(arquivoTemporario);
		Log.d("arquivo.caminhoArquivo (depois): " + arquivo.getCaminhoArquivo());
		Log.d("arquivoTemporario.caminhoArquivo (depois): " + arquivoTemporario.getCaminhoArquivo());
		Log.d("renameRealizado: " + renameRealizado);
		
		if(!renameRealizado) {
			throw new Exception("Nao foi possivel renomear o arquivo.");
		}
		
		GZIPInputStream gis = new GZIPInputStream(arquivoTemporario.getInputStream());
		arquivo = new LazierFile(caminhoArquivo);
		OutputStream novoOS = arquivo.getOutputStream();
		try {
			transferirDados(gis, novoOS);
		} finally {
			novoOS.close();
			gis.close();
		}
		
		arquivoTemporario.delete();
		
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
	
//	public static File arquivoNaRede(String caminhoNaRede) throws IOException {
//		InputStream in = new SmbFile(caminhoNaRede).getInputStream();
//        final File tempFile = File.createTempFile("file", "tmp");
//        tempFile.deleteOnExit();
//        OutputStream out = new FileOutputStream(tempFile);
//        transferirDados(in, out);
//        return tempFile;
//    }
	
	public static LazierFile downloadTemporario(String link) throws IOException {
		DownloadFile downloadFile = downloadFile(link);
		InputStream in = downloadFile.getIs();
		
		String nomeArquivo = downloadFile.getFileName();
		String extensaoArquivo = nomeArquivo.substring(nomeArquivo.lastIndexOf("."));
		
        final File tempFile = File.createTempFile("file", extensaoArquivo);
        
        tempFile.deleteOnExit();
        
        OutputStream out = new FileOutputStream(tempFile);
        transferirDados(in, out);
        
        return new LazierFile(tempFile.getAbsolutePath());
    }
	
	public static boolean isGZipped(LazierFile arquivo) throws IOException {
		InputStream in = arquivo.getInputStream();
		if (!in.markSupported()) {
			in = new BufferedInputStream(in);
		}
		
		in.mark(2);
		int magic = 0;
		
		try {
			magic = in.read() & 0xff | ((in.read() << 8) & 0xff00);
			in.reset();
		} catch (IOException e) {
			e.printStackTrace(System.err);
			return false;
		}

		in.close();
		return magic == GZIPInputStream.GZIP_MAGIC;
	}
}