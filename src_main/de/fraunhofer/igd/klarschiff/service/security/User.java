package de.fraunhofer.igd.klarschiff.service.security;

import java.util.List;

import de.fraunhofer.igd.klarschiff.context.AppContext;

public class User {
	String id;
//	String vorname;
//	String nachname;
	String name;
	String email;
	String dn;
	
	public List<Role> getZustaendigkeiten() {
		return AppContext.getApplicationContext().getBean(SecurityService.class).getZustaendigkeiten(id, true);
	}

	public List<Role> getDelegiertAn() {
		return AppContext.getApplicationContext().getBean(SecurityService.class).getDelegiertAn(id);
	}
	
	public boolean getUserExtern() {
		return AppContext.getApplicationContext().getBean(SecurityService.class).isUserExtern(id);
	}
	
	public boolean getUserIntern() {
		return AppContext.getApplicationContext().getBean(SecurityService.class).isUserIntern(id);
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
//	public String getVorname() {
//		return vorname;
//	}
//	public void setVorname(String vorname) {
//		this.vorname = vorname;
//	}
//	public String getNachname() {
//		return nachname;
//	}
//	public void setNachname(String nachname) {
//		this.nachname = nachname;
//	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getDn() {
		return dn;
	}

	public void setDn(String dn) {
		this.dn = dn;
	}
}
