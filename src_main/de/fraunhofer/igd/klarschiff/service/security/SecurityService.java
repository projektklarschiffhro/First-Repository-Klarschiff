package de.fraunhofer.igd.klarschiff.service.security;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;

@Service
public class SecurityService {
	static final Logger logger = Logger.getLogger(SecurityService.class);
	
	@Autowired
	VorgangDao vorgangDao;
	
	SecurityServiceLdap securityServiceLdap;
	
	String root;
	String userAttributesMapping;
	String roleAttributesMapping;
	String userSearchBase;
	String userObjectClass;
	String userSearchFilter;
	String groupSearchBase;
	String groupObjectClass;
	String groupSearchFilter;
	String groupObjectId;
	String groupRoleAttribute;
	String groupIntern = "intern";
	String groupExtern = "extern";
	String groupAdmin = "admin";
	String groupDispatcher = "dispatcher";
	
	private ContextMapper<User> userContextMapper;
	private ContextMapper<Role> roleContextMapper;
	private UserLoginContextMapper userLoginContextMapper;
	
	@PostConstruct
	public void init() {
		userContextMapper = new ContextMapper<User>(User.class, userAttributesMapping, root);
		roleContextMapper = new ContextMapper<Role>(Role.class, roleAttributesMapping, root);
		userLoginContextMapper = new UserLoginContextMapper(groupSearchFilter);
	}
	
	
	public boolean isCurrentUserAdmin() {
		for (GrantedAuthority authority : SecurityContextHolder.getContext().getAuthentication().getAuthorities())
			if (authority.getAuthority().equals("ROLE_ADMIN")) return true;
		return false;
	}
	
	public boolean isUserAdmin(String login) {
		User user = getUser(login);
		if (user==null) return false;
		List<Role> role = securityServiceLdap.getObjectListFromLdap(groupSearchBase, "(&(objectclass="+groupObjectClass+")("+StringUtils.replace(groupSearchFilter, "{0}", user.getDn())+")("+groupRoleAttribute+"="+groupAdmin+"))", roleContextMapper);
		return (role.size()==0) ? false : true;
	}
	
	public boolean isCurrentUserDispatcher() {
		for (GrantedAuthority authority : SecurityContextHolder.getContext().getAuthentication().getAuthorities())
			if (authority.getAuthority().equals("ROLE_DISPATCHER")) return true;
		return false;
	}
	
	public boolean isUserDispatcher(String login) {
		User user = getUser(login);
		if (user==null) return false;
		List<Role> role = securityServiceLdap.getObjectListFromLdap(groupSearchBase, "(&(objectclass="+groupObjectClass+")("+StringUtils.replace(groupSearchFilter, "{0}", user.getDn())+")("+groupRoleAttribute+"="+groupDispatcher+"))", roleContextMapper);
		return (role.size()==0) ? false : true;
	}
	
	public User getCurrentUser() {
		try {
			return getUser(SecurityContextHolder.getContext().getAuthentication().getName());
		} catch (Exception e) {
			return null;
		}
	}

	public User getUser(String login) {
		try {
			List<User> users = securityServiceLdap.getObjectListFromLdap(userSearchBase, "(&(objectclass="+userObjectClass+")("+StringUtils.replace(userSearchFilter, "{0}", login)+"))", userContextMapper);
			return users.get(0);
		} catch (Exception e) {
			return null;
		}
	}
	
	
	public List<User> getAllUser(){
		//alle UserLogins in den Rollen ermitteln
		List<List<String>> usersLoginList = securityServiceLdap.getObjectListFromLdap(groupSearchBase, "(objectclass="+groupObjectClass+")", userLoginContextMapper);
		//Set
		Set<String> userLoginSet = new HashSet<String>();
		for(List<String> list : usersLoginList)
			userLoginSet.addAll(list);
		//User ermitteln
		List<User> userList = new ArrayList<User>();
		for(Iterator<String> iter = userLoginSet.iterator(); iter.hasNext(); )
			userList.add(getUser(iter.next()));
		return userList;
	}
	
	
	public List<User> getAllUserForRole(String roleId){
		//alle UserLogins in den Rollen ermitteln
		List<List<String>> usersLoginList = securityServiceLdap.getObjectListFromLdap(groupSearchBase, "(&(objectclass="+groupObjectClass+")("+groupObjectId+"="+roleId+"))", userLoginContextMapper);
		//Set
		Set<String> userLoginSet = new HashSet<String>();
		for(List<String> list : usersLoginList)
			userLoginSet.addAll(list);
		//User ermitteln
		List<User> userList = new ArrayList<User>();
		for(Iterator<String> iter = userLoginSet.iterator(); iter.hasNext(); )
			userList.add(getUser(iter.next()));
		return userList;
	}

	public String[] getAllUserEmailsForRole(String roleId) {
		List<String> reciever = new ArrayList<String>();
		for (User user : getAllUserForRole(roleId))
			if (!StringUtils.isBlank(user.getEmail()))
				reciever.add(user.getEmail());
		return reciever.toArray(new String[0]);
	}
	
	
	public Role getDispatcherZustaendigkeit() {
		return getZustaendigkeit(groupDispatcher);
	}
	public String getDispatcherZustaendigkeitId() {
		return groupDispatcher;
	}

