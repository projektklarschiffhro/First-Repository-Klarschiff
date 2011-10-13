package de.fraunhofer.igd.klarschiff.web;

import java.io.Serializable;

import de.fraunhofer.igd.klarschiff.vo.Vorgang;

@SuppressWarnings("serial")
public class VorgangDelegiertBearbeitenCommand implements Serializable {

	Vorgang vorgang;
	String kommentar;

	Integer page;
	Integer size;
	
	public Vorgang getVorgang() {
		return vorgang;
	}
	public void setVorgang(Vorgang vorgang) {
		this.vorgang = vorgang;
	}
	public String getKommentar() {
		return kommentar;
	}
	public void setKommentar(String kommentar) {
		this.kommentar = kommentar;
	}
	public Integer getPage() {
		return page;
	}
	public void setPage(Integer page) {
		this.page = page;
	}
	public Integer getSize() {
		return size;
	}
	public void setSize(Integer size) {
		this.size = size;
	}
}
