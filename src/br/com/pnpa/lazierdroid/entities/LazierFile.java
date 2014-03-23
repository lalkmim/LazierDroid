package br.com.pnpa.lazierdroid.entities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class LazierFile {
	private SmbFile arquivoNaRede;
	private File arquivoLocal;
	private String caminhoArquivo;
	
	public LazierFile(String caminhoArquivo) throws MalformedURLException {
		this.caminhoArquivo = caminhoArquivo;
		
		if(caminhoArquivo.startsWith("smb://")) {
			this.arquivoNaRede = new SmbFile(caminhoArquivo);
		} else {
			this.arquivoLocal = new File(caminhoArquivo);
		}
	}
		
	public SmbFile getArquivoNaRede() {
		return this.arquivoNaRede;
	}
	public void setArquivoNaRede(SmbFile arquivoNaRede) {
		this.arquivoNaRede = arquivoNaRede;
	}
	public File getArquivoLocal() {
		return this.arquivoLocal;
	}
	public void setArquivoLocal(File arquivoLocal) {
		this.arquivoLocal = arquivoLocal;
	}
	public String getCaminhoArquivo() {
		return this.caminhoArquivo;
	}
	public void setCaminhoArquivo(String caminhoArquivo) {
		this.caminhoArquivo = caminhoArquivo;
	}
	
	public InputStream getInputStream() throws IOException {
		if(this.arquivoNaRede != null) {
			return this.arquivoNaRede.getInputStream();
		} else {
			return new FileInputStream(this.arquivoLocal);
		}
	}
	
	public OutputStream getOutputStream() throws IOException {
		if(this.arquivoNaRede != null) {
			return this.arquivoNaRede.getOutputStream();
		} else {
			return new FileOutputStream(this.arquivoLocal);
		}
	}

	public boolean renameTo(LazierFile arquivoTemporario) throws SmbException {
		boolean retorno = false;
		if (this.arquivoNaRede != null) {
			this.arquivoNaRede.renameTo(arquivoTemporario.getArquivoNaRede());
			retorno = true;
		} else {
			retorno = this.arquivoLocal.renameTo(arquivoTemporario.getArquivoLocal());
		}
		
		this.caminhoArquivo = arquivoTemporario.getCaminhoArquivo();
		
		return retorno;
	}

	public void delete() throws SmbException {
		if(this.arquivoNaRede != null) {
			this.arquivoNaRede.delete();
		} else {
			this.arquivoLocal.delete();
		}
	}

	public boolean exists() throws SmbException {
		if(this.arquivoNaRede != null) {
			return this.arquivoNaRede.exists();
		} else {
			return this.arquivoLocal.exists();
		}
	}

	public void mkdirs() throws SmbException {
		if(this.arquivoNaRede != null) {
			this.arquivoNaRede.mkdirs();
		} else {
			this.arquivoLocal.mkdirs();
		}
	}	
}