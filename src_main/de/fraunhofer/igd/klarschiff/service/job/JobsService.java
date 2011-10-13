package de.fraunhofer.igd.klarschiff.service.job;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.service.classification.ClassificationService;
import de.fraunhofer.igd.klarschiff.service.cluster.ScheduledSyncInCluster;
import de.fraunhofer.igd.klarschiff.service.mail.MailService;
import de.fraunhofer.igd.klarschiff.service.security.Role;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.vo.Missbrauchsmeldung;
import de.fraunhofer.igd.klarschiff.vo.Unterstuetzer;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;

@Service
public class JobsService {
	private static final Logger logger = Logger.getLogger(JobsService.class);
	
	int monthsToArchivVorgaenge;
	int hoursToRemoveUnbestaetigtVorgang;
	int hoursToRemoveUnbestaetigtUnterstuetzer;
	int hoursToRemoveUnbestaetigtMissbrauchsmeldung;
	
	@Autowired
	VorgangDao vorgangDao;
	
	@Autowired
	SecurityService securityService;
	
	@Autowired
	MailService mailService;
	
	@Autowired
	ClassificationService classificationService;
	
	@Transactional
	@ScheduledSyncInCluster(cron="0 40 0 * * *", name="Vorgaenge archivieren")
	public void archivVorgaenge() {
		Date date = DateUtils.addMonths(new Date(), -monthsToArchivVorgaenge);
		for(Vorgang vorgang : vorgangDao.findNotArchivVorgang(date))
		{
			vorgang.setArchiviert(true);
			vorgangDao.merge(vorgang);
		}
	}

	@Transactional
	@ScheduledSyncInCluster(cron="0 43 * * * *", name="unbestaetigte Vorgaenge entfernen")
	public void removeUnbestaetigtVorgang() {
		Date date = DateUtils.addHours(new Date(), -hoursToRemoveUnbestaetigtVorgang);
		for(Vorgang vorgang : vorgangDao.findUnbestaetigtVorgang(date))
		{
			vorgangDao.remove(vorgang);
		}
	}

	@Transactional
	@ScheduledSyncInCluster(cron="0 46 * * * *", name="unbestaetigte Unterstuetzer entfernen")
	public void removeUnbestaetigtUnterstuetzer() {
		Date date = DateUtils.addHours(new Date(), -hoursToRemoveUnbestaetigtUnterstuetzer);
		for(Unterstuetzer unterstuetzer : vorgangDao.findUnbestaetigtUnterstuetzer(date))
		{
			vorgangDao.remove(unterstuetzer);
		}
	}

	@Transactional
	@ScheduledSyncInCluster(cron="0 49 * * * *", name="unbestaetigte Missbrauchsmeldungen entfernen")
	public void removeUnbestaetigtMissbrauchsmeldung() {
		Date date = DateUtils.addHours(new Date(), -hoursToRemoveUnbestaetigtMissbrauchsmeldung);
		for(Missbrauchsmeldung missbrauchsmeldung : vorgangDao.findUnbestaetigtMissbrauchsmeldung(date))
		{
			vorgangDao.remove(missbrauchsmeldung);
		}
	}

	@Scheduled(cron="0 52 * * * *")
	public void reBuildClassifier() {
		try {
			classificationService.reBuildClassifier();
		} catch (Exception e) {
			logger.error("ClassificationContext konnte nicht erneuert werden.", e);
		}
	}

	@ScheduledSyncInCluster(cron="0 0 5 * * *", name="Externe ueber neue Vorgaenge informieren")
	public void informExtern() {
		Date date = DateUtils.addDays(new Date(), -1);

		//F�r alle delegiertAn
		for(Role delegiertAn : securityService.getAllDelegiertAn()) {

			//Finde alle Vorg�nge, dessen DelegiertAn in den letzten 24h ge�ndert wurde und deren DelegiertAn=delegiertAn ist
			List<Vorgang> vorgaenge = vorgangDao.findVorgaengeForDelegiertAn(date, delegiertAn.getId());

			//sende eMail
			mailService.sendInformExternMail(vorgaenge, securityService.getAllUserEmailsForRole(delegiertAn.getId()));
		}
	}
	
	@ScheduledSyncInCluster(cron="0 5 5 * * *", name="Dispatcher ueber neue Vorgaenge informieren")
	public void informDispatcher() {
		Date date = DateUtils.addDays(new Date(), -1);
		
		//Finde alle Vorg�nge, dessen Zust�ndigkeit in den letzten 24h ge�ndert wurde und deren Zust�ndigkeit=dispatcher ist
		List<Vorgang> vorgaenge = vorgangDao.findVorgaengeForZustaendigkeit(date, securityService.getDispatcherZustaendigkeitId());
		
		//sende eMail
		mailService.sendInformDispatcherMail(vorgaenge, securityService.getAllUserEmailsForRole(securityService.getDispatcherZustaendigkeitId()));
	}

	@ScheduledSyncInCluster(cron="0 5 10 * * *", name="Ersteller ueber Vorgangsabschluss informieren")
	public void informErsteller() {
		Date date = DateUtils.addDays(new Date(), -1);
		
		//Finde alle Vorg�nge, die in den letzten 24h abgeschlossen wurden und eine autorEmail haben
		List<Vorgang> vorgaenge = vorgangDao.findClosedVorgaenge(date);
		
		//sende eMail
		for (Vorgang vorgang : vorgaenge)
			mailService.sendInformErstellerMail(vorgang);
	}


//	@ScheduledSyncInCluster(cron="0 * * * * *", name="test")
//	public void test() {
//		if (new Random().nextBoolean()) throw new RuntimeException("Fehlermeldung");
//	}
	
	
	public int getMonthsToArchivVorgaenge() {
		return monthsToArchivVorgaenge;
	}

	public void setMonthsToArchivVorgaenge(int monthsToArchivVorgaenge) {
		this.monthsToArchivVorgaenge = monthsToArchivVorgaenge;
	}

	public int getHoursToRemoveUnbestaetigtVorgang() {
		return hoursToRemoveUnbestaetigtVorgang;
	}

	public void setHoursToRemoveUnbestaetigtVorgang(
			int hoursToRemoveUnbestaetigtVorgang) {
		this.hoursToRemoveUnbestaetigtVorgang = hoursToRemoveUnbestaetigtVorgang;
	}

	public int getHoursToRemoveUnbestaetigtUnterstuetzer() {
		return hoursToRemoveUnbestaetigtUnterstuetzer;
	}

	public void setHoursToRemoveUnbestaetigtUnterstuetzer(
			int hoursToRemoveUnbestaetigtUnterstuetzer) {
		this.hoursToRemoveUnbestaetigtUnterstuetzer = hoursToRemoveUnbestaetigtUnterstuetzer;
	}

	public int getHoursToRemoveUnbestaetigtMissbrauchsmeldung() {
		return hoursToRemoveUnbestaetigtMissbrauchsmeldung;
	}

	public void setHoursToRemoveUnbestaetigtMissbrauchsmeldung(
			int hoursToRemoveUnbestaetigtMissbrauchsmeldung) {
		this.hoursToRemoveUnbestaetigtMissbrauchsmeldung = hoursToRemoveUnbestaetigtMissbrauchsmeldung;
	}
}
