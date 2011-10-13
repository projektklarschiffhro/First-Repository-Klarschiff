package de.fraunhofer.igd.klarschiff.util;

import java.io.UnsupportedEncodingException;

public class StringUtil {
	public static String encode(String str, String fromEncoding, String toEncoding) {
		try {
			return new String(str.getBytes(fromEncoding), toEncoding);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}
