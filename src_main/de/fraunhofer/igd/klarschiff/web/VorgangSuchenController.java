package de.fraunhofer.igd.klarschiff.web;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.fraunhofer.igd.klarschiff.dao.GrenzenDao;
import de.fraunhofer.igd.klarschiff.dao.KategorieDao;
import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.service.geo.GeoService;
import de.fraunhofer.igd.klarschiff.service.poi.PoiService;
import de.fraunhofer.igd.klarschiff.service.security.Role;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.service.settings.SettingsService;
import de.fraunhofer.igd.klarschiff.vo.EnumPrioritaet;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangStatus;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangTyp;
import de.fraunhofer.igd.klarschiff.web.VorgangSuchenCommand.EinfacheSuche;
import de.fraunhofer.igd.klarschiff.web.VorgangSuchenCommand.Suchtyp;

@SessionAttributes({"cmdvorgangsuchen"})
@RequestMapping("/vorgang/suchen")
@Controller
public class VorgangSuchenController {

	Logger logger = Logger.getLogger(VorgangSuchenController.class);
	
	@Autowired
	VorgangDao vorgangDao;

	@Autowired
	GrenzenDao grenzenDao;
	
	@Autowired
	PoiService poiService;
	
	@Autowired
	GeoService geoService;
	
	@Autowired
	SecurityService securityService;
	
	@Autowired
	KategorieDao kategorieDao;
	
	@Autowired
	SettingsService settingsService;
	
	@ModelAttribute("allZustaendigkeiten")
    public List<Role> allZustaendigkeiten() {
        return securityService.getAllZustaendigkeiten(true);
    }

	@ModelAttribute("allDelegiertAn")
	public List<Role> allDelegiertAn() {
		return securityService.getAllDelegiertAn();
	}

	@ModelAttribute("allVorgangStatus")
	public EnumVorgangStatus[] allVorgangStatus() {
		return EnumVorgangStatus.values();
	}
	
	@ModelAttribute("vorgangtypen")
    public Collection<EnumVorgangTyp> populateEnumVorgangTypen() {
        return Arrays.asList(EnumVorgangTyp.values());
    }

	@ModelAttribute("allPrioritaeten")
    public Collection<EnumPrioritaet> allPrioritaeten() {
        return Arrays.asList(EnumPrioritaet.values());
    }

	@ModelAttribute("vorgangIdeenUnterstuetzer")
    public Long vorgangIdeenUnterstuetzer() {
        return settingsService.getVorgangIdeeUnterstuetzer();
    }
	
	@ModelAttribute("allStadtteile")
    public List<Object[]> allStadtteile() {
        return grenzenDao.findStadtteilGrenzen();
    }
	
	@ModelAttribute("cmdvorgangsuchen")
    public VorgangSuchenCommand initCommand() {
		VorgangSuchenCommand cmd = new VorgangSuchenCommand();
    	cmd.setSize(20);
    	cmd.setOrder(2);
    	cmd.setOrderDirection(1);
    	//Suchtyp
    	cmd.setSuchtyp(VorgangSuchenCommand.Suchtyp.einfach);
    	//Initiale einfache Suche
    	cmd.setEinfacheSuche(VorgangSuchenCommand.EinfacheSuche.offene);
    	//Initiale erweiterte Suche
    	cmd.setErweitertArchiviert(false);
    	cmd.setErweitertZustaendigkeit("#mir zugewiesen#");
    	cmd.setErweitertUnterstuetzerAb(settingsService.getVorgangIdeeUnterstuetzer());
    	cmd.setErweitertVorgangStatus((EnumVorgangStatus[])ArrayUtils.removeElement(ArrayUtils.removeElement(EnumVorgangStatus.values(), EnumVorgangStatus.gemeldet), EnumVorgangStatus.geloescht));
        return cmd;
    }
	
	private void updateKategorieInModel(ModelMap model, VorgangSuchenCommand cmd) {
		try {
			model.addAttribute("hauptkategorien", kategorieDao.findRootKategorienForTyp(cmd.getErweitertVorgangTyp()));
			model.addAttribute("unterkategorien", kategorieDao.findKategorie(cmd.getErweitertHauptkategorie().getId()).getChildren());
		}catch (Exception e) {}
	}
	
	@RequestMapping(method = RequestMethod.GET)
    public String suchen(@ModelAttribute(value = "cmdvorgangsuchen") VorgangSuchenCommand cmd, @RequestParam(value = "neu", required = false) boolean neu, ModelMap modelMap) {
		if (neu) {
			cmd = initCommand();
			modelMap.put("cmdvorgangsuchen", cmd);
		}
		updateKategorieInModel(modelMap, cmd);
    	//Suchen
		modelMap.addAttribute("vorgaenge", vorgangDao.listVorgang(cmd));
		if (cmd.suchtyp==Suchtyp.einfach && cmd.einfacheSuche==EinfacheSuche.offene)
			modelMap.put("missbrauchsmeldungenAbgeschlossenenVorgaenge", vorgangDao.missbrauchsmeldungenAbgeschlossenenVorgaenge());
		modelMap.put("maxPages", calculateMaxPages(cmd.getSize(), vorgangDao.countVorgang(cmd)));

		return "vorgang/suchen";
	}
	
	@RequestMapping(value="/karte", method = RequestMethod.GET)
	public String karte(@ModelAttribute(value = "cmdvorgangsuchen") VorgangSuchenCommand cmd, ModelMap modelMap) 
	{
		try {
			VorgangSuchenCommand cmd2 = (VorgangSuchenCommand)BeanUtils.cloneBean(cmd);
			cmd2.setPage(null);
			cmd2.setSize(null);
			
			modelMap.addAttribute("geoService", geoService);
			modelMap.addAttribute("vorgaenge", vorgangDao.listVorgang(cmd2));
			return "vorgang/suchenKarte";
		} catch (Exception e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}

	@RequestMapping(value="/vorgaenge.xls", method = RequestMethod.GET)
    @ResponseBody
	public void excel(
    		@ModelAttribute(value = "cmdvorgangsuchen") VorgangSuchenCommand cmd,
			HttpServletRequest request,
			HttpServletResponse response) {
		try {
			VorgangSuchenCommand cmd2 = (VorgangSuchenCommand)BeanUtils.cloneBean(cmd);
			cmd2.setPage(null);
			cmd2.setSize(null);
			
			List<Object[]> vorgaenge = vorgangDao.listVorgang(cmd2);
			
			HSSFWorkbook workbook = poiService.createScheet(PoiService.Template.vorgangListe, vorgaenge);
			
			response.setHeader("Content-Type", "application/ms-excel");
			workbook.write(response.getOutputStream());
			response.setStatus(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}
	
	private int calculateMaxPages(int size, long count)
    {
		float nrOfPages = (float) count / size;
		return (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages);
    }
	
}
