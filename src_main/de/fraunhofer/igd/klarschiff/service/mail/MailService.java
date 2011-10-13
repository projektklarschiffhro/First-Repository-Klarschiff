package de.fraunhofer.igd.klarschiff.service.mail;

import java.text.SimpleDateFormat;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import de.fraunhofer.igd.klarschiff.dao.KommentarDao;
import de.fraunhofer.igd.klarschiff.dao.VerlaufDao;
import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.service.geo.GeoService;
import de.fraunhofer.igd.klarschiff.service.job.JobExecutorService;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.vo.EnumVerlaufTyp;
import de.fraunhofer.igd.klarschiff.vo.Kommentar;
import de.fraunhofer.igd.klarschiff.vo.Missbrauchsmeldung;
import de.fraunhofer.igd.klarschiff.vo.Unterstuetzer;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;

public class MailService {

	@Autowired
	ServletContext servletContext;

	@Autowired
	JobExecutorService jobExecutorService;
	
	@Autowired
	SecurityService securityService;
	
	@Autowired
	GeoService geoService;
	
	@Autowired
	KommentarDao kommentarDao;
	
	@Autowired
	VorgangDao vorgangDao;
	
	@Autowired
	VerlaufDao verlaufDao;

	JavaMailSender mailSender;
	String serverBaseUrlBackend;
	String serverBaseUrlFrontend;
	
	String sendAllMailsTo;

	SimpleMailMessage vorgangBestaetigungMailTemplate;
	SimpleMailMessage unterstuetzungBestaetigungMailTemplate;
	SimpleMailMessage missbrauchsmeldungBestaetigungMailTemplate;
	SimpleMailMessage vorgangWeiterleitenMailTemplate;
	SimpleMailMessage informDispatcherMailTemplate;
	SimpleMailMessage informExternMailTemplate;
	SimpleMailMessage informErstellerMailTemplate;

	private SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
	
	public void sendVorgangBestaetigungMail(Vorgang vorgang) {
		SimpleMailMessage msg = new SimpleMailMessage(vorgangBestaetigungMailTemplate);
		msg.setTo(vorgang.getAutorEmail());
		String mailText = msg.getText();
		mailText = mailText.replaceAll("%baseUrlFrontend%", getServerBaseUrlFrontend());
		mailText = mailText.replaceAll("%hash%", vorgang.getHash());
		msg.setText(mailText);
		jobExecutorService.runJob(new MailSenderJob(this, msg, EnumVerlaufTyp.vorgangBestaetigungEmail, MailSenderJob.SaveVerlaufOn.ever, vorgang));
	}
	public void sendUnterstuetzerBestaetigungMail(Unterstuetzer unterstuetzer, String email) {
		SimpleMailMessage msg = new SimpleMailMessage(unterstuetzungBestaetigungMailTemplate);
		msg.setTo(email);
		String mailText = msg.getText();
		mailText = mailText.replaceAll("%baseUrlFrontend%", getServerBaseUrlFrontend());
		mailText = mailText.replaceAll("%hash%", unterstuetzer.getHash());
		msg.setText(mailText);
		jobExecutorService.runJob(new MailSenderJob(this, msg, EnumVerlaufTyp.vorgangUnterstuetzerEmail, MailSenderJob.SaveVerlaufOn.error, unterstuetzer.getVorgang()));
	}

	public void sendMissbrauchsmeldungBestaetigungMail(Missbrauchsmeldung missbrauchsmeldung, String email) {
		SimpleMailMessage msg = new SimpleMailMessage(missbrauchsmeldungBestaetigungMailTemplate);
		msg.setTo(email);
		String mailText = msg.getText();
		mailText = mailText.replaceAll("%baseUrlFrontend%", getServerBaseUrlFrontend());
		mailText = mailText.replaceAll("%hash%", missbrauchsmeldung.getHash());
		msg.setText(mailText);
		jobExecutorService.runJob(new MailSenderJob(this, msg, EnumVerlaufTyp.missbrauchsmeldungEmail, MailSenderJob.SaveVerlaufOn.error, missbrauchsmeldung.getVorgang()));
	}
		
