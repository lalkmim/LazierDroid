package br.com.pnpa.lazierdroid.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "episodios")
public class Episodio {
	@DatabaseField(generatedId = true)
	private long id;
	
	@DatabaseField(canBeNull = true)
	private String date;
	
	@DatabaseField(canBeNull = true)
	private String link;
	
	@DatabaseField(canBeNull = true)
	private String title;
	
	@DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "idTemporada")
	private Temporada temporada;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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
}
