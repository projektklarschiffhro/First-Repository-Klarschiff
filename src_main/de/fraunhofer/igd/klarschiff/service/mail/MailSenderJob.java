package de.fraunhofer.igd.klarschiff.service.mail;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.mail.SimpleMailMessage;

import de.fraunhofer.igd.klarschiff.context.AppContext;
import de.fraunhofer.igd.klarschiff.dao.VerlaufDao;
import de.fraunhofer.igd.klarschiff.vo.EnumVerlaufTyp;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;

public class MailSenderJob implements Runnable {
	public enum SaveVerlaufOn { ever, error, none };
	
	Logger logger = Logger.getLogger(MailSenderJob.class);
	MailService mailService;
	Object msg;

	EnumVerlaufTyp verlaufTyp;
	SaveVerlaufOn saveVerlaufOn;
	Vorgang vorgang;

	public MailSenderJob(MailService mailService, MimeMessage msg, EnumVerlaufTyp verlaufTyp, SaveVerlaufOn saveVerlaufOn, Vorgang vorgang) {
		this.mailService = mailService;
		this.msg = msg;
		this.verlaufTyp = verlaufTyp;
		this.saveVerlaufOn = saveVerlaufOn;
		this.vorgang = vorgang;
	}
	public MailSenderJob(MailService mailService, SimpleMailMessage msg, EnumVerlaufTyp verlaufTyp, SaveVerlaufOn saveVerlaufOn, Vorgang vorgang) {
		this.mailService = mailService;
		this.msg = msg;
		this.verlaufTyp = verlaufTyp;
		this.saveVerlaufOn = saveVerlaufOn;
		this.vorgang = vorgang;
	}
	
	@SuppressWarnings("unused")
	private static VerlaufDao getVerlaufDao() {
		return (VerlaufDao)AppContext.getApplicationContext().getBean("verlaufDao");
	}
	
	@Override
	public void run() {
		try {
			if (msg instanceof SimpleMailMessage) { 
				SimpleMailMessage _msg = (SimpleMailMessage)msg;
				logger.debug("Send Mail: "+_msg.getSubject());
				if (!StringUtils.isBlank(mailService.getSendAllMailsTo())) _msg.setTo(mailService.getSendAllMailsTo());
				mailService.getMailSender().send(_msg);
			} else if (msg instanceof MimeMessage) {
				MimeMessage _msg = (MimeMessage)msg;
				logger.debug("Send Mail: "+_msg.getSubject());
				if (!StringUtils.isBlank(mailService.getSendAllMailsTo())) _msg.setRecipient(RecipientType.TO, new InternetAddress(mailService.getSendAllMailsTo()));
				mailService.getMailSender().send(_msg);
			} else throw new Exception();
//			if (saveVerlaufOn==SaveVerlaufOn.ever) {
//				getVerlaufDao().saveVerlaufForVorgang(vorgang, verlaufTyp, null, null);
//			}
		} catch (Exception e) {
			logger.fatal(e);
//			if (saveVerlaufOn==SaveVerlaufOn.ever || saveVerlaufOn==SaveVerlaufOn.error) {
//				getVerlaufDao().saveVerlaufForVorgang(vorgang, verlaufTyp, null, "ERROR: "+e.getMessage());
//			}
		}
	}
}
