package de.fraunhofer.igd.klarschiff.service.security;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang.StringUtils;
import org.springframework.ldap.core.DirContextAdapter;

public class ContextMapper<T> implements IContextMapper<T> {

	MultiValueMap mapping = new MultiValueMap();
	Class<T> clazz;
	String basePath;
	
	/**
	 * 
	 * @param mapping "name=cn,vorname=sn,..."
	 */
	public ContextMapper(Class<T> clazz, String mapping, String basePath) {
		this.clazz = clazz;
		this.basePath = basePath;
		for(String str : mapping.split(",")) {
			String[] pair = str.split("=");
			this.mapping.put(pair[1].trim(), pair[0].trim());
		}
	}
	
	@Override
	public T mapFromContext(Object ctx) {
		
		T t;
		try {
			t = clazz.newInstance();
		} catch (Exception e) {
			throw new RuntimeException();
		}
		
		DirContextAdapter dca = (DirContextAdapter)ctx;
		
		try {
			String dn = dca.getDn().toString();
			if (!StringUtils.isBlank(basePath) && !dn.contains(basePath)) dn += ","+basePath;
			PropertyUtils.setSimpleProperty(t, "dn", dn);
		} catch (Exception e) { }
		
		for (Object ldapKey : mapping.keySet())
		{
			String _ldapKey = (String)ldapKey;
			if (dca.getStringAttribute(_ldapKey) != null) {
				try {
					for(Object beanKey : mapping.getCollection(ldapKey))
						PropertyUtils.setSimpleProperty(t, (String)beanKey, dca.getStringAttribute(_ldapKey));
				} catch (Exception e) { }
			}
		}
		return t;
	}
}