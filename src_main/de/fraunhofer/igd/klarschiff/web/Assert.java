package de.fraunhofer.igd.klarschiff.web;

import static de.fraunhofer.igd.klarschiff.util.BeanUtil.getProperty;
import static de.fraunhofer.igd.klarschiff.util.BeanUtil.getPropertyString;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindingResult;

public class Assert
{
	public enum EvaluateOn { ever, firstError, firstPropertyError }


//	/**
//	 *
//	 * @param bean
//	 * @param context
//	 * @param evaluateOn
//	 * @param property
//	 * @param errorMessage
//	 */
//	public static void assertNotEmpty(Object bean, ValidationContext context, EvaluateOn evaluateOn, String property, String errorMessage)
//	{
//		if (!evaluate(evaluateOn, context, property)) return;
//		if (isEmpty(getProperty(bean, property))) addErrorMessage(context, property, errorMessage);
//	}

	/**
	 *
	 * @param bean
	 * @param context
	 * @param evaluateOn
	 * @param property
	 * @param errorMessage
	 */
	public static void assertNotEmpty(Object bean, BindingResult result, EvaluateOn evaluateOn, String property, String errorMessage)
	{
		if (!evaluate(evaluateOn, result, property)) return;
		if (isEmpty(getProperty(bean, property))) addErrorMessage(result, property, errorMessage);
	}

	public static void assertNotEmpty(Object bean, BindingResult result, EvaluateOn evaluateOn, String property, String errorProperty, String errorMessage)
	{
		if (!evaluate(evaluateOn, result, errorProperty)) return;
		if (isEmpty(getProperty(bean, property))) addErrorMessage(result, errorProperty, errorMessage);
	}

	public static void assertEquals(Object bean, BindingResult result, EvaluateOn evaluateOn, String property, Object value, String errorProperty, String errorMessage)
	{
		if (!evaluate(evaluateOn, result, errorProperty)) return;
		if (!isEquals(getProperty(bean, property), value)) addErrorMessage(result, errorProperty, errorMessage);
	}

//	/**
//	 *
//	 * @param bean
//	 * @param context
//	 * @param evaluateOn
//	 * @param property1
//	 * @param property2
//	 * @param errorMessage
//	 */
//	public static void assertLessEqualsDate(Object bean, ValidationContext context, EvaluateOn evaluateOn, String property1, String property2, String propertyError, String errorMessage)
//	{
//		if (!evaluate(evaluateOn, context, property1)) return;
//		if (!evaluate(evaluateOn, context, property2)) return;
//		if (isEmpty(getProperty(bean, property1))) return;
//		if (isEmpty(getProperty(bean, property2))) return;
//
//		try {
//			if (getPropertyDate(bean, property1).getTime()>getPropertyDate(bean, property2).getTime()) throw new Exception();
//		} catch (Exception e) {
//			addErrorMessage(context, propertyError, errorMessage);
//			addErrorMessage(context, property1, errorMessage);
//			addErrorMessage(context, property2, errorMessage);
//		}
//	}


//	/**
//	 *
//	 * @param bean
//	 * @param context
//	 * @param evaluateOn
//	 * @param property1
//	 * @param property2
//	 * @param errorMessage
//	 */
//	public static void assertLessEqualsDate(Object bean, BindingResult result, EvaluateOn evaluateOn, String property1, String property2, String propertyError, String errorMessage)
//	{
//		if (!evaluate(evaluateOn, result, property1)) return;
//		if (!evaluate(evaluateOn, result, property2)) return;
//		if (isEmpty(getProperty(bean, property1))) return;
//		if (isEmpty(getProperty(bean, property2))) return;
//
//		try {
//			if (getPropertyDate(bean, property1).getTime()>getPropertyDate(bean, property2).getTime()) throw new Exception();
//		} catch (Exception e) {
//			addErrorMessage(result, propertyError, errorMessage);
//			addErrorMessage(result, property1, errorMessage);
//			addErrorMessage(result, property2, errorMessage);
//		}
//	}

