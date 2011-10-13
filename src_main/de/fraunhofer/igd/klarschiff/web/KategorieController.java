package de.fraunhofer.igd.klarschiff.web;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.fraunhofer.igd.klarschiff.dao.KategorieDao;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangTyp;
import de.fraunhofer.igd.klarschiff.vo.Kategorie;
import flexjson.JSONSerializer;

@RequestMapping("/kategorien")
@Controller
public class KategorieController {
	public static Logger logger = Logger.getLogger(KategorieController.class);
	
	@Autowired
	KategorieDao kategorieDao;

	
	@RequestMapping(params={"kategorie"}, method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public Object childesForKategorieJson(@RequestParam("kategorie") Long kategorie) {
		List<Kategorie> list;
		try {
			list = kategorieDao.findKategorie(kategorie).getChildren();
		} catch (Exception e) {
			list = new ArrayList<Kategorie>();
		}
        return new JSONSerializer().include("id", "nameEscapeHtml").exclude("*.class", "name", "parent", "version").serialize(list);
    }

	@RequestMapping(params={"typ"}, method = RequestMethod.GET, headers = "Accept=application/json")
	@ResponseBody
	public Object childesForTypJson(@RequestParam("typ") String typ) {
		List<Kategorie> list;
		try {
			list = kategorieDao.findRootKategorienForTyp(EnumVorgangTyp.valueOf(typ));
		} catch (Exception e) {
			list = new ArrayList<Kategorie>();
		}
        return new JSONSerializer().include("id", "nameEscapeHtml").exclude("*.class", "name", "parent", "version").serialize(list);
	}
	
	@RequestMapping(value="/viewNaehereBeschreibung", method = RequestMethod.GET, headers = "Accept=application/json")
	@ResponseBody
	public Object viewNaehereBeschreibung(
			@RequestParam(value = "hauptkategorie", required = false) Long hauptkategorie,
			@RequestParam(value = "unterkategorie", required = false) Long unterkategorie) {
		return new JSONSerializer().include("string").serialize(kategorieDao.viewNaehereBeschreibung(hauptkategorie, unterkategorie));
	}
}
