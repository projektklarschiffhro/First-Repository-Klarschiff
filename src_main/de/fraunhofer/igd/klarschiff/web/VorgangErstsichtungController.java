package de.fraunhofer.igd.klarschiff.web;

import static de.fraunhofer.igd.klarschiff.web.Assert.assertNotEmpty;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.service.classification.ClassificationService;
import de.fraunhofer.igd.klarschiff.service.geo.GeoService;
import de.fraunhofer.igd.klarschiff.service.image.ImageService;
import de.fraunhofer.igd.klarschiff.service.security.Role;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.vo.EnumFreigabeStatus;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangStatus;
import de.fraunhofer.igd.klarschiff.vo.EnumZustaendigkeitStatus;
import de.fraunhofer.igd.klarschiff.vo.Verlauf;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;

@SessionAttributes("cmd")
@RequestMapping("/vorgang")
@Controller
public class VorgangErstsichtungController {

	@Autowired
	VorgangDao vorgangDao;

	//@Autowired
	//VerlaufDao verlaufDao;

	@Autowired
	ClassificationService classificationService;
	
	@Autowired
	SecurityService securityService;
		
	@Autowired
	GeoService geoService;
	
	@Autowired
	ImageService imageService;

	@ModelAttribute("currentZustaendigkeiten")
    public List<Role> currentZustaendigkeiten() {
        return securityService.getCurrentZustaendigkeiten(false);
    }
	
	@ModelAttribute("allZustaendigkeiten")
    public List<Role> allZustaendigkeiten() {
        return securityService.getAllZustaendigkeiten(false);
    }
	
	@ModelAttribute("geoService")
    public GeoService getGeoService() {
        return geoService;
    }
	
	@RequestMapping(value="/{id}/erstsichtung", method = RequestMethod.GET)
    public String erstsichtung(@PathVariable("id") Long id, ModelMap model, HttpServletRequest request) {

		Vorgang vorgang = vorgangDao.findVorgang(id);
		
		for (@SuppressWarnings("unused") Verlauf verlauf : vorgang.getVerlauf());
		
		VorgangErstsichtungCommand cmd = new VorgangErstsichtungCommand();
		cmd.setVorgang(vorgang);
		model.put("cmd", cmd);
		model.put("mapExternUrl", geoService.getMapExternUrl(vorgang));
		
		return "vorgang/erstsichtung/zustaendigkeit";
	}
	
