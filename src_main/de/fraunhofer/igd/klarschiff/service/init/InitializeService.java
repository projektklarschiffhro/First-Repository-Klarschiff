package de.fraunhofer.igd.klarschiff.service.init;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import de.fraunhofer.igd.klarschiff.service.dbsync.DbSyncService;


/**
 * Klasse zum Initialisieren der DB mit Werten die in der Konfiguration vergegeben werden. Die eigentliche 
 * Initialisierung erfolgt innerhalb eines Thread.
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public class InitializeService {

	private final static Logger logger = Logger.getLogger(InitializeService.class);

	@PersistenceContext(unitName="persistenceUnit")
	private EntityManager entityManager;
	
	@Autowired
	DbSyncService dbSyncService;
	
	/**
	 * Aktionen für das Ausführen eines SQL-Scriptes (z.B. Initialisierung des DbLink mit Trigger und Triggerfunktionen
	 * für die Synchronisystion der Frontend- und BackendDB)
	 */
	public enum InitSqlScript { disabled, warn, error }
	
	boolean enable = true;

	InitSqlScript initSqlScript = InitSqlScript.disabled;
	
	Long startDelay = new Long(1000);

	/**
	 * Liste mit den in der DB zu initalisierenden Objekten
	 */
	List<Object> initObjectList = new ArrayList<Object>();
	
	
	/**
	 * Start des Thread zum Ausführen der Initialisierung der DB.
	 * @throws Exception
	 */
    @PostConstruct
    public void afterPropertiesSet() throws Exception
    {
    	if (enable)	new InitializeServiceThread(this);
    }

	/* --------------- GET + SET ----------------------------*/

    public boolean getEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public Long getStartDelay() {
		return startDelay;
	}

	public void setStartDelay(Long startDelay) {
		this.startDelay = startDelay;
	}

	public static Logger getLogger() {
		return logger;
	}

	public List<Object> getInitObjectList() {
		return initObjectList;
	}

	public void setInitObjectList(List<Object> initObjectList) {
		this.initObjectList = initObjectList;
	}

	public EntityManager getEntityManager() {
		return entityManager;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
	public InitSqlScript getInitSqlScript() {
		return initSqlScript;
	}

	public void setInitSqlScript(InitSqlScript initSqlScript) {
		this.initSqlScript = initSqlScript;
	}

	public DbSyncService getDbSyncService() {
		return dbSyncService;
	}
}


