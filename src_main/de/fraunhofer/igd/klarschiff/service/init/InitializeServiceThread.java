package de.fraunhofer.igd.klarschiff.service.init;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Example;
import org.hibernate.impl.SessionImpl;
import org.hibernate.metadata.ClassMetadata;
import org.springframework.dao.DataAccessException;

import de.fraunhofer.igd.klarschiff.service.cluster.ClusterUtil;
import de.fraunhofer.igd.klarschiff.vo.EnumText;
import de.fraunhofer.igd.klarschiff.vo.extra.EnumFreigabeStatus;
import de.fraunhofer.igd.klarschiff.vo.extra.EnumNaehereBeschreibungNotwendig;
import de.fraunhofer.igd.klarschiff.vo.extra.EnumPrioritaet;
import de.fraunhofer.igd.klarschiff.vo.extra.EnumVerlaufTyp;
import de.fraunhofer.igd.klarschiff.vo.extra.EnumVorgangStatus;
import de.fraunhofer.igd.klarschiff.vo.extra.EnumVorgangTyp;
import de.fraunhofer.igd.klarschiff.vo.extra.EnumZustaendigkeitStatus;

public class InitializeServiceThread extends Thread {


	Logger logger;

	SessionFactory sessionFactory;
	Session session;
	Transaction transaction;

	InitializeService initializeService;
	
	
	@SuppressWarnings("static-access")
	public InitializeServiceThread(InitializeService initializeService)
	{
		this.initializeService = initializeService;
		logger = initializeService.getLogger();
		start();
	}

	@SuppressWarnings({ "static-access" })
	public void run()
	{
		try {
			setApacheDSWorkDir();
			sleep(initializeService.getStartDelay());
			initializeService.getLogger().debug("InitializeService started");
			
			
			sessionFactory = ((Session)initializeService.getEntityManager().getDelegate()).getSessionFactory();
			session = sessionFactory.openSession();
			session.setFlushMode(FlushMode.COMMIT);
			transaction = session.beginTransaction();			
			
			_initializeEnum();

			if (initializeService.getEntityManager().createQuery("select count(o) from Kategorie o", Long.class).getSingleResult()>0) {
				initializeService.getLogger().debug("init DB skiped");
			} else {
				//Tabellen mit initialen Daten füllen
				_initialize(initializeService.getInitObjectList());
	  			transaction.commit();
	  			
				if (initializeService.getInitSqlScript()!=InitializeService.InitSqlScript.disabled) 
				{
					//InitSqlScript ausführen
					initializeService.getDbSyncService().initDbLink(
							session, 
							initializeService.getInitSqlScript()==InitializeService.InitSqlScript.error);
				}
			}			

			
			
		} catch (Exception e) {
			try { transaction.rollback(); }catch (Exception ex) {}
			initializeService.getLogger().error(e);
		} finally {
			try { session.close(); }catch (Exception ex) {}
			initializeService.getLogger().debug("InitializeService stopped");
		}
	}
	
	private void removeApacheDSWorkDir() {
		try {
			String apacheWorkDir = System.getProperty("apacheDSWorkDir");
			
			if (apacheWorkDir == null) {
				apacheWorkDir = System.getProperty("java.io.tmpdir") + File.separator + "apacheds-spring-security";
			}
			FileUtils.deleteQuietly(new File(apacheWorkDir));
		} catch (Exception e) {}
	}
	
	private void setApacheDSWorkDir() {
		try {
	    	String id = ClusterUtil.getServerConnectorPort();
	    	if (id==null) id = UUID.randomUUID().toString();
			System.setProperty("apacheDSWorkDir", System.getProperty("java.io.tmpdir") + File.separator + "apacheds-spring-security "+id);

			removeApacheDSWorkDir();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void _initialize(List list) {
		for(Object o: list) {
			try {
				if (o instanceof List) _initialize((List)o);
				else {
					List l = findByExample(o);
					if (l.size()==1) {
						//Mergen
						Object entity = l.get(0);
				        Class clazz = Hibernate.getClass(entity);
				        ClassMetadata metadata = sessionFactory.getClassMetadata(clazz);
				        //Object id = metadata.getIdentifier(entity, EntityMode.POJO);
				        Object id = metadata.getIdentifier(entity, (SessionImpl)session);
						String idPropertyName = metadata.getIdentifierPropertyName();
						BeanUtils.setProperty(o, idPropertyName, id);
						session.refresh(o);
						logger.debug("Merge Object ["+o+"]");
					} else if (l.size()>1) {
						logger.warn("find duplicate object in db for  ["+o+"]");
					} else {
						//neu speichern
						session.saveOrUpdate(o);
						logger.debug("Save Object ["+o+"]");
					}
				}
			} catch (Exception e) {
				logger.warn("failed to save Object ["+o+"]", e);
			}
		}
		
	}

	@SuppressWarnings("rawtypes")
	private List findByExample(final Object exampleEntity) throws DataAccessException {

		Criteria executableCriteria = session.createCriteria(exampleEntity.getClass());
		executableCriteria.add(Example.create(exampleEntity));
		return executableCriteria.list();
	}

	private void _initializeEnum() {
		for (EnumText _enum : de.fraunhofer.igd.klarschiff.vo.EnumFreigabeStatus.values())
			session.saveOrUpdate(new EnumFreigabeStatus().fill(_enum));
		for (EnumText _enum : de.fraunhofer.igd.klarschiff.vo.EnumPrioritaet.values())
			session.saveOrUpdate(new EnumPrioritaet().fill(_enum));
		for (EnumText _enum : de.fraunhofer.igd.klarschiff.vo.EnumVerlaufTyp.values())
			session.saveOrUpdate(new EnumVerlaufTyp().fill(_enum));
		for (EnumText _enum : de.fraunhofer.igd.klarschiff.vo.EnumVorgangStatus.values())
			session.saveOrUpdate(new EnumVorgangStatus().fill(_enum));
		for (EnumText _enum : de.fraunhofer.igd.klarschiff.vo.EnumVorgangTyp.values())
			session.saveOrUpdate(new EnumVorgangTyp().fill(_enum));
		for (EnumText _enum : de.fraunhofer.igd.klarschiff.vo.EnumZustaendigkeitStatus.values())
			session.saveOrUpdate(new EnumZustaendigkeitStatus().fill(_enum));
		for (EnumText _enum : de.fraunhofer.igd.klarschiff.vo.EnumNaehereBeschreibungNotwendig.values())
			session.saveOrUpdate(new EnumNaehereBeschreibungNotwendig().fill(_enum));
	}
}

