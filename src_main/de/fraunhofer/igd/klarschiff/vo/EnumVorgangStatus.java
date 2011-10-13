package de.fraunhofer.igd.klarschiff.vo;

import org.apache.commons.lang.StringEscapeUtils;


public enum EnumVorgangStatus implements EnumText {
    gemeldet("gemeldet"),				//Ersteller hat seine Email noch nicht bestätigt
    offen("offen"),					//ErstellerEmail wurde bestätigt
    inBearbeitung("in Bearbeitung"),			//wenn das erste mal die Zuständigkeit durch einen Sachbearbeiter akzeptiert wurde
    wirdNichtBearbeitet("wird nicht bearbeitet"),
    duplikat("Duplikat"),
    abgeschlossen("abgeschlossen"),
    geloescht("gel&#246;scht");

    public static EnumVorgangStatus[] openVorgangStatus() {
    	return new EnumVorgangStatus[] {gemeldet, offen, inBearbeitung};
    }
    public static EnumVorgangStatus[] closedVorgangStatus() {
    	return new EnumVorgangStatus[] {wirdNichtBearbeitet, duplikat, abgeschlossen, geloescht};
    }
    
    private String text;
    
    private EnumVorgangStatus(String text) {
    	this.text = text;
    }
    
    public String getText() {
    	return StringEscapeUtils.unescapeHtml(text);
    }
    
    public String getTextEncoded() {
    	return text;
    }
}