	public Role getZustaendigkeit(String id) {
		try {
			List <Role> roles = securityServiceLdap.getObjectListFromLdap(groupSearchBase, "(&(objectclass="+groupObjectClass+")("+groupObjectId+"="+id+"))", roleContextMapper);
			return roles.get(0);
		} catch (Exception e) {
			return null;
		}
	}

//	public List<Role> getZustaendigkeiten(String login) {
//		return getZustaendigkeiten(login, false);	
//	}
//	
//	public List<Role> getCurrentZustaendigkeiten() {
//		return getZustaendigkeiten(SecurityContextHolder.getContext().getAuthentication().getName(), false);
//	}

	public List<Role> getCurrentZustaendigkeiten(boolean inclDispatcher) {
		return getZustaendigkeiten(SecurityContextHolder.getContext().getAuthentication().getName(), inclDispatcher);
	}
	
	public List<Role> getZustaendigkeiten(String login, boolean inclDispatcher) {
		if (isUserAdmin(login)) {
			return getAllZustaendigkeiten(inclDispatcher);
		} else {
			User user = getUser(login);
			if (user==null) throw new RuntimeException();
			String dispatcherFilter = inclDispatcher ? "" : "(!("+groupObjectId+"="+groupDispatcher+"))";

			return securityServiceLdap.getObjectListFromLdap(groupSearchBase, "(&(objectclass="+groupObjectClass+")("+groupRoleAttribute+"="+groupIntern+")"+dispatcherFilter+"("+StringUtils.replace(groupSearchFilter, "{0}", user.getDn())+"))", roleContextMapper);
		}
	}

	
	public boolean isZustaendigForVorgang(String login, Vorgang vorgang) {
		for (Role zustaendigkeit : getZustaendigkeiten(login, true))
			if (StringUtils.equals(zustaendigkeit.getId(), vorgang.getZustaendigkeit())) return true;
		return false;
	}

	public boolean isCurrentZustaendigForVorgang(Vorgang vorgang) {
		return isZustaendigForVorgang(SecurityContextHolder.getContext().getAuthentication().getName(), vorgang);
	}
	
//	public List<Role> getAllZustaendigkeiten() {
//		return getAllZustaendigkeiten(false);
//	}

	public List<Role> getAllZustaendigkeiten(boolean inclDispatcher) {
		String dispatcherFilter = inclDispatcher ? "" : "(!("+groupObjectId+"="+groupDispatcher+"))";
		List <Role> allZustaendigkeiten = securityServiceLdap.getObjectListFromLdap(groupSearchBase, "(&(objectclass="+groupObjectClass+")("+groupRoleAttribute+"="+groupIntern+")"+dispatcherFilter+")", roleContextMapper);
		return allZustaendigkeiten;
	}

	
	public List<Role> getDelegiertAn(String login) {
		if (isUserAdmin(login)) return getAllDelegiertAn();
		User user = getUser(login);
		if (user==null) throw new RuntimeException();
		List <Role> delegiertAn = securityServiceLdap.getObjectListFromLdap(groupSearchBase, "(&(objectclass="+groupObjectClass+")("+groupRoleAttribute+"="+groupExtern+")("+StringUtils.replace(groupSearchFilter, "{0}", user.getDn())+"))", roleContextMapper);
		return delegiertAn;
	}

	public List<Role> getCurrentDelegiertAn() {
		return getDelegiertAn(SecurityContextHolder.getContext().getAuthentication().getName());
	}

	public List<Role> getAllDelegiertAn() {
		List <Role> allDelegiertAn = securityServiceLdap.getObjectListFromLdap(groupSearchBase, "(&(objectclass="+groupObjectClass+")("+groupRoleAttribute+"="+groupExtern+"))", roleContextMapper);
		return allDelegiertAn;
	}

	public boolean isCurrentUserExtern() {
		return isUserExtern(SecurityContextHolder.getContext().getAuthentication().getName());
	}
	
	public boolean isUserExtern(String login) {
		if (isUserAdmin(login)) return true;
		User user = getUser(login);
		if (user==null) return false;
		List <Role> role = securityServiceLdap.getObjectListFromLdap(groupSearchBase, "(&(objectclass="+groupObjectClass+")("+StringUtils.replace(groupSearchFilter, "{0}", user.getDn())+")("+groupRoleAttribute+"="+groupExtern+"))", roleContextMapper);
		return (role.size()==0) ? false : true;
	}
	
	public boolean isCurrentUserIntern() {
		return isUserIntern(SecurityContextHolder.getContext().getAuthentication().getName());
	}

	public boolean isUserIntern(String login) {
		if (isUserAdmin(login)) return true;
		User user = getUser(login);
		if (user==null) return false;
		List <Role> role = securityServiceLdap.getObjectListFromLdap(groupSearchBase, "(&(objectclass="+groupObjectClass+")("+StringUtils.replace(groupSearchFilter, "{0}", user.getDn())+")("+groupRoleAttribute+"="+groupIntern+"))", roleContextMapper);
		return (role.size()==0) ? false : true;
	}

