package de.fraunhofer.igd.klarschiff.web;

import java.io.Serializable;

import de.fraunhofer.igd.klarschiff.vo.Vorgang;

@SuppressWarnings("serial")
public class VorgangErstsichtungCommand implements Serializable {

	Vorgang vorgang;
//	MultipartFile bild;

	
	public Vorgang getVorgang() {
		return vorgang;
	}
	public void setVorgang(Vorgang vorgang) {
		this.vorgang = vorgang;
	}

//	public MultipartFile getBild() {
//		return bild;
//	}
//
//
//	public void setBild(MultipartFile bild) {
//		this.bild = bild;
//	}
}
