package de.fraunhofer.igd.klarschiff.service.classification;

/**
 * Der Thread dient zum Initialisieren des Klassifikators. Nach dem Start des Threads
 * wird zunächst eine gegebene Zeit gewartet.
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public class ClassficationServiceInitThread extends Thread {	
	ClassificationService classificationService;
	
	
	/**
	 * Initialisieren des Threads
	 * @param classificationService ClassificationService, der initialisiert werden soll
	 */
	public ClassficationServiceInitThread(ClassificationService classificationService) {
		this.classificationService = classificationService;
		start();
	}
	
	
	/**
	 * Warten und initialisieren des Klassifikators
	 */
	@SuppressWarnings("static-access")
	public void run() {
		try {
			classificationService.logger.debug("init ClassificationService in 5000ms ...");
			sleep(classificationService.waitTimeToInitClassficationService);
			classificationService.logger.debug("init ClassificationService now ...");
			classificationService.reBuildClassifier();
		} catch (Exception e) {
			classificationService.logger.error("Fehler beim Initialisieren des ClassificationService", e);
		}
	}
}