	/**
	 *
	 * @param bean
	 * @param context
	 * @param evaluateOn
	 * @param property1
	 * @param property2
	 * @param propertyError
	 * @param errorMessage
	 */
	public static void assertEqualsPassword(Object bean, BindingResult result, EvaluateOn evaluateOn, String property1, String property2, String propertyError, String errorMessage)
	{
		if (!evaluate(evaluateOn, result, property1)) return;
		if (!evaluate(evaluateOn, result, property2)) return;
		if (isEmpty(getProperty(bean, property1))) return;
		if (isEmpty(getProperty(bean, property2))) return;

		try {
			if (!getProperty(bean, property1).equals(getProperty(bean, property2))) throw new Exception();
		} catch (Exception e) {
			addErrorMessage(result, propertyError, errorMessage);
			addErrorMessage(result, property1, errorMessage);
			addErrorMessage(result, property2, errorMessage);
		}
	}

//	/**
//	 *
//	 * @param bean
//	 * @param context
//	 * @param evaluateOn
//	 * @param property
//	 * @param errorMessage
//	 */
//	public static void assertPlz(Object bean, ValidationContext context, EvaluateOn evaluateOn, String property, String errorMessage)
//	{
//		assertPattern(bean, context, evaluateOn, property, "\\d\\d\\d\\d\\d[\\s]*", errorMessage);
//	}

	public static void assertMaxLength(Object bean, BindingResult result, EvaluateOn evaluateOn, String property, int length, String errorMessage)
	{
		if (!evaluate(evaluateOn, result, property)) return;
		if (isEmpty(getProperty(bean, property))) return;
		try {
			if (getPropertyString(bean, property).length()>length) throw new Exception();
		} catch (Exception e) {
			addErrorMessage(result, property, errorMessage);
		}
		
	}
	
	/**
	 *
	 * @param bean
	 * @param context
	 * @param evaluateOn
	 * @param property
	 * @param errorMessage
	 */
	public static void assertPlz(Object bean, BindingResult result, EvaluateOn evaluateOn, String property, String errorMessage)
	{
		assertPattern(bean, result, evaluateOn, property, "\\d\\d\\d\\d\\d[\\s]*", errorMessage);
	}


//	/**
//	 *
//	 * @param bean
//	 * @param context
//	 * @param evaluateOn
//	 * @param property
//	 * @param errorMessage
//	 */
//	public static void assertEmail(Object bean, ValidationContext context, EvaluateOn evaluateOn, String property, String errorMessage)
//	{
//		assertPattern(bean, context, evaluateOn, property, ".+@.+\\.[a-z]+", errorMessage);
//	}


	/**
	 *
	 * @param bean
	 * @param context
	 * @param evaluateOn
	 * @param property
	 * @param errorMessage
	 */
	public static void assertEmail(Object bean, BindingResult result, EvaluateOn evaluateOn, String property, String errorMessage)
	{
//		assertPattern(bean, result, evaluateOn, property, ".+@.+\\.[a-z]+", errorMessage);
		assertPattern(bean, result, evaluateOn, property, "[a-z0-9\\.-]+@[a-z0-9\\.-]+\\.[a-z]+", errorMessage);
	}


//	/**
//	 *
//	 * @param bean
//	 * @param context
//	 * @param evaluateOn
//	 * @param property
//	 * @param errorMessage
//	 * ^((\\+[0-9]{2,4}( [0-9]+? | ?\\([0-9]+?\\) ?))|(\\(0[0-9 ]+?\\) ?)|(0[0-9]+? ?( |-|\\/) ?))[0-9]+?[0-9 \\/-]*[0-9]$
//	 * [\\d\\s\\(\\)-+/]*
//	 */
//	public static void assertTelephone(Object bean, ValidationContext context, EvaluateOn evaluateOn, String property, String errorMessage)
//	{
//		assertPattern(bean, context, evaluateOn, property, "[\\d\\s\\(\\)\\-\\+\\/]*", errorMessage);
//	}