	public void sendVorgangWeiterleitenMail(Vorgang vorgang, String fromEmail, String toEmail, String text, boolean sendKarte, boolean sendKommentare, boolean sendFoto, boolean sendMissbrauchsmeldungen) throws RuntimeException
	{
		try {
			MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mailSender.createMimeMessage(), true);
			mimeMessageHelper.setSubject(vorgangWeiterleitenMailTemplate.getSubject());
			mimeMessageHelper.setFrom(StringUtils.isBlank(sendAllMailsTo) ? fromEmail : sendAllMailsTo);
			mimeMessageHelper.setTo(toEmail);

			String mailText = composeVorgangWeiterleitenMail(vorgang, text, sendKarte, sendKommentare, sendMissbrauchsmeldungen);

			mimeMessageHelper.setText(mailText);
			
			if (sendFoto && vorgang.getFotoNormalJpg()!=null) 
				mimeMessageHelper.addAttachment("foto.jpg", new ByteArrayResource(vorgang.getFotoNormalJpg()), "image/jpg");
				
			jobExecutorService.runJob(new MailSenderJob(this, mimeMessageHelper.getMimeMessage(), EnumVerlaufTyp.weiterleitenEmail, MailSenderJob.SaveVerlaufOn.ever, vorgang));
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	
	}

