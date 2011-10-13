package de.fraunhofer.igd.klarschiff.vo;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@SuppressWarnings("serial")
@Entity
public class VorgangHistoryClasses implements Serializable {

	@Id
	@OneToOne
	@JoinColumn
	Vorgang vorgang;

	@ElementCollection(fetch=FetchType.EAGER)
	Set<String> historyClasses = new HashSet<String>();

	public Vorgang getVorgang() {
		return vorgang;
	}

	public void setVorgang(Vorgang vorgang) {
		this.vorgang = vorgang;
	}

	public Set<String> getHistoryClasses() {
		return historyClasses;
	}

	public void setHistoryClasses(Set<String> historyClasses) {
		this.historyClasses = historyClasses;
	}
}
