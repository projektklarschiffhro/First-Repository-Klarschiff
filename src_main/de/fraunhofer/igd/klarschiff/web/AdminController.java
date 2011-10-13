package de.fraunhofer.igd.klarschiff.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import de.fraunhofer.igd.klarschiff.dao.JobDao;
import de.fraunhofer.igd.klarschiff.service.dbsync.DbSyncService;
import de.fraunhofer.igd.klarschiff.service.geo.GeoService;
import de.fraunhofer.igd.klarschiff.service.job.JobsService;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;

@RequestMapping("/admin")
@Controller
public class AdminController {
	
	@Autowired
	SecurityService securityService;
	
	@Autowired
	JobsService jobsService;
	
	@Autowired
	DbSyncService dbSyncService;
	
	@Autowired
	JobDao jobDao;
	
	@Autowired
	GeoService geoService;
	
	@RequestMapping(value="/uebersicht", method = RequestMethod.GET)
    public String uebersicht(Model model, HttpServletRequest request) {
		return "admin/uebersicht";
	}

	@RequestMapping(value="/benutzer", method = RequestMethod.GET)
	public String benutzer(Model model, HttpServletRequest request) {
		model.addAttribute("benutzer", securityService.getAllUser());
		return "admin/benutzer";
	}
	
	@RequestMapping(value="/rollen", method = RequestMethod.GET)
	public String rollen(Model model, HttpServletRequest request) {
		model.addAttribute("rollenIntern", securityService.getAllZustaendigkeiten(true));
		model.addAttribute("rollenExtern", securityService.getAllDelegiertAn());
		return "admin/rollen";
	}

	@RequestMapping(value="/status", method = RequestMethod.GET)
	public String status(Model model, HttpServletRequest request) {
		model.addAttribute("wfs", geoService.getDataStore()!=null);
		model.addAttribute("anzahlAbgeschlosseneJobs", jobDao.getAnzahlAbgeschlosseneJobs());
		model.addAttribute("fehlerhafteJobs", jobDao.getFehlerhafteJobs());
		return "admin/status";
	}
	
	@RequestMapping(value="/test", method = RequestMethod.GET)
	public String test(Model model, HttpServletRequest request) {
		return "admin/test";
	}

	@RequestMapping(value="/test", method = RequestMethod.POST)
	public String testPost(Model model, @RequestParam(value = "action", required = true) String action, HttpServletRequest request) {
		if(action.equalsIgnoreCase("archivVorgaenge")) {
			jobsService.archivVorgaenge();
		} else if(action.equalsIgnoreCase("removeUnbestaetigtVorgang")) {
			jobsService.removeUnbestaetigtVorgang();
		} else if(action.equalsIgnoreCase("removeUnbestaetigtUnterstuetzer")) {
			jobsService.removeUnbestaetigtUnterstuetzer();
		} else if(action.equalsIgnoreCase("removeUnbestaetigtMissbrauchsmeldung")) {
			jobsService.removeUnbestaetigtMissbrauchsmeldung();
		} else if(action.equalsIgnoreCase("reBuildClassifier")) {
			jobsService.reBuildClassifier();
		} else if(action.equalsIgnoreCase("informExtern")) {
			jobsService.informExtern();
		} else if(action.equalsIgnoreCase("informDispatcher")) {
			jobsService.informDispatcher();
		} else if(action.equalsIgnoreCase("informErsteller")) {
			jobsService.informErsteller();
		}
		return "admin/test";
	}

	@RequestMapping(value="/datenbank", method = RequestMethod.GET)
	public String datenbank(Model model, HttpServletRequest request) {
		return "admin/datenbank";
	}

	@RequestMapping(value="/datenbank", method = RequestMethod.POST)
	public String datenbankPost(Model model, @RequestParam(value = "action", required = true) String action, HttpServletRequest request) {
		if(action.equalsIgnoreCase("initDbLink")) {
			dbSyncService.initDbLink(true);
		} else if(action.equalsIgnoreCase("viewInitDbLinkScript")) {
			model.addAttribute("initDbLinkScript", dbSyncService.getInitSqlScript());
		} else if(action.equalsIgnoreCase("syncDb")) {
			model.addAttribute("syncDbResult", dbSyncService.syncDb());
		}
		return "admin/datenbank";
	}
}
