package de.fraunhofer.igd.klarschiff.service.dbsync;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 * Die Klasse ist eine Hilfsklasse zum Auslesen des Outputs beim Aufruf eines Programmes über die Kommandozeile. 
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public class StreamGobbler extends Thread
{
	InputStream is;
	String type;
	PrintStream ps;
    
	/**
	 * Initialisierung
	 * @param is Inputstream (z.B. <code>out</code> bzw. <code>err</code>)
	 * @param type Wird als Präfix vor jede Zeile geschrieben (z.B. <code>out</code> bzw. <code>err</code>)
	 * @param ps Printstream in dem die Outputs gestreamt werden
	 */
	StreamGobbler(InputStream is, String type, PrintStream ps)
	{
	    this.is = is;
	    this.type = type;
	    this.ps = ps;
	}
    
	/**
	 * Start des StreamGobbler als Thread.
	 */
    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line=null;
            while ( (line = br.readLine()) != null) ps.println(type + line);    
        } catch (IOException ioe) {
        	ioe.printStackTrace(ps);  
        }
    }
}