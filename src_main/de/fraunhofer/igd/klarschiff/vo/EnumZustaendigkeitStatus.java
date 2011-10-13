package de.fraunhofer.igd.klarschiff.vo;

public enum EnumZustaendigkeitStatus implements EnumText {
	zugewiesen,
	akzeptiert;

	@Override
	public String getText() {
		return name();
	}

}
