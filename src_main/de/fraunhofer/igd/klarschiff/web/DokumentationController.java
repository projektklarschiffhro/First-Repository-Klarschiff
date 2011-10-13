package de.fraunhofer.igd.klarschiff.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class DokumentationController {
	
	@RequestMapping(value="/dokumentation/index", method = RequestMethod.GET)
    public String dokumentation(Model model, HttpServletRequest request) {
		return "dokumentation/index";
	}

	@RequestMapping(value="/dokumentation/benutzer", method = RequestMethod.GET)
	public String dokumentationBenutzer(Model model, HttpServletRequest request) {
		return "dokumentation/benutzer";
	}
	
	@RequestMapping(value="/dokumentation/admin", method = RequestMethod.GET)
	public String dokumentationAdmin(Model model, HttpServletRequest request) {
		return "dokumentation/admin";
	}
	
	@RequestMapping(value="/dokumentation/entwickler", method = RequestMethod.GET)
	public String dokumentationEntwickler(Model model, HttpServletRequest request) {
		return "dokumentation/entwickler";
	}
	
	@RequestMapping(value="/dokumentation/api", method = RequestMethod.GET)
	public String dokumentationApidoc(Model model, HttpServletRequest request) {
		return "dokumentation/api";
	}
	
}
