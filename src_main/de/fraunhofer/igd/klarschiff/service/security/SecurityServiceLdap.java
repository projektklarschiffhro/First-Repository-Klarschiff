package de.fraunhofer.igd.klarschiff.service.security;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Service;

import com.googlecode.ehcache.annotations.Cacheable;
import com.googlecode.ehcache.annotations.KeyGenerator;
import com.googlecode.ehcache.annotations.Property;

@Service
public class SecurityServiceLdap {
	static final Logger logger = Logger.getLogger(SecurityServiceLdap.class);

	LdapTemplate ldapTemplate;
	
	@SuppressWarnings("unchecked")
	@Cacheable(cacheName="ldapCache", 
		keyGenerator = @KeyGenerator(name = "ListCacheKeyGenerator",
			properties = {
				@Property( name="useReflection", value="true"), 
				@Property( name="checkforCycles", value="true" ),
				@Property( name="includeMethod", value="true" )
			}
		)
	)
	public <T> List<T>  getObjectListFromLdap(String path, String filter, IContextMapper<T> contextMapper) {
		try {
			logger.debug("LdapSearch: path:"+path+" filter:"+filter);
			if (path==null) return ldapTemplate.search(DistinguishedName.EMPTY_PATH, filter, contextMapper);
			else return ldapTemplate.search(path, filter, contextMapper);
		} catch (RuntimeException e) {
			logger.error(e);
			e.printStackTrace();
			throw e;
		}
	}

	public LdapTemplate getLdapTemplate() {
		return ldapTemplate;
	}


	public void setLdapTemplate(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}

}
