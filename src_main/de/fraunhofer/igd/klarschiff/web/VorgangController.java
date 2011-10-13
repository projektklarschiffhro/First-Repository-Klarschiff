package de.fraunhofer.igd.klarschiff.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import de.fraunhofer.igd.klarschiff.dao.KommentarDao;
import de.fraunhofer.igd.klarschiff.dao.VerlaufDao;
import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.service.geo.GeoService;
import de.fraunhofer.igd.klarschiff.service.image.ImageService;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.vo.EnumFreigabeStatus;
import de.fraunhofer.igd.klarschiff.vo.EnumVerlaufTyp;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangStatus;
import de.fraunhofer.igd.klarschiff.vo.Verlauf;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;

@Controller
public class VorgangController {

	@Autowired
	VorgangDao vorgangDao;

	@Autowired
	VerlaufDao verlaufDao;

	@Autowired
	KommentarDao kommentarDao;
		
	@Autowired
	SecurityService securityService;	
	
	@Autowired
	GeoService geoService;
	
	@Autowired
	ImageService imageService;

	@RequestMapping(value="/vorgang/{id}/uebersicht", method = RequestMethod.GET)
    public String uebersicht(
    		@PathVariable("id") Long id, 
    		@RequestParam(value="page", defaultValue="1") Integer page, 
    		@RequestParam(value="size", defaultValue="5") Integer size,
    		ModelMap model, 
    		HttpServletRequest request) 
	{
    	Vorgang vorgang = vorgangDao.findVorgang(id);

		if (vorgang.getStatus()==EnumVorgangStatus.offen)
			return "redirect:/vorgang/" + id + "/erstsichtung";

		model.put("vorgang", vorgang);
		model.put("geoService", geoService);
		model.put("unterstuetzer", vorgangDao.countUnterstuetzerByVorgang(vorgang));
		model.put("missbrauch", vorgangDao.countOpenMissbrauchsmeldungByVorgang(vorgang));
		model.put("page", page);
		model.put("size", size);
    	model.put("maxPages", calculateMaxPages(size, kommentarDao.countKommentare(vorgang)));
		model.put("kommentare", kommentarDao.findKommentareForVorgang(vorgang, page, size));
		return "vorgang/uebersicht";
	}
	
	@RequestMapping(value="/vorgang/{id}/karte", method = RequestMethod.GET)
	public String karte(@PathVariable("id") Long id, ModelMap model, HttpServletRequest request) {
		Vorgang vorgang = vorgangDao.findVorgang(id);
		model.put("vorgang", vorgang);
		model.put("geoService", geoService);
		model.put("mapExternUrl", geoService.getMapExternUrl(vorgang));
		return "vorgang/karte";
	}
	
	@RequestMapping(value="/vorgang/{id}/foto", method = RequestMethod.GET)
	public String foto(@PathVariable("id") Long id, ModelMap model, HttpServletRequest request) {
		Vorgang vorgang = vorgangDao.findVorgang(id);
		model.put("vorgang", vorgang);
		return "vorgang/foto";
	}

	@RequestMapping(value="/vorgang/{id}/foto", method = RequestMethod.POST)
	public String foto(
			@PathVariable("id") Long id, 
			@RequestParam(value = "action", required = true) String action, 
			@RequestParam(value="censorRectangles", required=false) String censorRectangleString,
			@RequestParam(value="censoringWidth", required=false) Integer censoringWidth,
			@RequestParam(value="censoringHeight", required=false) Integer censoringHeight,
			ModelMap model, HttpServletRequest request) {
		Vorgang vorgang = vorgangDao.findVorgang(id);
		for (@SuppressWarnings("unused") Verlauf verlauf : vorgang.getVerlauf());

		if (action.equals("fotoSave")) {

			imageService.censorImageForVorgang(vorgang, censorRectangleString, censoringWidth, censoringHeight);
			vorgangDao.merge(vorgang);
		
		} else if (action!=null && action.startsWith("freigabeStatus_Foto")) {
			String str[] = action.split("_");
			EnumFreigabeStatus freigabeStatus = EnumFreigabeStatus.valueOf(str[2]);
			verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.fotoFreigabeStatus, vorgang.getFotoFreigabeStatus().getText(), freigabeStatus.getText());
			vorgang.setFotoFreigabeStatus(freigabeStatus);
			vorgangDao.merge(vorgang, false);
		}
		model.put("vorgang", vorgang);
		return "vorgang/foto";
	}		

	@RequestMapping(value="/vorgang/{id}/verlauf", method = RequestMethod.GET)
	public String verlauf(@PathVariable("id") Long id, @RequestParam(value="page", defaultValue="1") Integer page, @RequestParam(value="size", defaultValue="10") Integer size, ModelMap model, HttpServletRequest request) {
		Vorgang vorgang = vorgangDao.findVorgang(id);
		model.put("vorgang", vorgang);
		model.put("page", page);
		model.put("size", size);
		model.addAttribute("verlauf", verlaufDao.findVerlaufForVorgang(vorgang, page, size));
    	model.put("maxPages", calculateMaxPages(size, verlaufDao.countVerlauf(vorgang)));
		return "vorgang/verlauf";
	}
	
	private int calculateMaxPages(int size, long count)
    {
		float nrOfPages = (float) count / size;
		return (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages);
    }	
}
