package de.fraunhofer.igd.klarschiff.service.dbsync;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.fraunhofer.igd.klarschiff.service.settings.SettingsService;
import de.fraunhofer.igd.klarschiff.util.ClassPathResourceUtil;
import de.fraunhofer.igd.klarschiff.util.StreamUtil;

/**
 * Die Klasse stellt einen Service für die Synchronisation der Frontend- und BackendDB bereit. Für die Synchronisation
 * wird zur Laufzeit DbLink verwendet, wofür Trigger und Triggerfunktionen bei der BackendDb eingerichtet werden.
 * Im Initialisierungsscript für die Trigger und Triggerfunktionen können die Parameter für die FrontendDb durch die 
 * Platzhalter <code>%host%</code>, <code>%port%</code>, <code>%dbname%</code>, <code>%username%</code> und 
 * <code>%password%</code> angegeben werden.
 * Eine Synchronisation bei Bedarf kann über einen Kommandozeilenaufruf erfolgen.
 * @author Stefan Audersch (Fraunhofer IGD)
 * @author Hani Samara (Fraunhofer IGD)
 */
@Service
public class DbSyncService {
	
	public static final Logger logger = Logger.getLogger(DbSyncService.class);
	
	private enum OperationSystem { win, linux };
	static final String FS = System.getProperty("file.separator");
	static final String OS = System.getProperty("os.name");
	static final String JH = System.getProperty("java.home");
	
	static final String F_HOST = "Frontend_Server";
	static final String F_PORT = "Frontend_Port";
	static final String F_SCHEMA = "Frontend_Schema";
	static final String F_DBNAME = "Frontend_Database";
	static final String F_USERNAME = "Frontend_Login";
	static final String F_PASSWORD = "Frontend_Password";
	static final String B_HOST = "Backend_Server";
	static final String B_PORT = "Backend_Port";
	static final String B_SCHEMA = "Backend_Schema";
	static final String B_DBNAME = "Backend_Database";
	static final String B_USERNAME = "Backend_Login";
	static final String B_PASSWORD = "Backend_Password";
	static final String LOG_DIR = "log_dir";
	
	@PersistenceContext(unitName="persistenceUnit")
	private EntityManager entityManager;
	
	@Autowired
	SettingsService settingsService;
	
	String initSqlScriptFile;
	String frontendDbHost;
	String frontendDbPort;
	String frontendDbSchema;
	String frontendDbDbName;
	String frontendDbUsername;
	String frontendDbPassword;
	String backendDbHost;
	String backendDbPort;
	String backendDbSchema;
	String backendDbDbName;
	String backendDbUsername;
	String backendDbPassword;
	
	String dbSyncProgramDir;
	
