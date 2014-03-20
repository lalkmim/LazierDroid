package br.com.pnpa.lazierdroid.services;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import br.com.pnpa.lazierdroid.entities.LazierFile;
import br.com.pnpa.lazierdroid.entities.LegendaFile;
import br.com.pnpa.lazierdroid.util.Log;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;

public class RARService extends BaseService {
    public static void extractArchive(String archive, String destination) {
		if (archive == null || destination == null) {
		    throw new RuntimeException("archive and destination must me set");
		}
		
		File arch = new File(archive);
		if (!arch.exists()) {
		    throw new RuntimeException("the archive does not exit: " + archive);
		}
		
		File dest = new File(destination);
		if (!dest.exists() || !dest.isDirectory()) {
		    throw new RuntimeException("the destination must exist and point to a directory: " + destination);
		}
		
		extractArchive(arch, dest);
    }

//    public static void main(String[] args) {
//		if (args.length == 2) {
//		    extractArchive(args[0], args[1]);
//		} else {
//		    System.out.println("usage: java -jar extractArchive.jar <thearchive> <the destination directory>");
//		}
//    }

    public static void extractArchive(File archive, File destination) {
		Archive arch = null;
		try {
		    arch = new Archive(archive);
		} catch (RarException e) {
		    Log.e(e);
		} catch (IOException e1) {
		    Log.e(e1);
		}
		
		if (arch != null) {
		    if (arch.isEncrypted()) {
		    	Log.w("archive is encrypted cannot extreact");
		    	return;
		    }
		    
		    FileHeader fh = null;
		    while (true) {
		    	fh = arch.nextFileHeader();
		    	if (fh == null) {
		    		break;
		    	}
		    	
		    	if (fh.isEncrypted()) {
		    		Log.w("file is encrypted cannot extract: " + fh.getFileNameString());
		    		continue;
		    	}
			
		    	Log.i("extracting: " + fh.getFileNameString());
		    	try {
		    		if (fh.isDirectory()) {
		    			createDirectory(fh, destination);
		    		} else {
		    			File f = createFile(fh, destination);
		    			OutputStream stream = new FileOutputStream(f);
		    			arch.extractFile(fh, stream);
		    			stream.close();
		    		}
		    	} catch (IOException e) {
		    		Log.e("error extracting the file", e);
		    	} catch (RarException e) {
		    		Log.e("error extraction the file", e);
		    	}
		    }
		}
    }

    private static File createFile(FileHeader fh, String destination) {
    	return createFile(fh, new File(destination));
    }
    
    private static File createFile(FileHeader fh, File destination) {
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
			f = makeFile(destination, name);
		    } catch (IOException e) {
			Log.e("error creating the new file: " + f.getName(), e);
		    }
		}
		return f;
    }

    private static File makeFile(File destination, String name) throws IOException {
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

    private static void createDirectory(FileHeader fh, File destination) {
		File f = null;
		if (fh.isDirectory() && fh.isUnicode()) {
		    f = new File(destination, fh.getFileNameW());
		    if (!f.exists()) {
		    	makeDirectory(destination, fh.getFileNameW());
		    }
		} else if (fh.isDirectory() && !fh.isUnicode()) {
		    f = new File(destination, fh.getFileNameString());
		    if (!f.exists()) {
		    	makeDirectory(destination, fh.getFileNameString());
		    }
		}
    }

    private static void makeDirectory(File destination, String fileName) {
		String[] dirs = fileName.split("\\\\");
		if (dirs == null) {
		    return;
		}

		String path = "";
		for (String dir : dirs) {
		    path = path + File.separator + dir;
		    new File(destination, path).mkdir();
		}
    }
    
    public static LegendaFile extrairArquivoPeloNome(String tempFolder, String nomeProcurado, File arquivoLocal) throws RarException, IOException, FileNotFoundException {
		Archive arch = new Archive(arquivoLocal);
		FileHeader fh = null;
		File arquivoExtraido = null;
		
		while((fh = arch.nextFileHeader()) != null) {
			String arquivoCompactado = fh.getFileNameString();
			Log.d("arquivoCompactado: " + arquivoCompactado);
			
			if(arquivoCompactado.equals(nomeProcurado)) {
				arquivoExtraido = createFile(fh, tempFolder);
				OutputStream stream = new FileOutputStream(arquivoExtraido);
				arch.extractFile(fh, stream);
				stream.close();
			}
		}
		
		arch.close();
		
		if(arquivoExtraido == null) {
			return null;
		}
		
		LegendaFile legendaFile = new LegendaFile();
		legendaFile.setLocalFile(new LazierFile(arquivoExtraido.getAbsolutePath()));
		legendaFile.setFileName(nomeProcurado);
		
		return legendaFile;
	}
}