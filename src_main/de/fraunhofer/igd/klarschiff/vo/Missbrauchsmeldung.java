package de.fraunhofer.igd.klarschiff.vo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.format.annotation.DateTimeFormat;

@SuppressWarnings("serial")
@Entity
@Configurable
public class Missbrauchsmeldung implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@ManyToOne
    private Vorgang vorgang;
	
	@Size(max = 32)
    private String hash;
    
    @Lob
    @Type(type="org.hibernate.type.TextType")
	private String text;
	
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "S-")
    private Date datum;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "S-")
    private Date datumBestaetigung;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "S-")
    private Date datumAbarbeitung;

	/* --------------- GET + SET ----------------------------*/
    
	public Vorgang getVorgang() {
		return vorgang;
	}

	public void setVorgang(Vorgang vorgang) {
		this.vorgang = vorgang;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public Date getDatum() {
		return datum;
	}

	public void setDatum(Date datum) {
		this.datum = datum;
	}

	public Date getDatumBestaetigung() {
		return datumBestaetigung;
	}

	public void setDatumBestaetigung(Date datumBestaetigung) {
		this.datumBestaetigung = datumBestaetigung;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getDatumAbarbeitung() {
		return datumAbarbeitung;
	}

	public void setDatumAbarbeitung(Date datumAbarbeitung) {
		this.datumAbarbeitung = datumAbarbeitung;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
