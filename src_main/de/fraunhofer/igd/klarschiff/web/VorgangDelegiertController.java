package de.fraunhofer.igd.klarschiff.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import de.fraunhofer.igd.klarschiff.dao.KommentarDao;
import de.fraunhofer.igd.klarschiff.dao.VerlaufDao;
import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.service.geo.GeoService;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;

@Controller
public class VorgangDelegiertController {

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
	
	@ModelAttribute("delegiert")
    public boolean delegiert() {
        return true;
    }

	@RequestMapping(value="/vorgang/delegiert/{id}/uebersicht", method = RequestMethod.GET)
    public String uebersicht(
    		@PathVariable("id") Long id, 
    		@RequestParam(value="page", defaultValue="1") Integer page, 
    		@RequestParam(value="size", defaultValue="5") Integer size,
    		ModelMap model, 
    		HttpServletRequest request) 
	{
    	Vorgang vorgang = vorgangDao.findVorgang(id);

		model.put("vorgang", vorgang);
		model.put("page", page);
		model.put("geoService", geoService);
		model.put("size", size);
    	model.put("maxPages", calculateMaxPages(size, kommentarDao.countKommentare(vorgang)));
		model.put("kommentare", kommentarDao.findKommentareForVorgang(vorgang, page, size));
		return "vorgang/delegiert/uebersicht";
	}
	
	@RequestMapping(value="/vorgang/delegiert/{id}/karte", method = RequestMethod.GET)
	public String karte(@PathVariable("id") Long id, ModelMap model, HttpServletRequest request) {
		Vorgang vorgang = vorgangDao.findVorgang(id);
		model.put("vorgang", vorgang);
		model.put("geoService", geoService);
		return "vorgang/delegiert/karte";
	}
		
	@RequestMapping(value="/vorgang/delegiert/{id}/foto", method = RequestMethod.GET)
	public String foto(@PathVariable("id") Long id, ModelMap model, HttpServletRequest request) {
		Vorgang vorgang = vorgangDao.findVorgang(id);
		model.put("vorgang", vorgang);
		return "vorgang/delegiert/foto";
	}
		
	private int calculateMaxPages(int size, long count)
    {
		float nrOfPages = (float) count / size;
		return (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages);
    }	
}