	public String composeVorgangWeiterleitenMail(Vorgang vorgang, String text, boolean sendKarte, boolean sendKommentare, boolean sendMissbrauchsmeldungen) throws RuntimeException
	{
		
		try {
			String mailText = vorgangWeiterleitenMailTemplate.getText();
			mailText = mailText.replaceAll("%absender%", securityService.getCurrentUser().getName());
			mailText = mailText.replaceAll("%text%", text);
			StringBuilder str = new StringBuilder();
			str.append("Typ: " );
			str.append(vorgang.getTyp().getText());
			str.append("\n");

			str.append("ID: " );
			str.append(vorgang.getId());
			str.append("\n");			
			
			str.append("Hauptkategorie: " );
			str.append(vorgang.getKategorie().getParent().getName());
			str.append("\n");
			
			str.append("Kategorie: " );
			str.append(vorgang.getKategorie().getName());
			str.append("\n");
			
			if (vorgang.getBetreff().isEmpty() == false) {
				str.append("Betreff: " );
				str.append(vorgang.getBetreff());
				str.append("\n");
				str.append("Betreff Freigabestatus: " );
				str.append(vorgang.getBetreffFreigabeStatus().toString());
				str.append("\n");
			}
			
			if (vorgang.getDetails().isEmpty() == false) {
				str.append("Details: " );
				str.append(vorgang.getDetails());
				str.append("\n");
				str.append("Details Freigabestatus: " );
				str.append(vorgang.getDetailsFreigabeStatus().toString());
				str.append("\n");
			}
						
			str.append("Erstellung: " );
			str.append(formatter.format(vorgang.getDatum()));
			str.append("\n");
			
			if (vorgang.getAutorEmail().isEmpty() == false) {
				str.append("Autor: " );
				str.append(vorgang.getAutorEmail());
				str.append("\n");
			}
			
			str.append("Status: " );
			str.append(vorgang.getStatus().getText());
			str.append("\n");
			
			if (vorgang.getStatusKommentar() != null) {
				str.append("Statuskommentar: " );
				str.append(vorgang.getStatusKommentar());
				str.append("\n");
			}
			
			str.append("Zust\u00e4ndigkeit: " ); 
			str.append(vorgang.getZustaendigkeit());
			str.append(" (");
			str.append(vorgang.getZustaendigkeitStatus());
			str.append(")");
			str.append("\n");

			if (vorgang.getDelegiertAn() != null) {
				str.append("Delegiert an: " );
				str.append(vorgang.getDelegiertAn());
				str.append("\n");
			}

			if ( sendKarte==true ) {
				str.append("\nKarte\n----------------------\n");
				str.append("Aufruf von extern: "+geoService.getMapExternExternUrl(vorgang)+"\n\n");
				str.append("Aufruf von intern: "+geoService.getMapExternUrl(vorgang)+"\n");
			}
			
			if ( sendKommentare == true) {
				str.append("\nKommentare\n----------------------\n");
				for (Kommentar kommentar : kommentarDao.findKommentareForVorgang(vorgang)) {
					str.append("- " + kommentar.getNutzer() + " " + formatter.format(kommentar.getDatum()) +" -\n" );
					str.append(kommentar.getText());
					str.append("\n\n");
				}
			}
				
			if ( sendMissbrauchsmeldungen == true) {	
				str.append("\nMissbrauchsmeldungen\n----------------------\n");
				for (Missbrauchsmeldung missbrauchsmeldung : vorgangDao.listMissbrauchsmeldung(vorgang)) {
					str.append("- " + formatter.format(missbrauchsmeldung.getDatum()) +" -\n" );
					str.append(missbrauchsmeldung.getText());
					str.append("\n\n");
				}
			}
			
			mailText = mailText.replaceAll("%vorgang%", str.toString());
			
			return mailText;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	
	}
	public void sendInformDispatcherMail(List<Vorgang> newVorgaenge, String[] to) {
		if (CollectionUtils.isEmpty(newVorgaenge) || ArrayUtils.isEmpty(to)) return;
		
		SimpleMailMessage msg = new SimpleMailMessage(informDispatcherMailTemplate);
		msg.setTo(to);
		StringBuilder str = new StringBuilder();
		for(Vorgang vorgang : newVorgaenge) {
			str.append("ID            : "+vorgang.getId()+"\n");
			str.append("Hauptkategorie: "+vorgang.getKategorie().getParent().getName()+"\n");
			str.append("Kategorie     : "+vorgang.getKategorie().getName()+"\n");
			str.append("URL           : "+getServerBaseUrlBackend()+"vorgang/"+vorgang.getId()+"/uebersicht\n");
			str.append("-----------------------------------------------------------------------\n");
		}
		
		msg.setText(msg.getText().replaceAll("%vorgaenge%", str.toString()));
		jobExecutorService.runJob(new MailSenderJob(this, msg, null, null, null));	
	}
	
	public void sendInformExternMail(List<Vorgang> newVorgaenge, String[] to) {
		if (CollectionUtils.isEmpty(newVorgaenge) || ArrayUtils.isEmpty(to)) return;
		
		SimpleMailMessage msg = new SimpleMailMessage(informExternMailTemplate);
		msg.setTo(to);
		StringBuilder str = new StringBuilder();
		for(Vorgang vorgang : newVorgaenge) {
			str.append("ID            : "+vorgang.getId()+"\n");
			str.append("Hauptkategorie: "+vorgang.getKategorie().getParent().getName()+"\n");
			str.append("Kategorie     : "+vorgang.getKategorie().getName()+"\n");
			str.append("URL           : "+getServerBaseUrlBackend()+"vorgang/delegiert/"+vorgang.getId()+"/uebersicht\n");
			str.append("-----------------------------------------------------------------------\n");
		}
		
		msg.setText(msg.getText().replaceAll("%vorgaenge%", str.toString()));
		jobExecutorService.runJob(new MailSenderJob(this, msg, null, null, null));
	}

	public void sendInformErstellerMail(Vorgang vorgang) {
		SimpleMailMessage msg = new SimpleMailMessage(informErstellerMailTemplate);
		msg.setTo(vorgang.getAutorEmail());

		String mailtext = msg.getText();
		StringBuilder str = new StringBuilder();
		//Vorgang
		str.append("ID            : "+vorgang.getId()+"\n");
		str.append("Typ           : "+vorgang.getTyp().getText()+"\n");
		str.append("Hauptkategorie: "+vorgang.getKategorie().getParent().getName()+"\n");
		str.append("Kategorie     : "+vorgang.getKategorie().getName());
		mailtext = mailtext.replaceAll("%vorgang%", str.toString());
		//Datum
		mailtext = mailtext.replaceAll("%datum%", formatter.format(vorgang.getDatum()));
		//Status
		str = new StringBuilder();
		str.append("Status            : "+vorgang.getStatus().getText()+"\n");
		if (!StringUtils.isBlank(vorgang.getStatusKommentar()))
			str.append("Statuskommentar   : "+vorgang.getStatusKommentar()+"\n");
		mailtext = mailtext.replaceAll("%status%", str.toString());
		
		msg.setText(mailtext);
		
		jobExecutorService.runJob(new MailSenderJob(this, msg, null, null, null));
	}
	
	
	public String getServerBaseUrlBackend() {
		return getServerUrl(serverBaseUrlBackend);
	}

	public String getServerBaseUrlFrontend() {
		return getServerUrl(serverBaseUrlFrontend);
	}
	
	private String getServerUrl(String url) {
		if (!StringUtils.isBlank(url)) {
			return url.endsWith("/") ? url : url+"/";
		} else {
			try {
				return "http:/"+servletContext.getResource("/").getPath();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
//	public String getBaseUrl() {
//		if (!StringUtils.isBlank(baseUrl) && !baseUrl.trim().equalsIgnoreCase("backend")) return baseUrl;
//		try {
//			return "http:/"+servletContext.getResource("/").getPath()+"service/";
//		} catch (Exception e) {
//			return null;
//		}
//	}
//	
//	public void setBaseUrl(String baseUrl) {
//		this.baseUrl = baseUrl;
//	}
	
	
	
	
	public JavaMailSender getMailSender() {
		return mailSender;
	}
	public void setMailSender(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}
	public String getSendAllMailsTo() {
		return sendAllMailsTo;
	}
	public void setSendAllMailsTo(String sendAllMailsTo) {
		this.sendAllMailsTo = sendAllMailsTo;
	}
	public SimpleMailMessage getVorgangBestaetigungMailTemplate() {
		return vorgangBestaetigungMailTemplate;
	}
	public void setVorgangBestaetigungMailTemplate( SimpleMailMessage vorgangBestaetigungMailTemplate) {
		this.vorgangBestaetigungMailTemplate = vorgangBestaetigungMailTemplate;
	}
	public SimpleMailMessage getUnterstuetzungBestaetigungMailTemplate() {
		return unterstuetzungBestaetigungMailTemplate;
	}
	public void setUnterstuetzungBestaetigungMailTemplate( SimpleMailMessage unterstuetzungBestaetigungMailTemplate) {
		this.unterstuetzungBestaetigungMailTemplate = unterstuetzungBestaetigungMailTemplate;
	}
	public SimpleMailMessage getMissbrauchsmeldungBestaetigungMailTemplate() {
		return missbrauchsmeldungBestaetigungMailTemplate;
	}
	public void setMissbrauchsmeldungBestaetigungMailTemplate(SimpleMailMessage missbrauchsmeldungBestaetigungMailTemplate) {
		this.missbrauchsmeldungBestaetigungMailTemplate = missbrauchsmeldungBestaetigungMailTemplate;
	}
	public SimpleMailMessage getVorgangWeiterleitenMailTemplate() {
		return vorgangWeiterleitenMailTemplate;
	}
	public void setVorgangWeiterleitenMailTemplate(SimpleMailMessage vorgangWeiterleitenMailTemplate) {
		this.vorgangWeiterleitenMailTemplate = vorgangWeiterleitenMailTemplate;
	}
	public SimpleMailMessage getInformDispatcherMailTemplate() {
		return informDispatcherMailTemplate;
	}
	public void setInformDispatcherMailTemplate(SimpleMailMessage informDispatcherMailTemplate) {
		this.informDispatcherMailTemplate = informDispatcherMailTemplate;
	}
	public SimpleMailMessage getInformExternMailTemplate() {
		return informExternMailTemplate;
	}
	public void setInformExternMailTemplate(SimpleMailMessage informExternMailTemplate) {
		this.informExternMailTemplate = informExternMailTemplate;
	}
	public void setServerBaseUrlBackend(String serverBaseUrlBackend) {
		this.serverBaseUrlBackend = serverBaseUrlBackend;
	}
	public void setServerBaseUrlFrontend(String serverBaseUrlFrontend) {
		this.serverBaseUrlFrontend = serverBaseUrlFrontend;
	}
	public SimpleMailMessage getInformErstellerMailTemplate() {
		return informErstellerMailTemplate;
	}
	public void setInformErstellerMailTemplate(
			SimpleMailMessage informErstellerMailTemplate) {
		this.informErstellerMailTemplate = informErstellerMailTemplate;
	}	
}
