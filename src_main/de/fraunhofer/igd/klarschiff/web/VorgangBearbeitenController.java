package de.fraunhofer.igd.klarschiff.web;

import static de.fraunhofer.igd.klarschiff.web.Assert.assertMaxLength;
import static de.fraunhofer.igd.klarschiff.web.Assert.assertNotEmpty;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.persistence.Transient;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.fraunhofer.igd.klarschiff.dao.KategorieDao;
import de.fraunhofer.igd.klarschiff.dao.KommentarDao;
import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.service.classification.ClassificationService;
import de.fraunhofer.igd.klarschiff.service.security.Role;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.vo.EnumFreigabeStatus;
import de.fraunhofer.igd.klarschiff.vo.EnumPrioritaet;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangStatus;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangTyp;
import de.fraunhofer.igd.klarschiff.vo.EnumZustaendigkeitStatus;
import de.fraunhofer.igd.klarschiff.vo.Kommentar;
import de.fraunhofer.igd.klarschiff.vo.StatusKommentarVorlage;
import de.fraunhofer.igd.klarschiff.vo.Verlauf;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;
import de.fraunhofer.igd.klarschiff.vo.VorgangHistoryClasses;

@SessionAttributes("cmd")
@Controller
public class VorgangBearbeitenController {

	Logger logger = Logger.getLogger(VorgangBearbeitenController.class);
	
	@Autowired
	VorgangDao vorgangDao;

	@Autowired
	KommentarDao kommentarDao;
	
	@Autowired
	KategorieDao kategorieDao;
	
	@Autowired
	ClassificationService classificationService;
	
	@Autowired
	SecurityService securityService;
		
	@ModelAttribute("currentZustaendigkeiten")
    public List<Role> currentZustaendigkeiten() {
        return securityService.getCurrentZustaendigkeiten(false);
    }
	
	@ModelAttribute("allZustaendigkeiten")
    public List<Role> allZustaendigkeiten() {
        return securityService.getAllZustaendigkeiten(false);
    }
	
	@ModelAttribute("allDelegiertAn")
	public List<Role> allDelegiertAn() {
		return securityService.getAllDelegiertAn();
	}
	
	@ModelAttribute("allVorgangStatus")
    public EnumVorgangStatus[] allVorgangStatus() {
		EnumVorgangStatus[] allVorgangStatus = EnumVorgangStatus.values();
		allVorgangStatus = (EnumVorgangStatus[])ArrayUtils.removeElement(ArrayUtils.removeElement(allVorgangStatus, EnumVorgangStatus.gemeldet), EnumVorgangStatus.offen);
        return allVorgangStatus;
    }

	@ModelAttribute("vorgangtypen")
    public Collection<EnumVorgangTyp> populateEnumVorgangTypen() {
        return Arrays.asList(EnumVorgangTyp.values());
    }
	
	@ModelAttribute("allPrioritaet")
    public Collection<EnumPrioritaet> allPrioritaet() {
        return Arrays.asList(EnumPrioritaet.values());
    }

	@ModelAttribute("allStatusKommentarVorlage")
    public List<StatusKommentarVorlage> allStatusKommentarVorlage() {
        return vorgangDao.findStatusKommentarVorlage();
    }

	private void updateKategorieInModel(ModelMap model, VorgangBearbeitenCommand cmd) {
		try {
			cmd.setKategorie(cmd.getVorgang().getKategorie().getParent());
			model.addAttribute("hauptkategorien", kategorieDao.findRootKategorienForTyp(cmd.getVorgang().getTyp()));
			model.addAttribute("unterkategorien", kategorieDao.findKategorie(cmd.getKategorie().getId()).getChildren());
		}catch (Exception e) {}
	}
	private void updateKommentarInModel(ModelMap model, VorgangBearbeitenCommand cmd) {
		try {
			model.addAttribute("kommentare", kommentarDao.findKommentareForVorgang(cmd.getVorgang(), cmd.getPage(), cmd.getSize()));
	    	model.put("maxPages", calculateMaxPages(cmd.getSize(), kommentarDao.countKommentare(cmd.getVorgang())));

		}catch (Exception e) {}
	}
	private void updateZustaendigkeitStatusInModel(ModelMap model, VorgangBearbeitenCommand cmd) {
		try {
			model.addAttribute("isDispatcherInVorgangHistoryClasses", classificationService.isDispatcherInVorgangHistoryClasses(cmd.getVorgang()));
		}catch (Exception e) {}
	}

	@RequestMapping(value="/vorgang/{id}/bearbeiten", method = RequestMethod.GET)
	public String bearbeiten(@PathVariable("id") Long id, ModelMap model, HttpServletRequest request) {
		VorgangBearbeitenCommand cmd = new VorgangBearbeitenCommand();
		cmd.setSize(5);
		cmd.setVorgang(getVorgang(id));
		model.put("cmd", cmd);
		updateKategorieInModel(model, cmd);
		updateKommentarInModel(model, cmd);
		updateZustaendigkeitStatusInModel(model, cmd);
		
		return (cmd.getVorgang().getStatus()==EnumVorgangStatus.gemeldet) ? "vorgang/bearbeitenDisabled" : "vorgang/bearbeiten";
	}