	/**
	 *
	 * @param bean
	 * @param context
	 * @param evaluateOn
	 * @param property
	 * @param errorMessage
	 */
	public static void assertTelephone(Object bean, BindingResult result, EvaluateOn evaluateOn, String property, String errorMessage)
	{
		assertPattern(bean, result, evaluateOn, property, "[\\d\\s\\(\\)\\-\\+\\/]*", errorMessage);
	}


//	/**
//	 *
//	 * @param bean
//	 * @param context
//	 * @param evaluateOn
//	 * @param property
//	 * @param errorMessage
//	 */
//	public static void assertCaptcha(BindingResult result, EvaluateOn evaluateOn, HttpServletRequest request, String privateKey, String propertyError, String errorMessage)
//	{
//		if (!evaluate(evaluateOn, result, propertyError)) return;
//
//		String remoteAddr = request.getRemoteAddr();
//        ReCaptchaImpl reCaptcha = new ReCaptchaImpl();
//        reCaptcha.setPrivateKey(privateKey);
//
//        String challenge = request.getParameter("recaptcha_challenge_field");
//        String uresponse = request.getParameter("recaptcha_response_field");
//        ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer(remoteAddr, challenge, uresponse);
//
//        if (!reCaptchaResponse.isValid())
//        	addErrorMessage(result, propertyError, errorMessage);
//	}


//	/**
//	 *
//	 * @param bean
//	 * @param context
//	 * @param evaluateOn
//	 * @param property
//	 * @param pattern
//	 * @param errorMessage
//	 */
//	public static void assertPattern(Object bean, ValidationContext context, EvaluateOn evaluateOn, String property, String pattern, String errorMessage)
//	{
//		if (!evaluate(evaluateOn, context, property)) return;
//		if (isEmpty(getProperty(bean, property))) return;
//		try {
//			if (!matches(getPropertyString(bean, property), pattern)) throw new Exception();
//		} catch (Exception e) {
//			addErrorMessage(context, property, errorMessage);
//		}
//	}


	/**
	 *
	 * @param bean
	 * @param context
	 * @param evaluateOn
	 * @param property
	 * @param pattern
	 * @param errorMessage
	 */
	public static void assertPattern(Object bean, BindingResult result, EvaluateOn evaluateOn, String property, String pattern, String errorMessage)
	{
		if (!evaluate(evaluateOn, result, property)) return;
		if (isEmpty(getProperty(bean, property))) return;
		try {
			if (!matches(getPropertyString(bean, property), pattern)) throw new Exception();
		} catch (Exception e) {
			addErrorMessage(result, property, errorMessage);
		}
	}


	/* ########################### Hilfsfunktionen zum validieren ***************************** */

	/**
	 * Testet eine String nach einem vorgegebenen regulären Ausdruck
	 * @param str
	 * @param pattern
	 * @return
	 */
	private static boolean matches(String str, String pattern)
	{
       Pattern p = Pattern.compile(pattern);
       Matcher m = p.matcher(str);
       return m.matches();
	}


	/**
	 * Testet ob ein Object leer ist. String nur aus Leerzeichen oder leere Collection sind dabei ebenfalls leer
	 * @param o
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static boolean isEmpty(Object o)
	{
		if (o==null) return true;
		if (o instanceof String)
			if (StringUtils.isBlank((String)o)) return true;
			else return false;
		if (o instanceof Collection)
			if (((Collection)o).isEmpty()) return true;
			else return false;
		if (o instanceof Object[])
			if (((Object[])o).length==0) return true;
			else return false;
		return false;
	}

	private static boolean isEquals(Object o, Object value)
	{
		if (o==value) return true;
		if (o==null || value==null) return false;
		return o.equals(value);
	}

	/**
	 *
	 * @param bean
	 * @param property
	 * @return
	 */
	public static boolean isEmpty(Object bean, String property)
	{
		return isEmpty(getProperty(bean, property));
	}