	public boolean isCurrentZustaendigkeiten(Long vorgangId) {
		//Zuständigkeit für den Vorgang ermitteln
		String zustaendigkeit = vorgangDao.getZustaendigkeitForVorgang(vorgangId);
		if (StringUtils.isBlank(zustaendigkeit)) {
			return isCurrentUserAdmin();
		} else {
			for (Role role : getCurrentZustaendigkeiten(true))
				if (StringUtils.equals(role.getId(), zustaendigkeit)) return true;
		}
		return false;
	}


	public boolean isCurrentDelegiertAn(Long vorgangId) {
		//DelegiertAn für den Vorgang ermitteln
		String delegiertAn = vorgangDao.getDelegiertAnForVorgang(vorgangId);
		if (StringUtils.isBlank(delegiertAn)) {
			return isCurrentUserAdmin();
		} else {
			for (Role role : getCurrentDelegiertAn())
				if (StringUtils.equals(role.getId(), delegiertAn)) return true;
		}
		return false;
	}	
	
	/* --------------- MD5 Hash erzeugen ----------------------------*/

	public String createHash(String str) {
		try {
			 MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			 messageDigest.update(str.getBytes(), 0, str.length());
			 return new BigInteger(1, messageDigest.digest()).toString(32);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
//	/* --------------- Hilfsfunktionen zum Zugriff auf das LDAP ----------------------------*/
//
//	/**
//	 * 
//	 * @param <T>
//	 * @param path
//	 * @param filter "(&(objectclass=inetOrgPerson)(uid=audersch))"
//	 * @param mapping "name=cn,vorname=sn,..."
//	 * @param resultClass
//	 * @return
//	 */
//	@SuppressWarnings("unused")
//	private <T> List<T>  getObjectListFromLdap(String path, String filter, Class<T> resultClass, String mapping) {
//		return securityServiceLdap.getObjectListFromLdap(path, filter, new AttributesMapper<T>(resultClass, mapping));
//	}
	
	/* --------------- GET + SET ----------------------------*/

	public String getUserAttributesMapping() {
		return userAttributesMapping;
	}

	public void setUserAttributesMapping(String userAttributesMapping) {
		this.userAttributesMapping = userAttributesMapping;
	}

	public String getRoleAttributesMapping() {
		return roleAttributesMapping;
	}

	public void setRoleAttributesMapping(String roleAttributesMapping) {
		this.roleAttributesMapping = roleAttributesMapping;
	}

	public String getUserSearchBase() {
		return userSearchBase;
	}

	public void setUserSearchBase(String userSearchBase) {
		this.userSearchBase = userSearchBase;
	}

	public String getUserObjectClass() {
		return userObjectClass;
	}

	public void setUserObjectClass(String userObjectClass) {
		this.userObjectClass = userObjectClass;
	}

	public String getUserSearchFilter() {
		return userSearchFilter;
	}

	public void setUserSearchFilter(String userSearchFilter) {
		this.userSearchFilter = userSearchFilter;
	}

	public String getGroupSearchBase() {
		return groupSearchBase;
	}

	public void setGroupSearchBase(String groupSearchBase) {
		this.groupSearchBase = groupSearchBase;
	}

	public String getGroupObjectClass() {
		return groupObjectClass;
	}

	public void setGroupObjectClass(String groupObjectClass) {
		this.groupObjectClass = groupObjectClass;
	}

	public String getGroupSearchFilter() {
		return groupSearchFilter;
	}

	public void setGroupSearchFilter(String groupSearchFilter) {
		this.groupSearchFilter = groupSearchFilter;
	}

	public String getGroupRoleAttribute() {
		return groupRoleAttribute;
	}

	public void setGroupRoleAttribute(String groupRoleAttribute) {
		this.groupRoleAttribute = groupRoleAttribute;
	}

	public String getGroupIntern() {
		return groupIntern;
	}

	public void setGroupIntern(String groupIntern) {
		this.groupIntern = groupIntern;
	}

	public String getGroupExtern() {
		return groupExtern;
	}

	public void setGroupExtern(String groupExtern) {
		this.groupExtern = groupExtern;
	}


	public String getGroupObjectId() {
		return groupObjectId;
	}


	public void setGroupObjectId(String groupObjectId) {
		this.groupObjectId = groupObjectId;
	}


	public String getGroupAdmin() {
		return groupAdmin;
	}


	public void setGroupAdmin(String groupAdmin) {
		this.groupAdmin = groupAdmin;
	}


	public SecurityServiceLdap getSecurityServiceLdap() {
		return securityServiceLdap;
	}


	public void setSecurityServiceLdap(SecurityServiceLdap securityServiceLdap) {
		this.securityServiceLdap = securityServiceLdap;
	}

	public String getRoot() {
		return root;
	}


	public void setRoot(String root) {
		this.root = root;
	}
}
