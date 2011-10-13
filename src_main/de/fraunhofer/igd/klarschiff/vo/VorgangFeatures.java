package de.fraunhofer.igd.klarschiff.vo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@SuppressWarnings("serial")
@Entity
public class VorgangFeatures implements Serializable {

	@Id
	@OneToOne
	@JoinColumn
	Vorgang vorgang;

	@ElementCollection(fetch=FetchType.EAGER)
	Map<String, String> features = new HashMap<String, String>();

	public Vorgang getVorgang() {
		return vorgang;
	}

	public void setVorgang(Vorgang vorgang) {
		this.vorgang = vorgang;
	}

	public Map<String, String> getFeatures() {
		return features;
	}

	public void setFeatures(Map<String, String> features) {
		this.features = features;
	}
}
