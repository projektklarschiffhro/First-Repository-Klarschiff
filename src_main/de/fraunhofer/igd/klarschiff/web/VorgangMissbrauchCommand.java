package de.fraunhofer.igd.klarschiff.web;

import java.io.Serializable;

import de.fraunhofer.igd.klarschiff.vo.Missbrauchsmeldung;

@SuppressWarnings("serial")
public class VorgangMissbrauchCommand implements Serializable {
	Missbrauchsmeldung missbrauchsmeldung;
	Long missbrauchsmeldungId;
	
	public Missbrauchsmeldung getMissbrauchsmeldung() {
		return missbrauchsmeldung;
	}
	public void setMissbrauchsmeldung(Missbrauchsmeldung missbrauchsmeldung) {
		this.missbrauchsmeldung = missbrauchsmeldung;
	}
	public Long getMissbrauchsmeldungId() {
		return missbrauchsmeldungId;
	}
	public void setMissbrauchsmeldungId(Long missbrauchsmeldungId) {
		this.missbrauchsmeldungId = missbrauchsmeldungId;
	}
}
