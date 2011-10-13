package de.fraunhofer.igd.klarschiff.web;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import de.fraunhofer.igd.klarschiff.context.AppContext;
import de.fraunhofer.igd.klarschiff.service.settings.SettingsService;

@Controller
public class FehlerController {
	private static final Logger logger = Logger.getLogger(FehlerController.class);
	
	@Autowired
	SettingsService settingsService;
	
    List<String> ignoredUserAgentForErrorLog = new ArrayList<String>(); 
    List<String> ignoredRequestPathRegexForErrorLog = new ArrayList<String>();
	
	@RequestMapping(value="/uncaughtException")
    public String uncaughtException(ModelMap model, HttpServletRequest request) {
		return processException(model, request, "uncaughtException");
	}
	
	@RequestMapping(value="/resourceNotFound")
    public String resourceNotFound(ModelMap model, HttpServletRequest request) {
		return processException(model, request, "resourceNotFound");
	}
	
	@RequestMapping(value="/dataAccessFailure")
    public String dataAccessFailure(ModelMap model, HttpServletRequest request) {
		return processException(model, request, "dataAccessFailure");
	}

	
	
    @SuppressWarnings("unchecked")
	private String processException(ModelMap model, HttpServletRequest request, String typ)
    {
    	//Informationen bei aufgetretenen Fehlerseiten loggen
    	Integer errorCode = (Integer)request.getAttribute("javax.servlet.error.status_code");
    	if (errorCode !=null && 
    		!contains(request.getHeader("user-agent"), ignoredUserAgentForErrorLog) && 
    		!containsRegex((String)request.getAttribute("javax.servlet.forward.request_uri"), ignoredRequestPathRegexForErrorLog))
    	{
    		//Daten ermitteln
    		String exceptionId = UUID.randomUUID().toString().replaceAll(":", "_");
    		Exception exception = (Exception)request.getAttribute("javax.servlet.error.exception");
    		String requestPath = (String)request.getAttribute("javax.servlet.forward.request_uri");
    		String queryString = (String)request.getAttribute("javax.servlet.forward.query_string");
    		String forwardRequestPath = (String)request.getAttribute("javax.servlet.forward.path_info");
    		String login = null;
    		try { login = SecurityContextHolder.getContext().getAuthentication().getName(); } catch (Exception e) {}
    		
    		//Fehlermeldung zusammensetzen
    		ByteArrayOutputStream bos = new ByteArrayOutputStream();
    		PrintWriter pw = new PrintWriter(bos, true);
    		pw.println("#################### ERROR_PAGE "+errorCode+" [BEGIN] ####################");

    		pw.println("  exceptionId="+exceptionId);

    		pw.println("  REQUEST");
    		pw.println("    method="+request.getMethod());
			pw.println("    requestPath="+requestPath);
			pw.println("    queryString="+queryString);
			for(Enumeration<String> iter=request.getParameterNames(); iter.hasMoreElements();)
			{
				String name = iter.nextElement();
				String value = request.getParameter(name);
				pw.println("    parameter: "+name+"="+value);
			}

			pw.println("  REQUESTHEADER");
			for(Enumeration<String> iter=request.getHeaderNames(); iter.hasMoreElements();)
			{
				String name = iter.nextElement();
				String value = request.getHeader(name);
				pw.println("    "+name+"="+value);
			}

    		pw.println("  REMOTE");
			pw.println("    remoteAddr="+request.getRemoteAddr());
			pw.println("    remoteHost="+request.getRemoteHost());
			pw.println("    remotePort="+request.getRemotePort());
			pw.println("    remoteUser="+request.getRemoteUser());

			pw.println("  forwardRequestPath="+forwardRequestPath);
			pw.println("  login="+login);

			//Exception
			if (exception!=null)
			{
				pw.println("\n---------- StackTrace ---------------------------\n");
				exception.printStackTrace(pw);
    		
				Throwable ex = exception;
				while(ex!=null) {
					//SQLException NextException
					if (ex instanceof SQLException && ((SQLException)ex).getNextException()!=null)
					{
						pw.println("\n---------- SQLException NextException ------------\n");
						((SQLException)ex).getNextException().printStackTrace(pw);
					}
					
					ex = ex.getCause();
				}
				pw.println("\n-------------------------------------------------\n");
			}

			//Fehlermeldung ins Log schreiben
    		String errorText = new String(bos.toByteArray());
    		pw.println("#################### ERROR_PAGE "+errorCode+" [END] ####################");
    		logger.error("\n\n"+errorText);

    		model.addAttribute("exceptionId", exceptionId);
    		model.addAttribute("exceptionText", errorText);
    		
    	}
    	model.addAttribute("showFehlerDetails", settingsService.getShowFehlerDetails());
    	model.addAttribute("bugTrackingUrl", settingsService.getBugTrackingUrl());
    	return typ;
    	
    }
    
	public static Map<String, Object> processException(Throwable exception) {
		Map<String, Object> map = new HashMap<String, Object>();

    	//Informationen bei aufgetretenen Fehlerseiten loggen
   		String exceptionId = UUID.randomUUID().toString().replaceAll(":", "_");
   		String login = null;
   		try { login = SecurityContextHolder.getContext().getAuthentication().getName(); } catch (Exception e) {}
    		
   		//Fehlermeldung zusammensetzen
   		ByteArrayOutputStream bos = new ByteArrayOutputStream();
   		PrintWriter pw = new PrintWriter(bos, true);
   		pw.println("#################### ERROR_PAGE [BEGIN] ####################");

		pw.println("  exceptionId="+exceptionId);
		pw.println("  login="+login);

		//Exception
		if (exception!=null)
		{
			pw.println("\n---------- StackTrace ---------------------------\n");
			exception.printStackTrace(pw);
		
			Throwable ex = exception;
			while(ex!=null) {
				//SQLException NextException
				if (ex instanceof SQLException && ((SQLException)ex).getNextException()!=null)
				{
					pw.println("\n---------- SQLException NextException ------------\n");
					((SQLException)ex).getNextException().printStackTrace(pw);
				}
				
				ex = ex.getCause();
			}
			pw.println("\n-------------------------------------------------\n");
		}

		pw.println("#################### ERROR_PAGE [END] ####################");
		String errorText = new String(bos.toByteArray());
		//Fehlermeldung ins Log schreiben
		logger.error("\n\n"+errorText);

    	map.put("exceptionId", exceptionId);
		map.put("exceptionText", errorText);

		SettingsService settingsService = AppContext.getApplicationContext().getBean(SettingsService.class);
		map.put("showFehlerDetails", settingsService.getShowFehlerDetails());
		map.put("bugTrackingUrl", settingsService.getBugTrackingUrl());
		return map;
	}

    private boolean contains(String str, List<String> strs)
    {
    	if (str==null || strs==null) return false;
    	for(String s : strs) {
    		if (str.contains(s)) return true;
    	}
    	return false;
    }
    
    private boolean containsRegex(String str, List<String> regex)
    {
    	if (str==null || regex==null) return false;
    	for(String reg : regex) {
    		if (str.matches(reg)) return true;
    	}
    	return false;
    }
    	
}
