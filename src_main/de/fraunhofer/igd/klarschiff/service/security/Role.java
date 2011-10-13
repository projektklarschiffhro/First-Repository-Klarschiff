package de.fraunhofer.igd.klarschiff.service.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class Role {
	String id;
//	String name;
	String description;
	String dn;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
//	public String getName() {
//		return name;
//	}
//	public void setName(String name) {
//		this.name = name;
//	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public static List<String> toString(Collection<Role> roles) {
		List<String> _roles = new ArrayList<String>();
		for (Role role : roles) _roles.add(role.getId());
		return _roles;
		
	}
	
	@Override
	public boolean equals(Object o) {
		if (o!=null && o instanceof Role)
			return StringUtils.equals(id, ((Role)o).getId());
		else return super.equals(o);
	}
	public String getDn() {
		return dn;
	}
	public void setDn(String dn) {
		this.dn = dn;
	}
}
