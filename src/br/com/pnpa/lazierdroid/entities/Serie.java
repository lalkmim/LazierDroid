package br.com.pnpa.lazierdroid.entities;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "series")
public class Serie extends LazierEntity {
	private static final long serialVersionUID = 1L;

	@DatabaseField(id = true)
    private int id;
	
	@DatabaseField(canBeNull = false)
    private String nome;
    
    @DatabaseField(canBeNull = true)
    private String link;

    @DatabaseField(canBeNull = true)
    private int anoInicio;
    
    @DatabaseField(canBeNull = true)
    private String imageURL;
    
    @DatabaseField(canBeNull = true)
    private String status;
    
    @ForeignCollectionField(eager = true)
    private ForeignCollection<Temporada> temporadas;
    
    public Serie() {}
    
    public int getId() {
		return id;
	}

	public void setId(int id) {
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public ForeignCollection<Temporada> getTemporadas() {
		return temporadas;
	}

	@Override
	public String toString() {
		return this.getNome();
	}

	public String getImageURL() {
		return imageURL;
	}

	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}
}