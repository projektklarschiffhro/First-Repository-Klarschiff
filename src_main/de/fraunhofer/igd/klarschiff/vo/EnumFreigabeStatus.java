package de.fraunhofer.igd.klarschiff.vo;



public enum EnumFreigabeStatus implements EnumText {

    intern, 
    extern,
    geloescht;
        
	@Override
	public String getText() {
		return name();
	}
}