	@RequestMapping(value="/{id}/erstsichtung", method = RequestMethod.POST)
    public String erstsichtungSubbmit(
    		@ModelAttribute(value = "cmd") VorgangErstsichtungCommand cmd, 
    		BindingResult result, 
    		@PathVariable("id") Long id, 
    		@RequestParam(value = "action", required = true) String action, 
			@RequestParam(value="censorRectangles", required=false) String censorRectangleString,
			@RequestParam(value="censoringWidth", required=false) Integer censoringWidth,
			@RequestParam(value="censoringHeight", required=false) Integer censoringHeight,
    		ModelMap model, 
    		HttpServletRequest request) {
		
		action = StringEscapeUtils.escapeHtml(action);
		
		if (action.equals("zuweisen")) {
			
			assertNotEmpty(cmd, result, Assert.EvaluateOn.ever, "vorgang.zustaendigkeit", null);
			if (result.hasErrors()) {
				cmd.getVorgang().setZustaendigkeit(vorgangDao.findVorgang(id).getZustaendigkeit());
				model.put("mapExternUrl", geoService.getMapExternUrl(cmd.getVorgang()));
	            return "vorgang/erstsichtung/zustaendigkeit";
	        }

			//verlaufDao.addVerlaufToVorgang(cmd.getVorgang(), EnumVerlaufTyp.zustaendigkeit, vorgangDao.findVorgang(id).getZustaendigkeit(), cmd.getVorgang().getZustaendigkeit());
			vorgangDao.merge(cmd.getVorgang());
			
			return "vorgang/erstsichtung/zustaendigkeitZugewiesen";

		} else if (action.equals("neu zuweisen")) {
			
			cmd.getVorgang().setZustaendigkeit(classificationService.calculateZustaendigkeitforVorgang(cmd.getVorgang()).getId());

			//verlaufDao.addVerlaufToVorgang(cmd.getVorgang(), EnumVerlaufTyp.zustaendigkeit, vorgangDao.findVorgang(id).getZustaendigkeit(), cmd.getVorgang().getZustaendigkeit());

			vorgangDao.merge(cmd.getVorgang());

			return "vorgang/erstsichtung/zustaendigkeitZugewiesen";
		
		} else if (action.equals("akzeptieren")) {
			
			cmd.getVorgang().setZustaendigkeitStatus(EnumZustaendigkeitStatus.akzeptiert);
			
			//verlaufDao.addVerlaufToVorgang(cmd.getVorgang(), EnumVerlaufTyp.zustaendigkeitAkzeptiert, null, null);
			
			return "vorgang/erstsichtung/pruefen";

		} else if (action.equals("&uuml;bernehmen und akzeptieren")) {

			assertNotEmpty(cmd, result, Assert.EvaluateOn.ever, "vorgang.zustaendigkeit", null);
			if (result.hasErrors()) {
				cmd.getVorgang().setZustaendigkeit(vorgangDao.findVorgang(id).getZustaendigkeit());
				model.put("mapExternUrl", geoService.getMapExternUrl(cmd.getVorgang()));
	            return "vorgang/erstsichtung/zustaendigkeit";
	        }
			cmd.getVorgang().setZustaendigkeitStatus(EnumZustaendigkeitStatus.akzeptiert);
			
			//verlaufDao.addVerlaufToVorgang(cmd.getVorgang(), EnumVerlaufTyp.zustaendigkeit, vorgangDao.findVorgang(id).getZustaendigkeit(), cmd.getVorgang().getZustaendigkeit());
			//verlaufDao.addVerlaufToVorgang(cmd.getVorgang(), EnumVerlaufTyp.zustaendigkeitAkzeptiert, null, null);
			
			return "vorgang/erstsichtung/pruefen";
		
		} else if (action.equals("Pr&uuml;fung abschlie&szlig;en")) {
		
			//Verlauf? betreff u/o freigabestatus ge�ndert
			//Verlauf? details u/o freigabestatus ge�ndert
			//Verlauf? foto u/o freigabestatus ge�ndert
			
			//Verlauf verlauf = verlaufDao.addVerlaufToVorgang(cmd.getVorgang(), EnumVerlaufTyp.status, cmd.getVorgang().getStatus().name(), null);
			cmd.getVorgang().setStatus(EnumVorgangStatus.inBearbeitung);
			//verlauf.setWertNeu(cmd.getVorgang().getStatus().name());
			
			vorgangDao.merge(cmd.getVorgang());
			
			return "redirect:/vorgang/" + id + "/uebersicht";
		
		} else if (action.equals("fotoSave")) {

			imageService.censorImageForVorgang(cmd.getVorgang(), censorRectangleString, censoringWidth, censoringHeight);
			vorgangDao.merge(cmd.getVorgang());
			cmd.setVorgang(vorgangDao.findVorgang(id));
			for (@SuppressWarnings("unused") Verlauf verlauf : cmd.getVorgang().getVerlauf());
			return "vorgang/erstsichtung/pruefen";
			
		} else if (action!=null && action.startsWith("freigabeStatus")) {
			String str[] = action.split("_");
			EnumFreigabeStatus freigabeStatus = EnumFreigabeStatus.valueOf(str[2]);
			if (str[1].equals("Betreff")) cmd.getVorgang().setBetreffFreigabeStatus(freigabeStatus);
			else if (str[1].equals("Details")) cmd.getVorgang().setDetailsFreigabeStatus(freigabeStatus);
			else if (str[1].equals("Foto")) cmd.getVorgang().setFotoFreigabeStatus(freigabeStatus);
			return "vorgang/erstsichtung/pruefen";
		}		

		throw new RuntimeException("unbekannte Action: " + action);
	}

}
