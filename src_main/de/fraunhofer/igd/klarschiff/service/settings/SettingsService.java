package de.fraunhofer.igd.klarschiff.service.settings;

import org.springframework.stereotype.Service;

@Service
public class SettingsService {

	String version;
	Long vorgangIdeeUnterstuetzer;
	String proxyHost;
	String proxyPort;
	boolean showLogins;
	boolean showFehlerDetails;
	String bugTrackingUrl;
	
	public String getProfile() {
		return PropertyPlaceholderConfigurer.getProfile();
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Long getVorgangIdeeUnterstuetzer() {
		return vorgangIdeeUnterstuetzer;
	}

	public void setVorgangIdeeUnterstuetzer(Long vorgangIdeeUnterstuetzer) {
		this.vorgangIdeeUnterstuetzer = vorgangIdeeUnterstuetzer;
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public String getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(String proxyPort) {
		this.proxyPort = proxyPort;
	}

	public boolean getShowLogins() {
		return showLogins;
	}

	public void setShowLogins(boolean showLogins) {
		this.showLogins = showLogins;
	}

	public boolean getShowFehlerDetails() {
		return showFehlerDetails;
	}

	public void setShowFehlerDetails(boolean showFehlerDetails) {
		this.showFehlerDetails = showFehlerDetails;
	}

	public String getBugTrackingUrl() {
		return bugTrackingUrl;
	}

	public void setBugTrackingUrl(String bugTrackingUrl) {
		this.bugTrackingUrl = bugTrackingUrl;
	}



}
