package de.fraunhofer.igd.klarschiff.web;

import static de.fraunhofer.igd.klarschiff.web.Assert.*;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import de.fraunhofer.igd.klarschiff.dao.KategorieDao;
import de.fraunhofer.igd.klarschiff.vo.EnumNaehereBeschreibungNotwendig;
import de.fraunhofer.igd.klarschiff.vo.Kategorie;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;

@SuppressWarnings("serial")
public class VorgangNeuCommand implements Serializable {

	Vorgang vorgang = new Vorgang();
	Kategorie kategorie;
	MultipartFile bild;
	
	public void validate(BindingResult result, KategorieDao kategorieDao) {
		if (StringUtils.equals("Geben Sie ein Betreff an!",vorgang.getBetreff())) vorgang.setBetreff("");
		if (StringUtils.equals("Geben Sie Details an!",vorgang.getDetails())) vorgang.setDetails("");
		assertNotEmpty(this, result, Assert.EvaluateOn.ever, "vorgang.typ", null);
		assertNotEmpty(this, result, Assert.EvaluateOn.ever, "vorgang.oviWkt", null);
		assertNotEmpty(this, result, Assert.EvaluateOn.ever, "kategorie", null);
		assertNotEmpty(this, result, Assert.EvaluateOn.ever, "vorgang.kategorie", null);
		if (!StringUtils.isBlank(vorgang.getAutorEmail())) assertEmail(this, result, Assert.EvaluateOn.ever, "vorgang.autorEmail", null);
		EnumNaehereBeschreibungNotwendig naehereBeschreibungNotwendig = kategorieDao.viewNaehereBeschreibung(kategorie, vorgang.getKategorie());
		switch (naehereBeschreibungNotwendig) {
		case betreff:
			assertNotEmpty(this, result, Assert.EvaluateOn.ever, "vorgang.betreff", null);
			break;
		case details:
			assertNotEmpty(this, result, Assert.EvaluateOn.ever, "vorgang.details", null);
			break;
		case betreffUndDetails:
			assertNotEmpty(this, result, Assert.EvaluateOn.ever, "vorgang.betreff", null);
			assertNotEmpty(this, result, Assert.EvaluateOn.ever, "vorgang.details", null);
			break;
		}
	}
	
	
	public Vorgang getVorgang() {
		return vorgang;
	}
	public void setVorgang(Vorgang vorgang) {
		this.vorgang = vorgang;
	}
	public Kategorie getKategorie() {
		return kategorie;
	}
	public void setKategorie(Kategorie kategorie) {
		this.kategorie = kategorie;
	}


	public MultipartFile getBild() {
		return bild;
	}


	public void setBild(MultipartFile bild) {
		this.bild = bild;
	}
}
