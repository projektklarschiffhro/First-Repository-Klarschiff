package de.fraunhofer.igd.klarschiff.web;

import static de.fraunhofer.igd.klarschiff.web.Assert.addErrorMessage;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.fraunhofer.igd.klarschiff.dao.KategorieDao;
import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.service.classification.ClassificationService;
import de.fraunhofer.igd.klarschiff.service.geo.GeoService;
import de.fraunhofer.igd.klarschiff.service.image.ImageService;
import de.fraunhofer.igd.klarschiff.vo.EnumPrioritaet;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangStatus;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangTyp;
import de.fraunhofer.igd.klarschiff.vo.EnumZustaendigkeitStatus;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;

@SessionAttributes("cmd")
@RequestMapping("/vorgangneu")
@Controller
public class VorgangNeuController {
	public static Logger logger = Logger.getLogger(VorgangNeuController.class);

	@Autowired
	KategorieDao kategorieDao;

	@Autowired
	VorgangDao vorgangDao;

	//@Autowired
	//VerlaufDao verlaufDao;
	
	@Autowired
	ClassificationService classificationService;
	
	@Autowired
	ImageService imageService;
	
	@Autowired
	GeoService geoService;
	
	@ModelAttribute("vorgangtypen")
    public Collection<EnumVorgangTyp> populateEnumVorgangTypen() {
        return Arrays.asList(EnumVorgangTyp.values());
    }

	@ModelAttribute("geoService")
    public GeoService getGeoService() {
        return geoService;
    }
	
	private void updateKategorieInModel(ModelMap model, VorgangNeuCommand cmd) {
		try {
			model.addAttribute("hauptkategorien", kategorieDao.findRootKategorienForTyp(cmd.getVorgang().getTyp()));
			model.addAttribute("unterkategorien", kategorieDao.findKategorie(cmd.getKategorie().getId()).getChildren());
		}catch (Exception e) {}
	}
	
	@RequestMapping(method = RequestMethod.GET)
    public String form(ModelMap model) {
		VorgangNeuCommand cmd = new VorgangNeuCommand();
		cmd.getVorgang().setTyp(EnumVorgangTyp.problem);
        model.addAttribute("cmd", cmd);
        updateKategorieInModel(model, cmd);
        //addDateTimeFormatPatterns(model);
		return "vorgangneu/form";
	}

	@RequestMapping(method = RequestMethod.POST)
    public String submit(@ModelAttribute("cmd") VorgangNeuCommand cmd, BindingResult result, ModelMap model, HttpServletRequest request) {
		Vorgang vorgang = cmd.getVorgang();
		
		if (cmd.getBild()!=null && !cmd.getBild().isEmpty()) 
			try {
				imageService.setImageForVorgang(cmd.getBild(), cmd.getVorgang());
				cmd.setBild(null);
			} catch (Exception e) {
				addErrorMessage(result, "bild", e.getMessage());
			}
			
		cmd.validate(result, kategorieDao);

		if (result.hasErrors()) {
			if (cmd.getKategorie()!=null) model.addAttribute("kategorien", cmd.getKategorie().getChildren());
	        updateKategorieInModel(model, cmd);
			return "vorgangneu/form";
        }
		vorgang.setDatum(new Date());
		vorgang.setStatus(EnumVorgangStatus.offen);
		vorgang.setPrioritaet(EnumPrioritaet.mittel);
		
		//verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.erzeugt, null, null);

		vorgangDao.persist(vorgang);
		
		vorgang.setZustaendigkeit(classificationService.calculateZustaendigkeitforVorgang(vorgang).getId());
		vorgang.setZustaendigkeitStatus(EnumZustaendigkeitStatus.zugewiesen);
		
		//verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.zustaendigkeit, null, vorgang.getZustaendigkeit());

		vorgangDao.merge(vorgang);

        return "vorgangneu/submit";
    }
	

}
