package br.com.pnpa.lazierdroid.entities;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "temporadas")
public class Temporada {
	@DatabaseField(generatedId = true)
	private int id;
	
	@DatabaseField(canBeNull = false, uniqueIndexName = "un_temp")
	private int numero;
	
	@ForeignCollectionField(eager = true)
	private ForeignCollection<Episodio> episodios;
	
	@DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "idSerie", uniqueIndexName = "un_temp")
	private Serie serie;

	public Temporada() {}
	
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

	public ForeignCollection<Episodio> getEpisodios() {
		return episodios;
	}

	public Serie getSerie() {
		return serie;
	}

	public void setSerie(Serie serie) {
		this.serie = serie;
	}
}
