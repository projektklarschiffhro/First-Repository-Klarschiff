package de.fraunhofer.igd.klarschiff.vo;



public enum EnumNaehereBeschreibungNotwendig implements EnumText {

    keine, 
    betreff,
    details,
    betreffUndDetails;
        
	@Override
	public String getText() {
		return name();
	}
}
