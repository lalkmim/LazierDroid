package br.com.pnpa.lazierdroid.entities;

import jcifs.smb.SmbFile;

public class Torrent {
	private String link;
	private String fileName;
	private SmbFile localFile;
	
	public SmbFile getLocalFile() {
		return localFile;
	}
	public void setLocalFile(SmbFile localFile) {
		this.localFile = localFile;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
}
