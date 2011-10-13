package de.fraunhofer.igd.klarschiff.vo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;

@SuppressWarnings("serial")
@Entity
@Component
@Configurable
public class Verlauf implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

    @ManyToOne(cascade=CascadeType.PERSIST)
    @JoinColumn
    private Vorgang vorgang;

	@Version
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "S-")
    private Date datum;

	private String nutzer; 
	
    @NotNull
    @Enumerated(EnumType.STRING)
    private EnumVerlaufTyp typ;

    private String wertAlt;

    private String wertNeu;

	/* --------------- GET + SET ----------------------------*/

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Vorgang getVorgang() {
		return vorgang;
	}

	public void setVorgang(Vorgang vorgang) {
		this.vorgang = vorgang;
	}

	public Date getDatum() {
		return datum;
	}

	public void setDatum(Date datum) {
		this.datum = datum;
	}

	public EnumVerlaufTyp getTyp() {
		return typ;
	}

	public void setTyp(EnumVerlaufTyp typ) {
		this.typ = typ;
	}

	public String getWertAlt() {
		return wertAlt;
	}

	public void setWertAlt(String wertAlt) {
		this.wertAlt = wertAlt;
	}

	public String getWertNeu() {
		return wertNeu;
	}

	public void setWertNeu(String wertNeu) {
		this.wertNeu = wertNeu;
	}

	public String getNutzer() {
		return nutzer;
	}

	public void setNutzer(String nutzer) {
		this.nutzer = nutzer;
	}

}
