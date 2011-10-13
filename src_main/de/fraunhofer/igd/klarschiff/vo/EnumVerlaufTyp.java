package de.fraunhofer.igd.klarschiff.vo;


public enum EnumVerlaufTyp implements EnumText{

    erzeugt, 
    betreff, 
    betreffFreigabeStatus, 
    detailsFreigabeStatus, 
    detail, 
    fotoFreigabeStatus, 
    foto, 
    typ,
    kategorie,
    status,
    statusKommentar,
    archiv,
    zustaendigkeitAkzeptiert,
    zustaendigkeit,
    delegiertAn,
    kommentar,
    prioritaet,
    missbrauchsmeldungErzeugt,
    missbrauchsmeldungBearbeitet,
    vorgangBestaetigungEmail,
    vorgangUnterstuetzerEmail,
    missbrauchsmeldungEmail,
    weiterleitenEmail,
    vorgangBestaetigung,
    vorgangUnterstuetzerBestaetigung,
    vorgangMissbrauchsmeldungBestaetigung;

	@Override
	public String getText() {
		return name();
	}
}
