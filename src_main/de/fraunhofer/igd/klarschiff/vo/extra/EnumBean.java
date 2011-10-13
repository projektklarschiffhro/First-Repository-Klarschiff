package de.fraunhofer.igd.klarschiff.vo.extra;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import de.fraunhofer.igd.klarschiff.vo.EnumText;

@MappedSuperclass
public class EnumBean {
	@Id
	String id;
	
	String text;
	
	int ordinal;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getOrdinal() {
		return ordinal;
	}

	public void setOrdinal(int ordinal) {
		this.ordinal = ordinal;
	}

	public EnumBean fill(EnumText enumText) {
		id = enumText.name();
		ordinal = enumText.ordinal();
		text = enumText.getText();
		return this;
	}
}
