package de.fraunhofer.igd.klarschiff.service.classification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import weka.classifiers.bayes.NaiveBayesUpdateable;
import weka.core.Instance;
import weka.core.Instances;
import de.fraunhofer.igd.klarschiff.dao.KategorieDao;
import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.service.security.Role;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.vo.Kategorie;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;
import de.fraunhofer.igd.klarschiff.vo.VorgangHistoryClasses;

/**
 * Die Klasse stellt einen Service als Klassifikator bzw. Zust�ndigkeitsfinder f�r das System bereit. Ein Vorgang kann dabei 
 * klassifiziert werden. Ergebnis dabei ist eine Liste von m�glichen Zust�ndigkeiten inkl. deren Relevanz. Damit der Klassifikator im
 * laufenden Betrieb aktualisiert bzw. erneuert werden kann, wird der eigentliche Klassifikator in einem Kontext gehalten.
 * @author Stefan Audersch (Fraunhofer IGD)
 * @author Marcus Kr�ller (Fraunhofer IGD)
 * @see de.fraunhofer.igd.klarschiff.service.classification.ClassificationContext
 */
@Service
public class ClassificationService {
	static final Logger logger = Logger.getLogger(ClassificationService.class);
	
	@Autowired
	FeatureService featureService;
	
	@Autowired
	VorgangDao vorgangDao;
	
	@Autowired
	KategorieDao kategorieDao;

	@Autowired
	SecurityService securityService;
	
	private int maxCountForClassifiereTrainSet = 500;
	protected int waitTimeToInitClassficationService = 10000;
	private ClassificationContext ctx;
	
	/**
	 * Initialisiert den Klassifikator und setzt den Kontext beim Service. Das Initialisieren wird als Thread
	 * ausgef�hrt, der erst eine Weile wartet, damit alle anderen Services, Repositorys etc. (z.B. LDAP, JPA)
	 * richtig initialisiert sind.
	 * @see de.fraunhofer.igd.klarschiff.service.classification.ClassficationServiceInitThread
	 */
	@PostConstruct
	public void init() {
		new ClassficationServiceInitThread(this);
	}
	
	
	/**
	 * Ermittelt f�r einen gegebenen Kontext das Trainingset f�r den Klassifikator 
	 * @param ctx Klassifikatorkontext
	 * @return Liste mit Trainingsdaten
	 * @throws Exception
	 * @see de.fraunhofer.igd.klarschiff.dao.VorgangDao#findVorgangForTrainClassificator(int)
	 */
	private Instances createTrainset(ClassificationContext ctx) throws Exception {
		Instances instances = new Instances(ctx.getDataset());
		//Vorg�nge f�r das Training holen
		List<Vorgang> vorgaenge = vorgangDao.findVorgangForTrainClassificator(maxCountForClassifiereTrainSet);
		//Features f�r jeden Vorgang ermitteln
		for (Vorgang vorgang : vorgaenge) {
			Instance instance = featureService.createFeature(vorgang, true, ctx);
			instance.setDataset(ctx.getDataset());
			instances.add(instance);
		}
		if (vorgaenge.size()<maxCountForClassifiereTrainSet) {
			//initiale Zust�ndigkeit bei den Kategorien hinzuf�gen
			for (Kategorie kategorie : kategorieDao.getKategorien()) {
				for (Instance instance : featureService.createFeature(kategorie, true, ctx)) {
					instance.setDataset(ctx.getDataset());
					instances.add(instance);
				}
			}
		}
		return instances;
	}
	
