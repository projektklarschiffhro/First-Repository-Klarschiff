package de.fraunhofer.igd.klarschiff.util;

import org.springframework.core.io.ClassPathResource;

public class ClassPathResourceUtil {
	
	public static String readFile(String file) throws Exception
	{
		return new String(StreamUtil.readInputStream(new ClassPathResource(file).getInputStream()));
	}

}
