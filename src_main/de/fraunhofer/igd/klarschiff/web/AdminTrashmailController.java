package de.fraunhofer.igd.klarschiff.web;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.fraunhofer.igd.klarschiff.dao.TrashmailDao;
import de.fraunhofer.igd.klarschiff.vo.Trashmail;

@RequestMapping("/admin")
@Controller
@SessionAttributes("cmd")
public class AdminTrashmailController {
	
	@Autowired
	TrashmailDao trashmailDao;
	
	
	@RequestMapping(value="/trashmail", method = RequestMethod.GET)
	public String trashmail(Model model, HttpServletRequest request){
		AdminTrashmailCommand cmd = new AdminTrashmailCommand();
		String trashmailStr = "";
		for(Trashmail trashmail : trashmailDao.findAllTrashmail()) trashmailStr+=trashmail.getPattern()+"\n";
		cmd.setTrashmailStr(trashmailStr);
		model.addAttribute("cmd", cmd);
		return "admin/trashmail";
	}
	
	@RequestMapping(value="/trashmail", method = RequestMethod.POST)
	public String trashmailSubmit(@ModelAttribute(value = "cmd") AdminTrashmailCommand cmd, 
    		BindingResult result, 
    		Model model, 
    		HttpServletRequest request) {
		
		trashmailDao.removeAll();
		
		for (String pattern : cmd.getTrashmailStr().split("\n")) {
			if (!StringUtils.isBlank(pattern)) {
				Trashmail trashmail = new Trashmail();
				trashmail.setPattern(pattern.trim());
				trashmailDao.persist(trashmail);
			}
		}
		return trashmail(model, request);
	}
	
}
