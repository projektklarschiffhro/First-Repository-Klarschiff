package de.fraunhofer.igd.klarschiff.web;

import static de.fraunhofer.igd.klarschiff.web.Assert.assertEmail;
import static de.fraunhofer.igd.klarschiff.web.Assert.assertMaxLength;
import static de.fraunhofer.igd.klarschiff.web.Assert.assertNotEmpty;

import java.io.OutputStream;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.service.mail.MailService;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;

@SessionAttributes("cmd")
@Controller
public class VorgangEmailController {

	Logger logger = Logger.getLogger(VorgangEmailController.class);
	
	@Autowired
	VorgangDao vorgangDao;
	
	@Autowired
	SecurityService securityService;

	@Autowired
	MailService mailService;
	
	@RequestMapping(value="/vorgang/{id}/email", method = RequestMethod.GET)
	public String email(@PathVariable("id") Long id, ModelMap model, HttpServletRequest request) {
		return email(id, model, request, false);
	}
	@RequestMapping(value="/vorgang/delegiert/{id}/email", method = RequestMethod.GET)
	public String emailDelegiert(@PathVariable("id") Long id, ModelMap model, HttpServletRequest request) {
		return email(id, model, request, true);
	}
	
	public String email(Long id, ModelMap model, HttpServletRequest request, boolean delegiert) {
		VorgangEmailCommand cmd = new VorgangEmailCommand();
		cmd.setVorgang(vorgangDao.findVorgang(id));
		cmd.setFromEmail(securityService.getCurrentUser().getEmail());
		cmd.setFromName(securityService.getCurrentUser().getName());
		if (delegiert) {
			cmd.setSendMissbrauchsmeldungen(false);
			model.put("delegiert", true);
		}
		model.put("cmd", cmd);
		
		return "noMenu/vorgang/printEmail/email";
	}
	
	
	@RequestMapping(value="/vorgang/{id}/email", method = RequestMethod.POST)
    public String emailSubmit(
    		@ModelAttribute(value = "cmd") VorgangEmailCommand cmd, 
    		BindingResult result, 
    		@PathVariable("id") Long id, 
    		ModelMap model, 
    		HttpServletRequest request) {
		return emailSubmit(cmd, result, id, model, request, false);
	}
	
	@RequestMapping(value="/vorgang/delegiert/{id}/email", method = RequestMethod.POST)
    public String emailDelegiertSubmit(
    		@ModelAttribute(value = "cmd") VorgangEmailCommand cmd, 
    		BindingResult result, 
    		@PathVariable("id") Long id, 
    		ModelMap model, 
    		HttpServletRequest request) {
		return emailSubmit(cmd, result, id, model, request, true);		
	}

    public String emailSubmit(
    		VorgangEmailCommand cmd, 
    		BindingResult result, 
    		Long id, 
    		ModelMap model, 
    		HttpServletRequest request,
    		boolean delegiert) {
		if (delegiert)
			model.put("delegiert", true);
		
		assertNotEmpty(cmd, result, Assert.EvaluateOn.ever, "toEmail", null);
		assertNotEmpty(cmd, result, Assert.EvaluateOn.ever, "text", null);
		assertMaxLength(cmd, result,  Assert.EvaluateOn.ever, "text", 300, null);
		assertEmail(cmd, result, Assert.EvaluateOn.firstPropertyError, "toEmail", null);
		if (result.hasErrors()) {
			return "noMenu/vorgang/printEmail/email";
		}			
		
		mailService.sendVorgangWeiterleitenMail(vorgangDao.findVorgang(id), cmd.getFromEmail(), cmd.getToEmail(), cmd.getText(), cmd.getSendKarte(), cmd.getSendKommentare(), cmd.getSendFoto(), cmd.getSendMissbrauchsmeldungen());
		return "noMenu/vorgang/printEmail/emailSubmit";
	}

	@RequestMapping(value="/vorgang/{id}/emailDirect")
	@ResponseBody
	public void emailDirect(@PathVariable("id") Long id, HttpServletResponse response) throws Exception {
		emailDirect(id, response, false);
	}

	@RequestMapping(value="/vorgang/delegiert/{id}/emailDirect")
	@ResponseBody
	public void emailDelegiertDirect(@PathVariable("id") Long id, HttpServletResponse response) throws Exception {
		emailDirect(id, response, true);
	}
	
	public void emailDirect(@PathVariable("id") Long id, HttpServletResponse response, boolean delegiert) throws Exception {
		String encoding = "UTF-8";
		Vorgang vorgang = vorgangDao.findVorgang(id);
		String mailText = mailService.composeVorgangWeiterleitenMail(vorgang, "", true, true, !delegiert);
		//mailText = StringUtil.encode(mailText, "UTF-8", encoding);
		//mailText = "%C3%BC"+mailText.replace(" ", "%20").replace("\n", "%20");
		//mailText = new URLCodec(encoding).encode(mailText).replace("+", "%20");
		mailText = URLEncoder.encode(mailText, encoding).replace("+", "%20");
		
		String mail = "mailto:?subject="+mailService.getVorgangWeiterleitenMailTemplate().getSubject()+"&body="+mailText;
		String content = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/><meta http-equiv=\"X-UA-Compatible\" content=\"IE=8\"/></head><body><script language=\"JavaScript\" type=\"text/javascript\">location.href='"+mail+"';window.close();</script></body></html>";
		logger.debug(content);
		response.setHeader("Content-Type", "text/html;charset="+encoding);
		OutputStream os = response.getOutputStream();
		os.write(content.getBytes(encoding));
		response.setStatus(HttpServletResponse.SC_OK);
		response.flushBuffer();
	}
}
