package de.fraunhofer.igd.klarschiff.web;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import de.fraunhofer.igd.klarschiff.vo.EnumPrioritaet;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangStatus;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangTyp;
import de.fraunhofer.igd.klarschiff.vo.Kategorie;



@SuppressWarnings("serial")
public class VorgangSuchenCommand implements Serializable {

	public enum Suchtyp { einfach, erweitert }; 
	public enum EinfacheSuche { offene, abgeschlossene };
	
	Integer page;
	Integer size;
	Integer order;
	Integer orderDirection;
	
	// einfache vs. erweiterte suche
	Suchtyp suchtyp;
	
	// einfach Suche
	EinfacheSuche einfacheSuche;
	
	// erweiterte Suche
	String erweitertFulltext;
	EnumVorgangTyp erweitertVorgangTyp;
	Kategorie erweitertHauptkategorie;
	Kategorie erweitertKategorie;
	Date erweitertDatumVon;
	Date erweitertDatumBis;
	EnumVorgangStatus[] erweitertVorgangStatus;
	Boolean erweitertArchiviert;
	String erweitertZustaendigkeit;
	Long erweitertUnterstuetzerAb;
	EnumPrioritaet erweitertPrioritaet;
	String erweitertDelegiertAn;
	Integer erweitertStadtteilgrenze;
	
	//NUR ADMIN dürfen andere Zuständigkeiten sehen
	
	public String getOrderString() {
		switch(order) {
			case 0: return "vo.id";
			case 1: return "vo.typ";
			case 2: return "vo.datum";
			case 3: return "MAX(ve.datum)";
			case 4: return "vo.kategorie.parent.name,vo.kategorie.name";
			case 5: return "vo.statusOrdinal";
			case 6: return "COUNT(DISTINCT un.hash)";
			case 7: return "vo.zustaendigkeit";
			case 8: return "vo.prioritaetOrdinal";
			default: return "";
		}
	}
	
	public String getOrderDirectionString() {
		switch(orderDirection) {
			case 1: return "desc";
			default: return "asc";
		}
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
	public Suchtyp getSuchtyp() {
		return suchtyp;
	}
	public void setSuchtyp(Suchtyp suchtyp) {
		setPage(1);
		this.suchtyp = suchtyp;
	}
	public EinfacheSuche getEinfacheSuche() {
		return einfacheSuche;
	}
	public void setEinfacheSuche(EinfacheSuche einfacheSuche) {
		this.einfacheSuche = einfacheSuche;
	}
	public Integer getOrder() {
		return order;
	}
	public void setOrder(Integer order) {
		this.order = order;
	}
	public Integer getOrderDirection() {
		return orderDirection;
	}
	public void setOrderDirection(Integer orderDirection) {
		this.orderDirection = orderDirection;
	}

	public String getErweitertFulltext() {
		return erweitertFulltext;
	}
	
	public void setErweitertFulltext(String erweitertFulltext) {
		try {
			if (!StringUtils.isBlank(erweitertFulltext))
				this.erweitertFulltext = new String(erweitertFulltext.getBytes(), "UTF-8");		
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public EnumVorgangTyp getErweitertVorgangTyp() {
		return erweitertVorgangTyp;
	}

	public void setErweitertVorgangTyp(EnumVorgangTyp erweitertVorgangTyp) {
		this.erweitertVorgangTyp = erweitertVorgangTyp;
	}

	public Kategorie getErweitertHauptkategorie() {
		return erweitertHauptkategorie;
	}

	public void setErweitertHauptkategorie(Kategorie erweitertHauptkategorie) {
		this.erweitertHauptkategorie = erweitertHauptkategorie;
	}

	public Kategorie getErweitertKategorie() {
		return erweitertKategorie;
	}

	public void setErweitertKategorie(Kategorie erweitertKategorie) {
		this.erweitertKategorie = erweitertKategorie;
	}

	public Date getErweitertDatumVon() {
		return erweitertDatumVon;
	}

	public void setErweitertDatumVon(Date erweitertDatumVon) {
		this.erweitertDatumVon = erweitertDatumVon;
	}

	public Date getErweitertDatumBis() {
		return erweitertDatumBis;
	}

	public void setErweitertDatumBis(Date erweitertDatumBis) {
		this.erweitertDatumBis = erweitertDatumBis;
	}

	public EnumVorgangStatus[] getErweitertVorgangStatus() {
		return erweitertVorgangStatus;
	}

	public void setErweitertVorgangStatus(EnumVorgangStatus[] erweitertVorgangStatus) {
		this.erweitertVorgangStatus = erweitertVorgangStatus;
	}

	public Boolean getErweitertArchiviert() {
		return erweitertArchiviert;
	}

	public void setErweitertArchiviert(Boolean erweitertArchiviert) {
		this.erweitertArchiviert = erweitertArchiviert;
	}

	public String getErweitertZustaendigkeit() {
		return erweitertZustaendigkeit;
	}

	public void setErweitertZustaendigkeit(String erweitertZustaendigkeit) {
		this.erweitertZustaendigkeit = erweitertZustaendigkeit;
	}

	public Long getErweitertUnterstuetzerAb() {
		return erweitertUnterstuetzerAb;
	}

	public void setErweitertUnterstuetzerAb(Long erweitertUnterstuetzerAb) {
		this.erweitertUnterstuetzerAb = erweitertUnterstuetzerAb;
	}

	public EnumPrioritaet getErweitertPrioritaet() {
		return erweitertPrioritaet;
	}

	public void setErweitertPrioritaet(EnumPrioritaet erweitertPrioritaet) {
		this.erweitertPrioritaet = erweitertPrioritaet;
	}

	public String getErweitertDelegiertAn() {
		return erweitertDelegiertAn;
	}

	public void setErweitertDelegiertAn(String erweitertDelegiertAn) {
		this.erweitertDelegiertAn = erweitertDelegiertAn;
	}

	public Integer getErweitertStadtteilgrenze() {
		return erweitertStadtteilgrenze;
	}

	public void setErweitertStadtteilgrenze(Integer erweitertStadtteilgrenze) {
		this.erweitertStadtteilgrenze = erweitertStadtteilgrenze;
	}
}
