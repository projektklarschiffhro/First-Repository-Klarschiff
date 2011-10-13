package de.fraunhofer.igd.klarschiff.util;

public class NumberUtil {

	public static Double min(Double a, Double b) {
		if (a==null) return b;
		if (b==null) return a;
		return Math.min(a, b);
	}

}
