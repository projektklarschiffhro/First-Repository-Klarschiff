package de.fraunhofer.igd.klarschiff.service.settings;

import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.core.io.DefaultResourceLoader;

public class PropertyPlaceholderConfigurer extends org.springframework.beans.factory.config.PropertyPlaceholderConfigurer {

	private static final Logger logger = Logger.getLogger(PropertyPlaceholderConfigurer.class); 
	
	private static final String PROFILE_SYSTEM_PROPERTY_NAME = "KLARSCHIFF_HRO_PROFILE";
	private static final String PROFILE_SETTINGS_FILE_PROPERTY_NAME = "profile";
	private static final String SETTINGS_FILE_LOCATION = "classpath:settings.properties";
	
	private static String profile;
	
	public PropertyPlaceholderConfigurer() {
		super();
		setLocation(new DefaultResourceLoader().getResource(SETTINGS_FILE_LOCATION));
	}

	@Override
	protected String resolvePlaceholder(String placeholder, Properties props) {
		String profile = getProfile();
		String result = super.resolvePlaceholder(profile+"."+placeholder, props);
		return (result!=null) ? result : super.resolvePlaceholder(placeholder, props);
	}

	protected static String getProfile() {
		if (profile!=null) return profile;
		try {
	        String _profile = System.getenv(PROFILE_SYSTEM_PROPERTY_NAME);
	        if (StringUtils.isBlank(_profile)) logger.info("Profil wurde mit System.getenv('"+PROFILE_SYSTEM_PROPERTY_NAME+"') gefunden.");
	        
			if (StringUtils.isBlank(_profile)) {
				_profile = System.getProperty(PROFILE_SYSTEM_PROPERTY_NAME);
				if (!StringUtils.isBlank(_profile)) logger.info("Profil wurde mit System.getProperty('"+PROFILE_SYSTEM_PROPERTY_NAME+"') gefunden.");
			}
			
			if (StringUtils.isBlank(_profile)) {
				Properties prop = new Properties();
				prop.load(new DefaultResourceLoader().getResource(SETTINGS_FILE_LOCATION).getInputStream());
				_profile = prop.getProperty(PROFILE_SETTINGS_FILE_PROPERTY_NAME);
				if (!StringUtils.isBlank(_profile)) logger.info("Profil wurde in "+SETTINGS_FILE_LOCATION+" gefunden.");
			} 
				
			if (StringUtils.isBlank(_profile)) throw new NullPointerException("profile is null");
				
			profile = _profile;
			logger.info("Verwendetes Profil: "+profile);
			return _profile;
		} catch (Exception e) {
			throw new RuntimeException("Profile kann nicht ermittelt werden. Das Profile kann über das SystemProperty "+PROFILE_SYSTEM_PROPERTY_NAME+
					" oder über die Variable "+PROFILE_SETTINGS_FILE_PROPERTY_NAME+" in der Datei "+SETTINGS_FILE_LOCATION+ " festgelegt werden.", e);
		}
	}

	public static String getPropertyValue(String name) {
		try {
			String profile = getProfile(); 
			Properties prop = new Properties();
			prop.load(new DefaultResourceLoader().getResource(SETTINGS_FILE_LOCATION).getInputStream());
			String result = prop.getProperty(profile+"."+name);
			return (result!=null) ? result : prop.getProperty(name);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
