package br.com.pnpa.lazierdroid.entities;

import java.util.Collection;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "series")
public class Serie {
	@DatabaseField(id = true)
    private long id;
	
	@DatabaseField(canBeNull = false)
    private String nome;
    
    @DatabaseField(canBeNull = true)
    private String link;

    @DatabaseField(canBeNull = true)
    private int anoInicio;
    
    @DatabaseField(canBeNull = true)
    private int anoFim;
    
    @DatabaseField(canBeNull = true)
    private int numeroTemporadas;
    
    @DatabaseField(canBeNull = true)
    private String status;
    
    @ForeignCollectionField
    private Collection<Temporada> temporadas;
    
    public Serie() {}
    
    public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public int getAnoInicio() {
		return anoInicio;
	}

	public void setAnoInicio(int anoInicio) {
		this.anoInicio = anoInicio;
	}

	public int getAnoFim() {
		return anoFim;
	}

	public void setAnoFim(int anoFim) {
		this.anoFim = anoFim;
	}

	public int getNumeroTemporadas() {
		return numeroTemporadas;
	}

	public void setNumeroTemporadas(int numeroTemporadas) {
		this.numeroTemporadas = numeroTemporadas;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Collection<Temporada> getTemporadas() {
		return temporadas;
	}

	public void setTemporadas(Collection<Temporada> temporadas) {
		this.temporadas = temporadas;
	}

	@Override
	public String toString() {
		return this.getNome();
	}
}