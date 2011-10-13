package de.fraunhofer.igd.klarschiff.vo;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Configurable;

@Entity
@Configurable
public class StatusKommentarVorlage {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Size(max = 100)
    private String titel;

	@Size(max = 300)
	private String text;

	/* --------------- TRANSIENT ----------------------------*/
	
	@Transient
	public String getTitelAbbreviate() {
		String str = (!StringUtils.isBlank(titel)) ? titel : text;
		return StringUtils.abbreviate(StringUtils.replace(str,"\n", " "), 20);
	}
	
	
	/* --------------- GET + SET ----------------------------*/

	public Long getId() {
        return this.id;
	}

	public void setId(Long id) {
        this.id = id;
    }

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getTitel() {
		return titel;
	}

	public void setTitel(String titel) {
		this.titel = titel;
	}

}
