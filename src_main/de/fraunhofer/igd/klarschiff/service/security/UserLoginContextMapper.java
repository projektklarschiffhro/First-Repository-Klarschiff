package de.fraunhofer.igd.klarschiff.service.security;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.directory.Attribute;

import org.apache.log4j.Logger;
import org.springframework.ldap.core.DirContextAdapter;

public class UserLoginContextMapper implements IContextMapper<List<String>> {
	private static final Logger logger = Logger.getLogger(UserLoginContextMapper.class);
	
	String userAttribut;	
	Pattern pattern;
	
	public UserLoginContextMapper(String groupSearchFilter) {
		userAttribut = groupSearchFilter.substring(0, groupSearchFilter.indexOf("="));
		pattern = Pattern.compile("=([\\w\\W]*?),[\\w\\W]*");
	}
	
	@Override
	public List<String> mapFromContext(Object ctx) {
		DirContextAdapter dca = (DirContextAdapter)ctx;
		List<String> userLoginList = new ArrayList<String>();
		Attribute attr = dca.getAttributes().get(userAttribut);
		if (attr==null) return userLoginList;
		for(int i=0; i<attr.size(); i++)
		{
			try {
				Matcher matcher = pattern.matcher((String)attr.get(i));
				if (matcher.find())
					userLoginList.add(matcher.group(1));
			} catch (Exception e) {
				logger.error("Userlogin kann nicht ermittelt werden", e);
			}
		}
		return userLoginList;
	}
}