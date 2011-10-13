package de.fraunhofer.igd.klarschiff.service.security;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;

public class InterceptRequestPathFilter implements Filter{

    private static final String FILTER_APPLIED = "__spring_security_interceptRequestPathFilter_filterApplied";

    public enum Role {zusteandigkeit, delegiertAn};
	
	Map<String, Role> pattern;
	Map<Pattern , Role> patternRoleMap;
	
	@Autowired
	SecurityService securityService;
	
	@PostConstruct
	public void init() {
		patternRoleMap = new HashMap<Pattern, Role>();
		for(String p : pattern.keySet()) {
			patternRoleMap.put(Pattern.compile(p), pattern.get(p)); 
		}
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		//request casten
		HttpServletRequest request = (HttpServletRequest)servletRequest;
		//Pattern durchgehen und nach einem passenden suchen
		for(Pattern regexPattern : patternRoleMap.keySet()) {
			Matcher regexMatcher = regexPattern.matcher(request.getRequestURI());
			if (regexMatcher.find()) {
				//passendes Pattern wurde gefunden
				Long vorgangId = Long.parseLong(regexMatcher.group(1));
				Role role = patternRoleMap.get(regexPattern);
				//war der Zugriff für den Vorgang und die Role bereits mal in der Session erlaubt?
				if (isFilterApplied(request, vorgangId, role)) {
					//Zugriff wieder erlauben
					chain.doFilter(servletRequest, response);
					return;
				}
				//Zugriff für Vorgang und Role prüfen
				boolean accessAllowed = false;
				switch (role) {
					case zusteandigkeit:
						accessAllowed = securityService.isCurrentZustaendigkeiten(vorgangId);
						break;
					case delegiertAn:
						accessAllowed = securityService.isCurrentDelegiertAn(vorgangId);
						break;
				}
				if (!accessAllowed) {
					//Zugriff ist nicht erlaubt
					throw new AccessDeniedException("Der Zugriff auf den Vorgang ist nicht erlaubt.");
				} else {
					//Zugriff erlaubt
					//erlaubten Zugriff für den Vorgang und die Role in der Session ablegen
					setFilterApplied(request, vorgangId, role);
					chain.doFilter(servletRequest, response);
					return;
				}
			}
		}
		//kein passendes Pattern gefunden -> Zugriff erlaubt 
		chain.doFilter(servletRequest, response);
	}
	
	@SuppressWarnings("unchecked")
	private boolean isFilterApplied(HttpServletRequest request, Long vorgangId, Role role) {
		try {
			return ((Set<String>)request.getSession().getAttribute(FILTER_APPLIED)).contains(role.name()+vorgangId);
		} catch (Exception e) {}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	private void setFilterApplied(HttpServletRequest request, Long vorgangId, Role role) {
		Set<String> filterAppliedSet = (Set<String>)request.getSession().getAttribute(FILTER_APPLIED);
		if (filterAppliedSet==null) {
			filterAppliedSet = new HashSet<String>();
			request.getSession().setAttribute(FILTER_APPLIED, filterAppliedSet);
		}
		filterAppliedSet.add(role.name()+vorgangId);
	}
	
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void destroy() {
	}

	public Map<String, Role> getPattern() {
		return pattern;
	}

	public void setPattern(Map<String, Role> pattern) {
		this.pattern = pattern;
	}
}
