package de.fraunhofer.igd.klarschiff.web;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
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

import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.service.geo.GeoService;
import de.fraunhofer.igd.klarschiff.service.poi.PoiService;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;

@SessionAttributes({"cmdvorgangdelegiertsuchen"})
@RequestMapping("/vorgang/delegiert/suchen")
@Controller
public class VorgangDelegiertSuchenController {

	Logger logger = Logger.getLogger(VorgangDelegiertSuchenController.class);
	
	@Autowired
	VorgangDao vorgangDao;

	@Autowired
	PoiService poiService;
	
	@Autowired
	GeoService geoService;
	
	@ModelAttribute("delegiert")
    public boolean delegiert() {
        return true;
    }
	
	@ModelAttribute("cmdvorgangdelegiertsuchen")
    public VorgangDelegiertSuchenCommand initCommand() {
		VorgangDelegiertSuchenCommand cmd = new VorgangDelegiertSuchenCommand();
    	cmd.setSize(20);
    	cmd.setOrder(2);
    	cmd.setOrderDirection(1);
    	cmd.setEinfacheSuche(VorgangDelegiertSuchenCommand.EinfacheSuche.offene);
        return cmd;
    }
	
	
	@RequestMapping(method = RequestMethod.GET)
    public String suchen(@ModelAttribute(value = "cmdvorgangdelegiertsuchen") VorgangDelegiertSuchenCommand cmd, @RequestParam(value = "neu", required = false) boolean neu, ModelMap modelMap) {
		if (neu) {
			cmd = initCommand();
			modelMap.put("cmdvorgangdelegiertsuchen", cmd);
		}
    	//Suchen
		modelMap.addAttribute("vorgaenge", vorgangDao.listVorgang(cmd));
    	modelMap.put("maxPages", calculateMaxPages(cmd.getSize(), vorgangDao.countVorgang(cmd)));

		return "vorgang/delegiert/suchen";
	}
	
	private int calculateMaxPages(int size, long count)
    {
		float nrOfPages = (float) count / size;
		return (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages);
    }
	
	
	@RequestMapping(value="/karte", method = RequestMethod.GET)
	public String karte(@ModelAttribute(value = "cmdvorgangdelegiertsuchen") VorgangDelegiertSuchenCommand cmd, ModelMap modelMap) 
	{
		try {
			VorgangDelegiertSuchenCommand cmd2 = (VorgangDelegiertSuchenCommand)BeanUtils.cloneBean(cmd);
			cmd2.setPage(null);
			cmd2.setSize(null);
			
			modelMap.addAttribute("geoService", geoService);
			modelMap.addAttribute("vorgaenge", vorgangDao.listVorgang(cmd2));
			return "vorgang/delegiert/suchenKarte";
		} catch (Exception e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}

	
	@RequestMapping(value="/vorgaenge.xls", method = RequestMethod.GET)
    @ResponseBody
	public void excel(
    		@ModelAttribute(value = "cmdvorgangdelegiertsuchen") VorgangDelegiertSuchenCommand cmd,
			HttpServletRequest request,
			HttpServletResponse response) {
		try {
			VorgangDelegiertSuchenCommand cmd2 = (VorgangDelegiertSuchenCommand)BeanUtils.cloneBean(cmd);
			cmd2.setPage(null);
			cmd2.setSize(null);
			
			List<Vorgang> vorgaenge = vorgangDao.listVorgang(cmd2);
			
			HSSFWorkbook workbook = poiService.createScheet(PoiService.Template.vorgangDelegiertListe, vorgaenge);
			
			response.setHeader("Content-Type", "application/ms-excel");
			workbook.write(response.getOutputStream());
			response.setStatus(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}
	
//	@RequestMapping(value="/vorgaenge.xls", method = RequestMethod.GET)
//    @ResponseBody
//	public void excel(
//    		@ModelAttribute(value = "cmdvorgangdelegiertsuchen") VorgangDelegiertSuchenCommand cmd,
//			HttpServletRequest request,
//			HttpServletResponse response) {
//		try {
//			VorgangDelegiertSuchenCommand cmd2 = (VorgangDelegiertSuchenCommand)BeanUtils.cloneBean(cmd);
//			cmd2.setPage(null);
//			cmd2.setSize(null);
//			
//			List<Vorgang> vorgaenge = vorgangDao.listVorgang(cmd2);
//			
//			HSSFWorkbook workbook = poiService.createScheet(PoiService.Template.vorgangListe, vorgaenge);
//			
//			response.setHeader("Content-Type", "application/ms-excel");
//			workbook.write(response.getOutputStream());
//			response.setStatus(HttpServletResponse.SC_OK);
//		} catch (Exception e) {
//			logger.error(e);
//			throw new RuntimeException(e);
//		}
//	
//	}
}
