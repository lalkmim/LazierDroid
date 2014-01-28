package br.com.pnpa.lazierdroid.entities;

import java.util.Collection;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "temporadas")
public class Temporada {
	@DatabaseField(generatedId = true)
	private long id;
	
	@DatabaseField(canBeNull = false)
	private int numero;
	
	@DatabaseField(canBeNull = true)
	@ForeignCollectionField
	private Collection<Episodio> episodios;
	
	@DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "idSerie")
	private Serie serie;

	public Temporada() {}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getNumero() {
		return numero;
	}

	public void setNumero(int numero) {
		this.numero = numero;
	}

	public Collection<Episodio> getEpisodios() {
		return episodios;
	}

	public void setEpisodios(Collection<Episodio> episodios) {
		this.episodios = episodios;
	}

	public Serie getSerie() {
		return serie;
	}

	public void setSerie(Serie serie) {
		this.serie = serie;
	}
}
