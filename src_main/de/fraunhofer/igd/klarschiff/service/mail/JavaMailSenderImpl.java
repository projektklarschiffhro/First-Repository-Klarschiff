package de.fraunhofer.igd.klarschiff.service.mail;

import java.util.Properties;

import org.apache.commons.lang.StringUtils;

public class JavaMailSenderImpl extends org.springframework.mail.javamail.JavaMailSenderImpl {

	@Override
	public void setUsername(String username) {
		if (!StringUtils.isBlank(username))
			super.setUsername(username);
	}

	@Override
	public void setPassword(String password) {
		if (!StringUtils.isBlank(password))
			super.setPassword(password);
	}

	public void setSmtpStarttlsEnable(boolean smtpStarttlsEnable) {
		if (smtpStarttlsEnable) {
			Properties prop = new Properties();
			prop.setProperty("mail.smtp.starttls.enable", "true");
			setJavaMailProperties(prop);
		}
	}
}
