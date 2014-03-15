package br.com.pnpa.lazierdroid.entities;


public class TorrentFile {
	private String link;
	private String fileName;
	private LazierFile localFile;
	
	public LazierFile getLocalFile() {
		return localFile;
	}
	public void setLocalFile(LazierFile arquivo) {
		this.localFile = arquivo;
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
