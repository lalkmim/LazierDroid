package br.com.pnpa.lazierdroid.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "episodios")
public class Episodio {
	@DatabaseField(generatedId = true)
	private int id;
	
	@DatabaseField(canBeNull = false, uniqueIndexName = "un_epis")
	private int numero;
	
	@DatabaseField(canBeNull = true)
	private String date;
	
	@DatabaseField(canBeNull = true)
	private String link;
	
	@DatabaseField(canBeNull = true)
	private String title;
	
	@DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "idTemporada", uniqueIndexName = "un_epis")
	private Temporada temporada;
	
	@DatabaseField(canBeNull = true)
	private String linkTorrent;
	
	@DatabaseField(canBeNull = true)
	private String caminhoTorrent;
	
	@DatabaseField(canBeNull = true)
	private String nomeVideo;
	
	@DatabaseField(canBeNull = true)
	private String caminhoVideo;
	
	@DatabaseField(canBeNull = true)
	private String nomeLegenda;
	
	@DatabaseField(canBeNull = true)
	private String caminhoLegenda;
	
	@DatabaseField(canBeNull = true)
	private String linkLegenda;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getNumero() {
		return numero;
	}

	public void setNumero(int numero) {
		this.numero = numero;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Temporada getTemporada() {
		return temporada;
	}

	public void setTemporada(Temporada temporada) {
		this.temporada = temporada;
	}

	public String getNumeroFormatado() {
		String numeroFormatado = "S";
		
		int numTemporada = getTemporada().getNumero();
		if(numTemporada < 10) numeroFormatado += "0";
		
		numeroFormatado += numTemporada + "E";
		
		if(getNumero() < 10) numeroFormatado += "0";
		numeroFormatado += getNumero();
		
		return numeroFormatado;
	}

	public String getNomeLegenda() {
		return nomeLegenda;
	}

	public void setNomeLegenda(String nomeLegenda) {
		this.nomeLegenda = nomeLegenda;
	}

	public String getNomeVideo() {
		return nomeVideo;
	}

	public void setNomeVideo(String nomeVideo) {
		this.nomeVideo = nomeVideo;
	}

	public String getLinkTorrent() {
		return linkTorrent;
	}

	public void setLinkTorrent(String linkTorrent) {
		this.linkTorrent = linkTorrent;
	}
	
	public String getStatusVideo() {
		if(this.getNomeVideo() != null)
			return "OK";
		
		if(this.getLinkTorrent() != null)
			return "Baixando";
		
		return "Pendente"; 
	}
	
	public String getStatusLegenda() {
		if(this.getNomeLegenda() != null)
			return "OK";
		else
			return "Pendente";
	}

	public String getCaminhoTorrent() {
		return caminhoTorrent;
	}

	public void setCaminhoTorrent(String caminhoTorrent) {
		this.caminhoTorrent = caminhoTorrent;
	}

	public String getCaminhoVideo() {
		return caminhoVideo;
	}

	public void setCaminhoVideo(String caminhoVideo) {
		this.caminhoVideo = caminhoVideo;
	}

	public String getCaminhoLegenda() {
		return caminhoLegenda;
	}

	public void setCaminhoLegenda(String caminhoLegenda) {
		this.caminhoLegenda = caminhoLegenda;
	}

	public String getLinkLegenda() {
		return linkLegenda;
	}

	public void setLinkLegenda(String linkLegenda) {
		this.linkLegenda = linkLegenda;
	}
}