	/**
	 * Ermittelt die Zust�ndigkeit f�r einen Vorgang bei einem gegebenen Kontext. 
	 * @param vorgang Vorgang f�r den die Zust�ndigkeit ermittelt werden soll
	 * @param ctx Klassifikatorkontext
	 * @return Liste mit Zust�ndigkeiten und deren Relevanz
	 * @throws Exception
	 */
	private List<ClassificationResultEntry> classifierVorgang(Vorgang vorgang, ClassificationContext ctx) throws Exception {
		
		//Features erzeugen
		Instance instance = featureService.createFeature(vorgang, false, ctx);
		//Dataset setzen
		instance.setDataset(ctx.getDataset());
		//Klassifizieren
		double[] distribution = ctx.getClassifier().distributionForInstance(instance);
		//Map erzeugen
		List<ClassificationResultEntry> classificationResult = new ArrayList<ClassificationResultEntry>();
		for (int i = 0; i < distribution.length; i++)
			classificationResult.add(new ClassificationResultEntry(ctx.getClassAttribute().value(i), distribution[i]));
		//Sortieren
		Collections.sort(classificationResult, new Comparator<ClassificationResultEntry>() {
			public int compare(ClassificationResultEntry o1, ClassificationResultEntry o2) {
				return -(o1.getWeight().compareTo(o2.getWeight()));
			}
		});
		
		return classificationResult;
	}
	
	
	/**
	 * Ermittelt die aktuell zu verwendene Zust�ndigkeit. Bei der Berechnung wird das Ergebnis des Klassifikators sowie die History
	 * der bisher bereits verwendeten Zust�ndigkeiten ber�cksichtigt. Die berechnete Zust�ndigkeit ist somit immer die Gruppe mit der 
	 * h�chsten Relevanz, die aber noch nicht f�r die Zust�ndigkeit verwendet wurde.
	 * Die berechnete Zust�ndigkeit wird abschlie�end in die History der Zust�ndigkeiten f�r den Vorgang mit aufgenommen.
	 * @param vorgang Vorgang f�r den die Zust�ndigkeit berechnet werden soll
	 * @return berechnete Zust�ndigkeit
	 */
	public Role calculateZustaendigkeitforVorgang(Vorgang vorgang) {
		try {
			ClassificationResultEntry resultClass = null;
			//History f�r den vorgang ermittlen
			VorgangHistoryClasses history = vorgangDao.findVorgangHistoryClasses(vorgang);
			//Wenn der Dispatcher bereits in der History ist keine neue Klassifikation
			if (isDispatcherInVorgangHistoryClasses(history)) return securityService.getDispatcherZustaendigkeit();
			//Vorgang klassifizieren
			List<ClassificationResultEntry> classificationResult = classifierVorgang(vorgang, ctx);
			//Zust�ndigkeiten aus der History und aktuelle �berspringen
			for(ClassificationResultEntry entry : classificationResult) {
				if ((history==null || !history.getHistoryClasses().contains(entry.getClassValue())) && !StringUtils.equals(vorgang.getZustaendigkeit(), entry.getClassValue())) {
					resultClass = entry;
					break;
				}
			}
			
			//ggf. zust�ndigkeit an Dispatcher �bergeben
			if (resultClass==null) {
				resultClass = new ClassificationResultEntry(securityService.getDispatcherZustaendigkeitId(), 0d);
			}
			
			//gew�hlte Zust�ndigkeit in der History speichern
			try {
				boolean isNew = false;
				if (history==null)
				{
					isNew = true;
					history = new VorgangHistoryClasses();
					history.setVorgang(vorgang);
				}
				history.getHistoryClasses().add(resultClass.getClassValue());
				if (isNew) vorgangDao.persist(history); else vorgangDao.merge(history);
			} catch (Exception e) {
				logger.error("Eine zugewiesene Zust�ndigkeit konnte nicht in die VorgangHistoryClass aufgenommen werden.", e);
			}

			return securityService.getZustaendigkeit(resultClass.getClassValue());
		} catch (Exception e) {
			logger.error("Die Zust�ndigkeit f�r den Vorgang konnte nicht ermittelt werden.", e);
			throw new RuntimeException("Die Zust�ndigkeit f�r den Vorgang konnte nicht ermittelt werden.", e);
		}
	}
	
	
	/**
	 * Ermittelt, ob bereits der Dispatcher f�r den Vorgang zust�ndig war
	 * @param history Zust�ndigkeitshistory f�r den Vorgang
	 * @return ja bzw. nein
	 */
	private boolean isDispatcherInVorgangHistoryClasses(VorgangHistoryClasses history) {
		if (history==null) return false;
		if (history.getHistoryClasses()==null) return false;
		return history.getHistoryClasses().contains(securityService.getDispatcherZustaendigkeitId());
	}

	
	/**
	 * Ermittelt, ob bereits der Dispatcher f�r den Vorgang zust�ndig war.
	 * @return ja bzw. nein
	 */
	public boolean isDispatcherInVorgangHistoryClasses(Vorgang vorgang) {
		return isDispatcherInVorgangHistoryClasses(vorgangDao.findVorgangHistoryClasses(vorgang));
	}
	
	
	/**
	 * Aktualisiert den Klassifikator mit der aktuellen Zust�ndigkeit des Vorgangs. Dabei wird f�r den gegebenen
	 * Vorgang ein Trainingsset erzeugt, womit der Klassifikator aktualisiert wird.
	 * @param vorgang Vorgang, der zur Aktualisierung verwendet wird
	 */
	public void registerZustaendigkeitAkzeptiert(Vorgang vorgang) {
		try {
			updateClassifier(vorgang, ctx);
		} catch (Exception e) {
			logger.error("Der Klassifizierer konnte nicht mit einer akzeptierten Zust�ndigkeit aktualisiert werden.", e);
		}
	}
	
	/**
	 * Aktualisiert den Klassifikator mit der aktuellen Zust�ndigkeit des Vorgangs. Dabei wird f�r den gegebenen
	 * Vorgang ein Trainingsset erzeugt, womit der Klassifikator aktualisiert wird.
	 * @param vorgang Vorgang, der zur Aktualisierung verwendet wird
	 * @param ctx Klassifikatorkontext
	 * @throws Exception
	 */
	private void updateClassifier(Vorgang vorgang, ClassificationContext ctx) throws Exception {
		Instance instance = featureService.createFeature(vorgang, true, ctx);
		instance.setDataset(ctx.getDataset());
		ctx.getClassifier().updateClassifier(instance);
	}
	
	
	/**
	 * aktualisiert den Klassifikator komplett anhand einer Trainingsmenge. Der Klassifikatorkontext wird danach beim Service neu gesetzt.
	 * @throws Exception
	 * @see #createTrainset(ClassificationContext)
	 */
	public void reBuildClassifier() throws Exception {
		logger.debug("rebuild ClassificationContext");
		ClassificationContext ctx = new ClassificationContext();
		//Klassifikator neu initialisieren
		ctx.setClassifier(new NaiveBayesUpdateable());
		//attributes und classAttribut neu initialisieren
		featureService.initClassificationContext(ctx);
		//Dataset neu laden
		ctx.setDataset(new Instances("", ctx.getAttributes(), 0));
		ctx.getDataset().setClass(ctx.getClassAttribute());
		//Trainingset neu erstellen
		Instances trainSet = createTrainset(ctx);
		//Klassifikator neu trainieren
		ctx.getClassifier().buildClassifier(trainSet);
		//neuen context setzen
		this.ctx = ctx;
	}
}