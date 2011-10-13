package de.fraunhofer.igd.klarschiff.service.dbsync;

/**
 * Klasse für das Ablegen des Ergebnisses eines externen Programmaufrufes 
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public class ProcessResult {
	int exitValue;
	String output;
	
	/**
	 * @param exitValue Exit-Wert
	 * @param output Output und Fehleroutput des Kommandozeilenaufrufes 
	 */
	public ProcessResult(int exitValue, String output) {
		super();
		this.exitValue = exitValue;
		this.output = output;
	}
	public int getExitValue() {
		return exitValue;
	}
	public String getOutput() {
		return output;
	}
}
