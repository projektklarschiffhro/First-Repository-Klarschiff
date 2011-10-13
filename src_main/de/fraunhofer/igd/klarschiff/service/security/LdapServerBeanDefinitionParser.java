package de.fraunhofer.igd.klarschiff.service.security;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import de.fraunhofer.igd.klarschiff.service.settings.PropertyPlaceholderConfigurer;

public class LdapServerBeanDefinitionParser extends org.springframework.security.config.ldap.LdapServerBeanDefinitionParser {

	@Override
	public BeanDefinition parse(Element elt, ParserContext parserContext) {
		try {
			if (StringUtils.isBlank(PropertyPlaceholderConfigurer.getPropertyValue("ldap.server.ldif"))) {
				//Connect to LDAP host
				elt.removeAttribute("ldif");
			} else {
				//Embedded LDAP
				elt.removeAttribute("port");
				elt.removeAttribute("url");
			}
			
			return super.parse(elt, parserContext);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