	@Transient
	private Vorgang getVorgang(Long id) {
		Vorgang vorgang = vorgangDao.findVorgang(id);
		for (@SuppressWarnings("unused") Verlauf verlauf : vorgang.getVerlauf());
		return vorgang;
	}
	
	@Transactional
	@RequestMapping(value="/vorgang/{id}/bearbeiten", method = RequestMethod.POST)
	public String bearbeitenSubbmit(
    		@ModelAttribute(value = "cmd") VorgangBearbeitenCommand cmd, 
    		BindingResult result, 
    		@PathVariable("id") Long id, 
    		@RequestParam(value = "action", required = true) String action, 
    		ModelMap model, 
    		HttpServletRequest request) {
		
		action = StringEscapeUtils.escapeHtml(action);

		
		assertNotEmpty(cmd, result, Assert.EvaluateOn.ever, "vorgang.zustaendigkeit", null);
		assertMaxLength(cmd, result,  Assert.EvaluateOn.ever, "vorgang.statusKommentar", 300, "Der Statuskommentar ist zu lang");
		if (result.hasErrors()) {
			cmd.setVorgang(getVorgang(id));
			updateKategorieInModel(model, cmd);
			updateKommentarInModel(model, cmd);
			updateZustaendigkeitStatusInModel(model, cmd);
			return "vorgang/bearbeiten";
		}			
		
		if (action.equals("akzeptieren")) {
			cmd.getVorgang().setZustaendigkeitStatus(EnumZustaendigkeitStatus.akzeptiert);
			vorgangDao.merge(cmd.getVorgang());
		} else if (action.equals("&uuml;bernehmen und akzeptieren")) {
//			cmd.getVorgang().setZustaendigkeitStatus(EnumZustaendigkeitStatus.zugewiesen);
//			vorgangDao.merge(cmd.getVorgang());
//			cmd.setVorgang(getVorgang(id));
			cmd.getVorgang().setZustaendigkeitStatus(EnumZustaendigkeitStatus.akzeptiert);
			vorgangDao.merge(cmd.getVorgang());
		} else if (action.equals("automatisch neu zuweisen")) {
			cmd.getVorgang().setZustaendigkeit(classificationService.calculateZustaendigkeitforVorgang(cmd.getVorgang()).getId());
			cmd.getVorgang().setZustaendigkeitStatus(EnumZustaendigkeitStatus.zugewiesen);
			vorgangDao.merge(cmd.getVorgang());
		} else if (action.equals("zuweisen")) {
			cmd.getVorgang().setZustaendigkeitStatus(EnumZustaendigkeitStatus.zugewiesen);
			vorgangDao.merge(cmd.getVorgang());
		} else if (action.equals("Status setzen")) {
			vorgangDao.merge(cmd.getVorgang());
		} else if (action.equals("freigabeStatus_Betreff_extern")) {
			cmd.getVorgang().setBetreffFreigabeStatus(EnumFreigabeStatus.extern);
			vorgangDao.merge(cmd.getVorgang());
		} else if (action.equals("freigabeStatus_Betreff_intern")) {
			cmd.getVorgang().setBetreffFreigabeStatus(EnumFreigabeStatus.intern);
			vorgangDao.merge(cmd.getVorgang());
		} else if (action.equals("freigabeStatus_Details_extern")) {
			cmd.getVorgang().setDetailsFreigabeStatus(EnumFreigabeStatus.extern);
			vorgangDao.merge(cmd.getVorgang());
		} else if (action.equals("freigabeStatus_Details_intern")) {
			cmd.getVorgang().setDetailsFreigabeStatus(EnumFreigabeStatus.intern);
			vorgangDao.merge(cmd.getVorgang());
		} else if (action.equals("Vorgangsdaten &auml;ndern")) {
			vorgangDao.merge(cmd.getVorgang());
		} else if (action.equals("Kommentar speichern")) {
			if (!StringUtils.isBlank(cmd.getKommentar())) {		
				Kommentar kommentar = new Kommentar();
				kommentar.setVorgang(cmd.getVorgang());
				kommentar.setText(cmd.getKommentar());
				kommentar.setNutzer(securityService.getCurrentUser().getName());
				kommentarDao.persist(kommentar);
				cmd.setKommentar(null);
			}
		} else if (action.equals("delegieren")) {
			vorgangDao.merge(cmd.getVorgang());
		} else if (action.equals("archivieren")) {
			cmd.getVorgang().setArchiviert(true);
			vorgangDao.merge(cmd.getVorgang());
		} else if (action.equals("wiederherstellen")) {
			cmd.getVorgang().setArchiviert(false);
			vorgangDao.merge(cmd.getVorgang());
		} else if (action.equals("setzen")) {
			vorgangDao.merge(cmd.getVorgang());
		} else if (action.equals("zur&uuml;cksetzen")) {
			VorgangHistoryClasses history = vorgangDao.findVorgangHistoryClasses(cmd.getVorgang());
			history.getHistoryClasses().clear();
			vorgangDao.merge(history);
		}

		cmd.setVorgang(getVorgang(id));
		updateKategorieInModel(model, cmd);
		updateKommentarInModel(model, cmd);
		updateZustaendigkeitStatusInModel(model, cmd);
		return "vorgang/bearbeiten";
	}

	private int calculateMaxPages(int size, long count)
    {
		float nrOfPages = (float) count / size;
		return (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages);
    }
	
}
