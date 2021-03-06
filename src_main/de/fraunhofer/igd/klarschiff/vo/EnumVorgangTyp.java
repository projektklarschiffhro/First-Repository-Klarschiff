package de.fraunhofer.igd.klarschiff.vo;


public enum EnumVorgangTyp implements EnumText {

    problem("Problem"),
    idee("Idee");
    
    private String text;
    
    private EnumVorgangTyp(String text) {
    	this.text = text;
    }
    
    public String getText() {
    	return text;
    }
}
