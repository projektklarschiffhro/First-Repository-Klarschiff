package de.fraunhofer.igd.klarschiff.tld;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import de.fraunhofer.igd.klarschiff.context.AppContext;
import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.service.cluster.ClusterUtil;
import de.fraunhofer.igd.klarschiff.service.security.Role;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.service.security.User;
import de.fraunhofer.igd.klarschiff.service.settings.SettingsService;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangStatus;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;
import de.fraunhofer.igd.klarschiff.web.FehlerController;


public class CustomFunctions {
	public static final Logger logger = Logger.getLogger(CustomFunctions.class); 
	
	
	public static boolean roleContains(Collection<Role> collection, Role o) {
		return collection.contains(o);
	}
	
	public static Integer roleSize(Collection<Role> collection) {
	    return collection.size();
	}
	
	public static List<Role> roleMinus(Collection<Role> collection, Role o) {
		ArrayList<Role> c = new ArrayList<Role>();
		c.addAll(collection);
    	c.remove(o);
	    return c;
	}
	public static List<Role> roleMinus(Collection<Role> collection, Collection<Role> o) {
		ArrayList<Role> c = new ArrayList<Role>();
		c.addAll(collection);
		c.removeAll(o);
		return c;
	}
	public static Role role(String id) {
		return AppContext.getApplicationContext().getBean(SecurityService.class).getZustaendigkeit(id);
	}

	public static Object bean(String name) {
		return AppContext.getApplicationContext().getBean(name);
	}

	public static String version() {
		return AppContext.getApplicationContext().getBean(SettingsService.class).getVersion();
	}
	public static boolean showLogins() {
		return AppContext.getApplicationContext().getBean(SettingsService.class).getShowLogins();
	}
	
	public static String vorgangStatus(String status) {
		return EnumVorgangStatus.valueOf(status).getText();
	}
	
	public static boolean isOpenMissbrauchsmeldung(Vorgang vorgang) {
		VorgangDao vorgangDao = AppContext.getApplicationContext().getBean(VorgangDao.class);
		if (vorgangDao.countOpenMissbrauchsmeldungByVorgang(vorgang)==0) return false;
		else return true;
	}
	
	public static long countMissbrauchsmeldungen(Vorgang vorgang){
		if(isOpenMissbrauchsmeldung(vorgang)){
			VorgangDao vorgangDao = AppContext.getApplicationContext().getBean(VorgangDao.class);
			return vorgangDao.countOpenMissbrauchsmeldungByVorgang(vorgang);
		} else return 0;
	}

	public static User getCurrentUser() {
		return ((SecurityService)AppContext.getApplicationContext().getBean("securityService")).getCurrentUser();
	}
	
	public static boolean isCurrentZustaendigForVorgang(Vorgang vorgang) {
		return ((SecurityService)AppContext.getApplicationContext().getBean("securityService")).isCurrentZustaendigForVorgang(vorgang);
	}
	
	public static Map<String, Object> processException(Throwable exception) {
		return FehlerController.processException(exception);
	}
	public static String connector() {
		return ClusterUtil.getServerName()+":"+ClusterUtil.getServerConnectorPort();
	}
	
}