	public Logger getLogger() {
		return logger;
	}

	
	/**
	 * Initialisiert die notwendigen Trigger und Triggerfunktionen für das DbLink anhand eines in der Konfiguration vorgegebenen
	 * Scriptes. Die Ausführung des Scriptes erfolgt über ein InitSqlScriptWork.
     * Die DB-Parameter für die FrontendDB können im Script durch die Platzhalter <code>%host%</code>, <code>%port%</code>, 
     * <code>%dbname%</code>, <code>%username%</code> und <code>%password%</code> angegeben werden.
	 * @param session Session zum Zugriff auf die DB
	 * @param throwExeptionOnException Soll bei einem Fehler eine Exception geworfen werden? Ansonsten wird nur ein Logeintrag vorgenommen.
	 * @see InitSqlScriptWork
	 */
	public void initDbLink(Session session, boolean throwExeptionOnException) {
		Transaction transaction = null;

		try {
			transaction = session.beginTransaction();			
			session.doWork(new InitSqlScriptWork(this, throwExeptionOnException));
  			transaction.commit();
			
		} catch (Exception e) {
			try { transaction.rollback(); }catch (Exception ex) {}
			if (throwExeptionOnException) {
				throw new RuntimeException(e);
			} else {
				logger.error(e);
			}
		}
	}
	
	
	/**
	 * Initialisiert die notwendigen Trigger und Triggerfunktionen für das DbLink anhand eines in der Konfiguration vorgegebenen
	 * Scriptes. Eine Session für die DB wird vom EntityManager geholt.
     * Die DB-Parameter für die FrontendDB können im Script durch die Platzhalter <code>%host%</code>, <code>%port%</code>, 
     * <code>%dbname%</code>, <code>%username%</code> und <code>%password%</code> angegeben werden.
	 * @param throwExeptionOnException Soll bei einem Fehler eine Exception geworfen werden? Ansonsten wird nur ein Logeintrag vorgenommen.
	 */
	public void initDbLink(boolean throwExeptionOnException) {
		SessionFactory sessionFactory;
		Session session = null;

		try {
			sessionFactory = ((Session)entityManager.getDelegate()).getSessionFactory();
			session = sessionFactory.openSession();
			session.setFlushMode(FlushMode.COMMIT);

			initDbLink(session, throwExeptionOnException);
			
		} catch (Exception e) {
			if (throwExeptionOnException) {
				throw new RuntimeException(e);
			} else {
				logger.error(e);
			}
		} finally {
			try { session.close(); }catch (Exception ex) {}
		}
		
	}
	
	
	public String getInitSqlScript() {
		String script;
		try {
			script = ClassPathResourceUtil.readFile(getInitSqlScriptFile());
			script = script.replace("%host%", getFrontendDbHost());
			script = script.replace("%port%", getFrontendDbPort());
			script = script.replace("%dbname%", getFrontendDbDbName());
			script = script.replace("%username%", getFrontendDbUsername());
			script = script.replace("%password%", getFrontendDbPassword());
			return script;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Synchronisiert die DB des Frontends mit der des Backends anhand eines über die Konfiguration definierbaren 
	 * Komandozeilenaufrufes.
	 * Der Output der Kommandozeile wird mit Hilfe eines Gobbler's abgefangen.
	 * @return Ergebnis des Kommandozeilenaufrufes
	 * @see StreamGobbler
	 * @see ProcessResult
	 */
	public ProcessResult syncDb() {
		String profile=settingsService.getProfile();

		try {
//			//Umgebungsvariablen
//			for(Object key : System.getProperties().keySet())
//				System.out.println("prop: "+key+"="+System.getProperty((String)key));
//			for(String key : System.getenv().keySet())
//				System.out.println("env: "+key+"="+System.getenv().get(key));

			//neue Parameter ermitteln
			Properties newProp = new Properties();
			newProp.setProperty(F_HOST, frontendDbHost);
			newProp.setProperty(F_PORT, frontendDbPort);
			newProp.setProperty(F_SCHEMA, frontendDbSchema);
			newProp.setProperty(F_DBNAME, frontendDbDbName);
			newProp.setProperty(F_USERNAME, frontendDbUsername);
			newProp.setProperty(F_PASSWORD, frontendDbPassword);
			newProp.setProperty(B_HOST, backendDbHost);
			newProp.setProperty(B_PORT, backendDbPort);
			newProp.setProperty(B_SCHEMA, backendDbSchema);
			newProp.setProperty(B_DBNAME, backendDbDbName);
			newProp.setProperty(B_USERNAME, backendDbUsername);
			newProp.setProperty(B_PASSWORD, backendDbPassword);

			String contextParams = "";
			for(Object key : newProp.keySet())
				 contextParams += " --context_param "+key+"="+newProp.getProperty((String)key);
			
			OperationSystem operationSystem = (OS.toLowerCase().contains("windows")) ? OperationSystem.win : OperationSystem.linux;
			
			//Startdatei erzeugen
			File startScriptFile = null;
			switch (operationSystem) {
				case win:
					{
						String javaRuntime=System.getProperty("java.home")+FS+"bin"+FS+"java";
						//Orginal Startdatei finden
						File defaultStartScriptFile = FileUtils.iterateFiles(new File(dbSyncProgramDir), FileFilterUtils.suffixFileFilter("Everything_run.bat"), FileFilterUtils.directoryFileFilter()).next();
						//neue Startdatei festlegen
						startScriptFile = new File(defaultStartScriptFile.getParentFile().getAbsolutePath()+FS+"run_"+profile+".bat");
						String script = new String(StreamUtil.readInputStream(new FileInputStream(defaultStartScriptFile)));
						//Erste Zeile entfernen
						script = script.replaceAll("%~d0\\s*", ""); 
						//Verzeichniswechsel ändern
						script = StringUtils.replace(script, "%~dp0", "\""+defaultStartScriptFile.getParentFile().getAbsolutePath()+"\""); 
						//Java setzen
						script = StringUtils.replace(script, "\njava ", "\n\""+javaRuntime+"\" "); 
						//Context setzen
						script = StringUtils.replace(script, "Default %*", "Default"+contextParams); 
//						script = StringUtils.replace(script, "Default %*", profile+contextParams); 
						//ggf. vorhandenen neu Startdatei löschen
						FileUtils.deleteQuietly(startScriptFile);
						//neu Startdatei schreiben
						FileUtils.write(startScriptFile, script);
					}
					break;
				case linux:
					{
						String javaRuntime=System.getProperty("java.home")+FS+"bin"+FS+"java";
						//Orginal Startdatei finden
						File defaultStartScriptFile = FileUtils.iterateFiles(new File(dbSyncProgramDir), FileFilterUtils.suffixFileFilter("Everything_run.sh"), FileFilterUtils.directoryFileFilter()).next();
						//neue Startdatei festlegen
						startScriptFile = new File(defaultStartScriptFile.getParentFile().getAbsolutePath()+FS+"run_"+profile+".sh");
						String script = new String(StreamUtil.readInputStream(new FileInputStream(defaultStartScriptFile)));
						//Verzeichniswechsel ändern
						script = StringUtils.replace(script, "$0", defaultStartScriptFile.getParentFile().getAbsolutePath()); 
						//Java setzen
						script = StringUtils.replace(script, "\njava ", "\n"+javaRuntime+" "); 
						//Context setzen
						script = StringUtils.replace(script, "Default $*", "Default"+contextParams); 
//						script = StringUtils.replace(script, "Default %*", profile+contextParams); 
						//ggf. vorhandenen neu Startdatei löschen
						FileUtils.deleteQuietly(startScriptFile);
						//neu Startdatei schreiben
						FileUtils.write(startScriptFile, script);
					}
					break;
				default:
					throw new NotImplementedException();
			}
			
			
//			//ContextDateien anpassen
//			for (File defaultContextFile : FileUtils.listFiles(new File(dbSyncProgramDir), FileFilterUtils.nameFileFilter("Default.properties"), FileFilterUtils.directoryFileFilter())) {
//				//PropertieDatei laden
//				Properties prop = new Properties();
//				prop.load(new FileInputStream(defaultContextFile));
//				//Properties anpassen
//				for(Object key : prop.keySet()) 
//					if (newProp.containsKey(key))
//						prop.put(key, newProp.get(key));
//				//neue PropertieDatei festlegen
//				File contextfile = new File(defaultContextFile.getParentFile().getAbsolutePath()+FS+profile+".properties");
//				//ggf. vorhandenen PropertieDatei löschen
//				FileUtils.deleteQuietly(contextfile);
//				//neue PropertieDatei speichern
//				prop.store(new FileOutputStream(contextfile), null);
//			}
			
//			//.item-Dateien anpassen
//			SAXBuilder saxBuilder = new SAXBuilder();
//			XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
//			for (File itemFile : FileUtils.listFiles(new File(dbSyncProgramDir), FileFilterUtils.suffixFileFilter(".item"), FileFilterUtils.directoryFileFilter())) {
////			for (File itemFile : FileUtils.listFiles(new File(dbSyncProgramDir), FileFilterUtils.fileFileFilter(), FileFilterUtils.directoryFileFilter())) {
//				try {
//					if (itemFile.getName().endsWith("B_Lookups_0.1.item")) {
//						System.out.println(itemFile);
//					}
//					
//					Document doc = saxBuilder.build(itemFile);
//					boolean isChanged = false;
//					for(Object key : newProp.keySet()) {
//						if (StringUtils.equals((String)key, F_PASSWORD)||StringUtils.equals((String)key, B_PASSWORD)) continue;
//						for(Object node : XPath.selectNodes(doc, "//contextParameter[@name='"+key+"']")) {
//							isChanged=true;
//							Element elem = (Element)node;
//							elem.setAttribute("value", newProp.getProperty((String)key));
//						}
//						XPath xpath = XPath.newInstance("//talend:contextParameter[@name='"+key+"']");
//						xpath.addNamespace("talend", "http://www.talend.org/mapper");
//						for(Object node : xpath.selectNodes(doc)) {
//							isChanged=true;
//							Element elem = (Element)node;
//							elem.setAttribute("value", newProp.getProperty((String)key));
//						}
//						
//					}
//					if (isChanged) {
//						xmlOutputter.output(doc, new FileOutputStream(itemFile));
//						//System.out.println("File changed: "+itemFile);
//					} else {
//						//System.out.println("XML "+itemFile);
//						
//					}
//				} catch (Exception e) {
//					//System.out.println("NOXML "+itemFile);
//				}
//			}
			
			Process p = Runtime.getRuntime().exec(startScriptFile.getAbsolutePath());

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(bos);
			
			StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(),  "ERROR  ", ps);            
			StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT ", ps);
       
			errorGobbler.start();
			outputGobbler.start();
                           
			int exitVal = p.waitFor();
			ps.println("ExitValue: " + exitVal);        
			ps.close();
			
			return new ProcessResult(exitVal, new String(bos.toByteArray()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String getFrontendDbSchema() {
		return frontendDbSchema;
	}

	public void setFrontendDbSchema(String frontendDbSchema) {
		this.frontendDbSchema = frontendDbSchema;
	}

	public String getBackendDbHost() {
		return backendDbHost;
	}

	public void setBackendDbHost(String backendDbHost) {
		this.backendDbHost = backendDbHost;
	}

	public String getBackendDbPort() {
		return backendDbPort;
	}

	public void setBackendDbPort(String backendDbPort) {
		this.backendDbPort = backendDbPort;
	}

	public String getBackendDbSchema() {
		return backendDbSchema;
	}

	public void setBackendDbSchema(String backendDbSchema) {
		this.backendDbSchema = backendDbSchema;
	}

	public String getBackendDbDbName() {
		return backendDbDbName;
	}

	public void setBackendDbDbName(String backendDbDbName) {
		this.backendDbDbName = backendDbDbName;
	}

	public String getBackendDbUsername() {
		return backendDbUsername;
	}

	public void setBackendDbUsername(String backendDbUsername) {
		this.backendDbUsername = backendDbUsername;
	}

	public String getBackendDbPassword() {
		return backendDbPassword;
	}

	public void setBackendDbPassword(String backendDbPassword) {
		this.backendDbPassword = backendDbPassword;
	}

	public void setInitSqlScriptFile(String initSqlScriptFile) {
		this.initSqlScriptFile = initSqlScriptFile;
	}

	public void setFrontendDbHost(String frontendDbHost) {
		this.frontendDbHost = frontendDbHost;
	}

	public void setFrontendDbPort(String frontendDbPort) {
		this.frontendDbPort = frontendDbPort;
	}

	public void setFrontendDbDbName(String frontendDbDbName) {
		this.frontendDbDbName = frontendDbDbName;
	}

	public void setFrontendDbUsername(String frontendDbUsername) {
		this.frontendDbUsername = frontendDbUsername;
	}

	public void setFrontendDbPassword(String frontendDbPassword) {
		this.frontendDbPassword = frontendDbPassword;
	}

	public String getInitSqlScriptFile() {
		return initSqlScriptFile;
	}

	public String getFrontendDbHost() {
		return frontendDbHost;
	}

	public String getFrontendDbPort() {
		return frontendDbPort;
	}

	public String getFrontendDbDbName() {
		return frontendDbDbName;
	}

	public String getFrontendDbUsername() {
		return frontendDbUsername;
	}

	public String getFrontendDbPassword() {
		return frontendDbPassword;
	}

	public String getDbSyncProgramDir() {
		return dbSyncProgramDir;
	}

	public void setDbSyncProgramDir(String dbSyncProgramDir) {
		this.dbSyncProgramDir = dbSyncProgramDir;
	}
}
