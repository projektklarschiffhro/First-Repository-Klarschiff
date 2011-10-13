package de.fraunhofer.igd.klarschiff.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import de.fraunhofer.igd.klarschiff.dao.KommentarDao;
import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.service.geo.GeoService;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;

@Controller
public class VorgangPrintController {

	@Autowired
	VorgangDao vorgangDao;

	@Autowired
	KommentarDao kommentarDao;
	
	@Autowired
	GeoService geoService;
	
	@RequestMapping(value="/vorgang/{id}/print", method = RequestMethod.GET)
	public String print(@PathVariable("id") Long id, ModelMap model, HttpServletRequest request) {
		return print(id, model, request, false);
	}
	
	@RequestMapping(value="/vorgang/delegiert/{id}/print", method = RequestMethod.GET)
	public String printDelegiert(@PathVariable("id") Long id, ModelMap model, HttpServletRequest request) {
		return print(id, model, request, true);
	}

	public String print(Long id, ModelMap model, HttpServletRequest request, boolean delegiert) {
		Vorgang vorgang = vorgangDao.findVorgang(id);
		model.put("geoService", geoService);
		model.put("kommentare", kommentarDao.findKommentareForVorgang(vorgang, null, null));
		if (delegiert) model.put("delegiert", true);
		else model.put("missbrauchsmeldungen", vorgangDao.listMissbrauchsmeldung(vorgang));
		model.put("vorgang", vorgang);
		return "noMenu/vorgang/printEmail/print";
	}
}
