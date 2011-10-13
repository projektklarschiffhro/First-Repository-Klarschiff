package de.fraunhofer.igd.klarschiff.service.dbsync;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.hibernate.jdbc.Work;

/**
 * Klasse zum Ausführen eines SQL-Scriptes in der DB. Diese Klasse ist speziell für die Ausführung des Scriptes zum
 * Initialisieren der Trigger und Triggerfunktionen in der BackendDB gedacht.
 * @author Stefan Audersch (Fraunhofer IGD)
 * @author Hani Samara (Fraunhofer IGD)
 */
public class  InitSqlScriptWork implements Work {

	DbSyncService dbSyncService;
	boolean throwExeptionOnException;
	
	/**
	 * Initialisierung
	 * @param dbSyncService Service mit dem Zugriff auf DB-Parameter und das Script
	 * @param throwExeptionOnException Soll bei einem Fehler eine Exception geworfen werden? Ansonsten wird ein Logeintrag vorgenommen.
	 */
	public InitSqlScriptWork(DbSyncService dbSyncService, boolean throwExeptionOnException) {
		this.dbSyncService = dbSyncService;
		this.throwExeptionOnException = throwExeptionOnException;
	}
	
	/**
	 * Ausführen des Scriptes, wobei zuvor im Script die DB-Parameter für die FrontendDb entsprechend gesetzt werden.
     * Die DB-Parameter für die FrontendDB können im Script durch die Platzhalter <code>%host%</code>, <code>%port%</code>, 
     * <code>%dbname%</code>, <code>%username%</code> und <code>%password%</code> angegeben werden.
	 */
	@Override
	public void execute(Connection connection) throws SQLException {
		try {
			dbSyncService.getLogger().debug("init SQL script");
			PreparedStatement statement = connection.prepareStatement(dbSyncService.getInitSqlScript());
			statement.executeUpdate();
		} catch (Exception e) {
			if (throwExeptionOnException) {
				throw new SQLException(e);
			} else {
				dbSyncService.getLogger().error("Fehler bei der Ausführung des initSqlScript.", e);
			}
		}
	}

}
