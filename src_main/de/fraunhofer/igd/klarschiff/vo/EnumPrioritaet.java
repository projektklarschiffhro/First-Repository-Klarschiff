package de.fraunhofer.igd.klarschiff.vo;

public enum EnumPrioritaet implements EnumText {
	niedrig,
	mittel,
	hoch;
	
	@Override
	public String getText() {
		return name();
	}
}
