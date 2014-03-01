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
}