	/* ########################### Prüfung, ob eine Validierung stattfinden soll ***************************** */


//	/**
//	 * prï¿½ft ob eine Validierung notwendig ist
//	 * @param evaluateOn
//	 * @param context
//	 * @param property
//	 * @return
//	 */
//	private static boolean evaluate(EvaluateOn evaluateOn, ValidationContext context, String property)
//	{
//		switch(evaluateOn) {
//			case ever:
//				return true;
//			case firstError:
//				return !context.getMessageContext().hasErrorMessages();
//			case firstPropertyError:
//				return context.getMessageContext().getMessagesBySource(property).length==0;
//		}
//		return true;
//	}

	/**
	 * prüft ob eine Validierung notwendig ist
	 * @param evaluateOn
	 * @param result
	 * @param property
	 * @return
	 */
	private static boolean evaluate(EvaluateOn evaluateOn, BindingResult result, String property)
	{
		switch(evaluateOn) {
			case ever:
				return true;
			case firstError:
				return !result.hasErrors();
			case firstPropertyError:
				return !result.hasFieldErrors(property);
		}
		return true;
	}

//	public static boolean hasError(ValidationContext context)
//	{
//		return context.getMessageContext().hasErrorMessages();
//	}
//	public static boolean hasError(ValidationContext context, String property)
//	{
//		return context.getMessageContext().getMessagesBySource(property).length!=0;
//	}
	public static boolean hasError(BindingResult result)
	{
		return result.hasErrors();
	}
	public static boolean hasError(BindingResult result, String property)
	{
		return result.hasFieldErrors(property);
	}


	/* ########################### Hinzufügen von Fehlernachrichten ***************************** */

//	/**
//	 * Fï¿½gt eine Fehlernachricht dem ValidationContext hinzu
//	 * @param context
//	 * @param property
//	 * @param errorMessage
//	 */
//	public static void addErrorMessage(ValidationContext context, String property, String errorMessage)
//	{
//		context.getMessageContext().addMessage(new MessageBuilder().error().source(property).defaultText(errorMessage==null ? "ERROR" : errorMessage).build());
//	}

	/**
	 * Fügt eine Fehlernachricht dem BindingResult hinzu
	 * @param result
	 * @param property
	 * @param errorMessage
	 */
	public static void addErrorMessage(BindingResult result, String property, String errorMessage)
	{
		result.rejectValue(property, "error" ,errorMessage==null ? "Error" : errorMessage);
	}

	public static void addErrorMessage(BindingResult result, EvaluateOn evaluateOn, String property, String errorMessage)
	{
		if (!evaluate(evaluateOn, result, property)) return;
		result.rejectValue(property, "error" ,errorMessage==null ? "Error" : errorMessage);
	}

	/* ########################### Zugriff auf die Properties ***************************** */

//	/**
//	 * Zugriff auf ein Property
//	 * @param bean
//	 * @param property
//	 * @return Property als Object oder null
//	 */
//	public static Object getProperty(Object bean, String property)
//	{
//		ExpressionParser parser = new SpelExpressionParser();
//		Expression exp = parser.parseExpression(property);
//		EvaluationContext context = new StandardEvaluationContext(bean);
//		return exp.getValue(context);
//	}
//
//
//	/**
//	 * Zugriff auf ein Property
//	 * @param bean
//	 * @param property
//	 * @return Property als String oder null
//	 */
//	public static String getPropertyString(Object bean, String property)
//	{
//		Object o = getProperty(bean, property);
//		return (o!=null) ? o.toString() : null;
//	}
//
//
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
//
//
//	/**
//	 * Zugriff auf ein Property
//	 * @param bean
//	 * @param property
//	 * @return Property als Boolean oder null
//	 */
//	public static Boolean getPropertyBoolean(Object bean, String property)
//	{
//		Object o = getProperty(bean, property);
//		if (o==null) return null;
//		if (o instanceof Boolean) return (Boolean)o;
//		if (o instanceof String) return Boolean.parseBoolean((String)o);
//		return null;
//	}
}
