package br.com.pnpa.lazierdroid.services;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Locale;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import jcifs.smb.SmbException;
import br.com.pnpa.lazierdroid.entities.DownloadFile;
import br.com.pnpa.lazierdroid.entities.LazierFile;
import br.com.pnpa.lazierdroid.entities.LegendaFile;
import br.com.pnpa.lazierdroid.util.Log;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;

public class IOService extends BaseService {
	public static LazierFile salvarArquivo(String link, String caminhoArquivo) throws Exception {
		DownloadFile file = downloadFile(link);
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
		
		if(isGZIP(arquivo)) {
			arquivo = extrairGZIP(caminhoArquivo, arquivo);
		}
		
		return arquivo;
	}

	private static LazierFile extrairGZIP(String caminhoArquivo, LazierFile arquivo) throws Exception {
		String caminhoArquivoTemporario = caminhoArquivo + ".gz";
		
		LazierFile arquivoTemporario = new LazierFile(caminhoArquivoTemporario);
		boolean renameRealizado = arquivo.renameTo(arquivoTemporario);
		
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
	
	public static boolean isGZIP(LazierFile arquivo) throws IOException {
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
	
    private static File createFileFromRAR(FileHeader fh, String destination) {
    	return createFileFromRAR(fh, new File(destination));
    }
    
    private static File createFileFromRAR(FileHeader fh, File destination) {
		File f = null;
		String name = null;
		if (fh.isFileHeader() && fh.isUnicode()) {
		    name = fh.getFileNameW();
		} else {
		    name = fh.getFileNameString();
		}
		f = new File(destination, name);
		if (!f.exists()) {
		    try {
			f = makeFileFromRAR(destination, name);
		    } catch (IOException e) {
			Log.e("error creating the new file: " + f.getName(), e);
		    }
		}
		return f;
    }

    private static File makeFileFromRAR(File destination, String name) throws IOException {
		String[] dirs = name.split("\\\\");
		if (dirs == null) {
		    return null;
		}
		
		String path = "";
		int size = dirs.length;
		
		if (size == 1) {
		    return new File(destination, name);
		} else if (size > 1) {
		    for (int i = 0; i < dirs.length - 1; i++) {
				path = path + File.separator + dirs[i];
				new File(destination, path).mkdir();
		    }
		    path = path + File.separator + dirs[dirs.length - 1];
		    File f = new File(destination, path);
		    f.createNewFile();
		    
		    return f;
		} else {
		    return null;
		}
    }

    public static LegendaFile extrairArquivoRARPeloNome(String tempFolder, String nomeProcurado, File arquivoLocal) throws RarException, IOException, FileNotFoundException {
		LegendaFile legendaFile = null;
		
		LazierFile arquivoExtraido = extrairArquivoRARPeloNomeExato(tempFolder, nomeProcurado, arquivoLocal);
		if(arquivoExtraido == null) {
			arquivoExtraido = extrairArquivoRARPeloGrupo(tempFolder, nomeProcurado, arquivoLocal);
		}
		
		if(arquivoExtraido != null) {
			legendaFile = new LegendaFile();
			legendaFile.setLocalFile(new LazierFile(arquivoExtraido.getCaminhoArquivo()));
			legendaFile.setFileName(nomeProcurado);
		}
		
		return legendaFile;
	}

	private static LazierFile extrairArquivoRARPeloGrupo(String tempFolder, String nomeProcurado, File arquivoLocal) throws RarException, IOException, MalformedURLException, SmbException {
		LazierFile arquivoExtraido = null;
		FileHeader fh = null;
		
		Archive arch = new Archive(arquivoLocal);
		String grupoProcurado = nomeProcurado.substring(nomeProcurado.lastIndexOf("-") + 1).toLowerCase(Locale.US);
		
		while((fh = arch.nextFileHeader()) != null) {
			String nomeArquivoCompactado = fh.getFileNameString();
			Log.d("arquivoCompactado: " + nomeArquivoCompactado);
			
			if(nomeArquivoCompactado.toLowerCase(Locale.US).endsWith(grupoProcurado)) {
				arquivoExtraido = new LazierFile(createFileFromRAR(fh, tempFolder).getAbsolutePath());
				LazierFile arquivoCorreto = new LazierFile(tempFolder + nomeProcurado);
				arquivoExtraido.renameTo(arquivoCorreto);
				
				OutputStream stream = arquivoExtraido.getOutputStream();
				arch.extractFile(fh, stream);
				stream.close();
				
				break;
			}
		}
		
		arch.close();
		
		return arquivoExtraido;
	}

	private static LazierFile extrairArquivoRARPeloNomeExato(String tempFolder, String nomeProcurado, File arquivoLocal) throws RarException, IOException, MalformedURLException {
		LazierFile arquivoExtraido = null;
		FileHeader fh = null;
		
		Archive arch = new Archive(arquivoLocal);
		
		while((fh = arch.nextFileHeader()) != null) {
			String nomeArquivoCompactado = fh.getFileNameString();
			Log.d("arquivoCompactado: " + nomeArquivoCompactado);
			
			if(nomeArquivoCompactado.equalsIgnoreCase(nomeProcurado)) {
				arquivoExtraido = new LazierFile(createFileFromRAR(fh, tempFolder).getAbsolutePath());
				OutputStream stream = arquivoExtraido.getOutputStream();
				arch.extractFile(fh, stream);
				stream.close();
				
				if(!nomeArquivoCompactado.equals(nomeProcurado)) {
					LazierFile arquivoCorreto = new LazierFile(tempFolder + nomeProcurado);
					arquivoExtraido.renameTo(arquivoCorreto);
				}
				
				break;
			}
		}
		
		arch.close();
		
		return arquivoExtraido;
	}
    
    public static LegendaFile extrairArquivoZIPPeloNome(String tempFolder, String nomeProcurado, File arquivoLocal) throws RarException, IOException, FileNotFoundException {
		LegendaFile legendaFile = null;
		
		LazierFile arquivoExtraido = extrairArquivoZIPPeloNomeExato(tempFolder, nomeProcurado, arquivoLocal);
		if(arquivoExtraido == null) {
			arquivoExtraido = extrairArquivoZIPPeloGrupo(tempFolder, nomeProcurado, arquivoLocal);
		}
		
		if(arquivoExtraido != null) {
			legendaFile = new LegendaFile();
			legendaFile.setLocalFile(new LazierFile(arquivoExtraido.getCaminhoArquivo()));
			legendaFile.setFileName(nomeProcurado);
		}
		
		return legendaFile;
    }

	private static LazierFile extrairArquivoZIPPeloGrupo(String tempFolder, String nomeProcurado, File arquivoLocal) throws FileNotFoundException, IOException, MalformedURLException, SmbException {
		LazierFile arquivoExtraido = null;
		ZipEntry ze = null;
		
		ZipInputStream zis = new ZipInputStream(new FileInputStream(arquivoLocal));
		String grupoProcurado = nomeProcurado.substring(nomeProcurado.lastIndexOf("-") + 1).toLowerCase(Locale.US);
		
		while((ze = zis.getNextEntry()) != null) {
			String nomeArquivoCompactado = ze.getName();
			Log.d("arquivoCompactado: " + nomeArquivoCompactado);
			
			if(nomeArquivoCompactado.toLowerCase(Locale.US).endsWith(grupoProcurado)) {
				arquivoExtraido = new LazierFile(tempFolder + nomeArquivoCompactado);
				LazierFile arquivoCorreto = new LazierFile(tempFolder + nomeProcurado);
				arquivoExtraido.renameTo(arquivoCorreto);
				
				OutputStream stream = arquivoExtraido.getOutputStream();
				transferirDados(zis, stream);
				stream.close();
				
				break;
			}
		}
		
		zis.close();
		
		return arquivoExtraido;
	}

	private static LazierFile extrairArquivoZIPPeloNomeExato(String tempFolder, String nomeProcurado, File arquivoLocal) throws FileNotFoundException, IOException, MalformedURLException {
		LazierFile arquivoExtraido = null;
		ZipEntry ze = null;
    	
		ZipInputStream zis = new ZipInputStream(new FileInputStream(arquivoLocal));

    	while((ze = zis.getNextEntry()) != null) {
			String nomeArquivoCompactado = ze.getName();
			Log.d("arquivoCompactado: " + nomeArquivoCompactado);
			
			if(nomeArquivoCompactado.equalsIgnoreCase(nomeProcurado)) {
				arquivoExtraido = new LazierFile(tempFolder + nomeArquivoCompactado);
				OutputStream stream = arquivoExtraido.getOutputStream();
				transferirDados(zis, stream);
				stream.close();
				
				if(!nomeArquivoCompactado.equals(nomeProcurado)) {
					LazierFile arquivoCorreto = new LazierFile(tempFolder + nomeProcurado);
					arquivoExtraido.renameTo(arquivoCorreto);
				}
				
				break;
			}
		}
		
		zis.close();
		
		return arquivoExtraido;
	}

	public static void copiarArquivo(LazierFile origem, LazierFile destino) throws IOException {
		transferirDados(origem.getInputStream(), destino.getOutputStream());
	}
}