package de.fraunhofer.igd.klarschiff.dao;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import de.fraunhofer.igd.klarschiff.vo.JobRun;
import de.fraunhofer.igd.klarschiff.vo.JobRun.Ergebnis;

@Repository
public class JobDao {

	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");

	@PersistenceContext
	EntityManager em;

	@Transactional
	public String registerJobRun(Date datum, String name, String serverIp, String serverName, String serverConnectorPort, int truncateField) {
		try {
			String id =	dateFormat.format(DateUtils.truncate(datum, truncateField)) + " " + name;
			
			EntityManager entityManager = em.getEntityManagerFactory().createEntityManager();
			entityManager.getTransaction().begin();
			try {
				entityManager.createNativeQuery("INSERT INTO klarschiff_job_run (id, ergebnis) VALUES (:id, :ergebnis)")
					.setParameter("id", id)
					.setParameter("ergebnis", Ergebnis.gestartet.name()) 
					.executeUpdate();
				entityManager.getTransaction().commit();
				entityManager.close();
			} catch (Exception e) {
				entityManager.getTransaction().rollback();
				entityManager.close();
				throw e;
			}

			JobRun jobRun = em.find(JobRun.class, id);
			jobRun.setName(name);
			jobRun.setDatum(datum);
			jobRun.setServerIp(serverIp);
			jobRun.setServerName(serverName);
			jobRun.setServerPort(serverConnectorPort);
			em.merge(jobRun);
			return id;
		} catch (Exception e) {
			return null;
		}
	}

	@Transactional
	public void updateJobRun(String id, Ergebnis ergebnis, Throwable exception) {
		JobRun jobRun = em.find(JobRun.class, id);
		jobRun.setErgebnis(ergebnis);
		if (exception!=null) {
    		ByteArrayOutputStream bos = new ByteArrayOutputStream();
    		PrintWriter pw = new PrintWriter(bos, true);
			exception.printStackTrace(pw);
		
			while(exception!=null) {
				//SQLException NextException
				if (exception instanceof SQLException && ((SQLException)exception).getNextException()!=null)
				{
					pw.println("\n---------- SQLException NextException ------------\n");
					((SQLException)exception).getNextException().printStackTrace(pw);
				}
				
				exception = exception.getCause();
			}
			jobRun.setFehlermeldung(new String(bos.toByteArray()));
		}
		em.merge(jobRun);
	}

	public Long getAnzahlAbgeschlosseneJobs() {
		return (Long)em.createQuery("SELECT COUNT(*) FROM JobRun j WHERE j.ergebnis=:ergebnis").setParameter("ergebnis", JobRun.Ergebnis.abgeschlossen).getSingleResult();
	}

	@SuppressWarnings("unchecked")
	public List<JobRun> getFehlerhafteJobs() {
		return em.createQuery("SELECT j FROM JobRun j WHERE j.ergebnis=:ergebnis ORDER BY j.datum DESC").setParameter("ergebnis", JobRun.Ergebnis.fehlerhaft).getResultList();
	}
}
