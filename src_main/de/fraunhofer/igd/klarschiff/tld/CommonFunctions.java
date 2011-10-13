package de.fraunhofer.igd.klarschiff.tld;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class CommonFunctions {

	public static String[] array(String array) {
		return array.split(",");
	}
	
	public static String[] array(String array, String delim) {
		return array.split(delim);
	}

	public static String toHtml(String str) {
		return str.replaceAll("\n", "<br/>");
	}
	
	public static String whitespaceToHtml(String str) {
		return str.replaceAll("  ", "&nbsp;&nbsp;");
	}

	public static String abbreviate(String str, Integer maxWidth) {
		return StringUtils.abbreviate(str, maxWidth);
	}
	
	@SuppressWarnings({ "rawtypes" })
	public static Collection extractArrayItemFromList(Collection listOfArrays, Integer position) {
		List<Object> result = new ArrayList<Object>();
		for(Object array : listOfArrays) result.add(((Object[])array)[position]);
		return result;
	}
	
}
