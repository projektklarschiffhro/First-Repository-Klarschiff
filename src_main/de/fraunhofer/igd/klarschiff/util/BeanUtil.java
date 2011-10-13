package de.fraunhofer.igd.klarschiff.util;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class BeanUtil {

	/**
	 * Zugriff auf ein Property
	 * @param bean
	 * @param property
	 * @return Property als Object oder null
	 */
	public static Object getProperty(Object bean, String property)
	{
		ExpressionParser parser = new SpelExpressionParser();
		Expression exp = parser.parseExpression(property);
		EvaluationContext context = new StandardEvaluationContext(bean);
		try {
			return exp.getValue(context);
		} catch (Exception e) {
			return null;
		}
	}


	/**
	 * Zugriff auf ein Property
	 * @param bean
	 * @param property
	 * @return Property als String oder null
	 */
	public static String getPropertyString(Object bean, String property)
	{
		Object o = getProperty(bean, property);
		return (o!=null) ? o.toString() : null;
	}


//	/**
//	 * Zugriff auf ein Property
//	 * @param bean
//	 * @param property
//	 * @return Property als Date oder null
//	 */
//	public static Date getPropertyDate(Object bean, String property)
//	{
//		Object o = getProperty(bean, property);
//		if (o==null) return null;
//		if (o instanceof Date) return (Date)o;
//		if (o instanceof String) return DateUtil.parse((String)o);
//		return null;
//	}


	/**
	 * Zugriff auf ein Property
	 * @param bean
	 * @param property
	 * @return Property als Boolean oder null
	 */
	public static Boolean getPropertyBoolean(Object bean, String property)
	{
		Object o = getProperty(bean, property);
		if (o==null) return null;
		if (o instanceof Boolean) return (Boolean)o;
		if (o instanceof String) return Boolean.parseBoolean((String)o);
		return null;
	}

}
