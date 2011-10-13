package de.fraunhofer.igd.klarschiff.web;

import java.io.PrintWriter;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.codec.Base64;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.fraunhofer.igd.klarschiff.dao.KategorieDao;
import de.fraunhofer.igd.klarschiff.dao.VerlaufDao;
import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.service.classification.ClassificationService;
import de.fraunhofer.igd.klarschiff.service.image.ImageService;
import de.fraunhofer.igd.klarschiff.service.mail.MailService;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.vo.EnumPrioritaet;
import de.fraunhofer.igd.klarschiff.vo.EnumVerlaufTyp;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangStatus;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangTyp;
import de.fraunhofer.igd.klarschiff.vo.EnumZustaendigkeitStatus;
import de.fraunhofer.igd.klarschiff.vo.GeoRss;
import de.fraunhofer.igd.klarschiff.vo.Missbrauchsmeldung;
import de.fraunhofer.igd.klarschiff.vo.Unterstuetzer;
import de.fraunhofer.igd.klarschiff.vo.Verlauf;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;

@RequestMapping("/service")
@Controller
public class BackendController {
	Logger logger = Logger.getLogger(BackendController.class);
	
	@Autowired
	KategorieDao kategorieDao;
	
	@Autowired
	VorgangDao vorgangDao;
	
	@Autowired
	VerlaufDao verlaufDao;
	
	@Autowired
	ClassificationService classificationService;
	
	@Autowired
	SecurityService securityService;

	@Autowired
	ImageService imageService;
	
	@Autowired
	MailService mailService;
	
	/**
	 * 
	 * @param typ
	 * @param kategorie
	 * @param oviWkt
	 * @param autorEmail
	 * @param betreff
	 * @param details
	 * @param model
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="/vorgang", method = RequestMethod.POST)
	@ResponseBody
	public void vorgang(
			@RequestParam(value = "typ", required = false) String typ, 
			@RequestParam(value = "kategorie", required = false) Long kategorie,
			@RequestParam(value = "oviWkt", required = false) String oviWkt,
			@RequestParam(value = "autorEmail", required = false) String autorEmail,
			@RequestParam(value = "betreff", required = false) String betreff,
			@RequestParam(value = "details", required = false) String details,
			@RequestParam(value = "bild", required = false) String bild,
			ModelMap model, 
			HttpServletRequest request,
			HttpServletResponse response) {
		
		try {
			Vorgang vorgang = new Vorgang();
			
			if (StringUtils.isBlank(typ)) throw new BackendControllerException(1, "[typ] fehlt");
			vorgang.setTyp(EnumVorgangTyp.valueOf(typ));
			if (vorgang.getTyp()==null) throw new BackendControllerException(2, "[typ] nicht korrekt");
			
			if (kategorie==null) throw new BackendControllerException(3, "[kategorie] fehlt");
			vorgang.setKategorie(kategorieDao.findKategorie(kategorie));
			if (vorgang.getKategorie()==null 
					|| vorgang.getKategorie().getParent()==null
					|| vorgang.getKategorie().getParent().getTyp()!=vorgang.getTyp()) throw new BackendControllerException(4, "[kategorie] nicht korrekt");
			
			if (oviWkt==null) throw new BackendControllerException(5, "[oviWkt] fehlt");
			try {
				vorgang.setOviWkt(oviWkt);
			}catch (Exception e) {
				throw new BackendControllerException(6, "[oviWkt] nicht korrekt", e);
			}

			if (StringUtils.isBlank(autorEmail)) throw new BackendControllerException(7, "[autorEmail] fehlt");
			if (!isMaxLength(autorEmail, 300)) throw new BackendControllerException(8, "[autorEmail] zu lang");
			if (!isEmail(autorEmail)) throw new BackendControllerException(9, "[autorEmail] nicht korrekt");
			vorgang.setAutorEmail(autorEmail);
			vorgang.setHash(securityService.createHash(autorEmail+System.currentTimeMillis()));
			
			if (!isMaxLength(betreff, 300)) throw new BackendControllerException(10, "[betreff] zu lang");
			vorgang.setBetreff(betreff);
			
			vorgang.setDetails(details);
			
			if (bild!=null)
				try {
					imageService.setImageForVorgang(Base64.decode(bild.getBytes()), vorgang);
				} catch (Exception e) {
					throw new BackendControllerException(11, "[bild] nicht korrekt");
				}
			
			vorgang.setDatum(new Date());
			vorgang.setStatus(EnumVorgangStatus.gemeldet);
			vorgang.setPrioritaet(EnumPrioritaet.mittel);
						
			vorgangDao.persist(vorgang);

			sendOk(response, vorgang.getHash());

			mailService.sendVorgangBestaetigungMail(vorgang);
		} catch (Exception e) {
			logger.warn(e);
			sendError(response, e);
		}
	}


	
	
	private static boolean isEmail(String email)
	{
		if (!matches(email, ".+@.+\\.[a-z]+")) return false;
		else return true;
	}
	private static boolean matches(String str, String pattern)
	{
       Pattern p = Pattern.compile(pattern);
       Matcher m = p.matcher(str);
       return m.matches();
	}
	private static boolean isMaxLength(String str, int maxLength)
	{
		if (str==null || str.length()<=maxLength) return true;
		else return true;
	}

	/**
	 * 
	 * @param hash
	 * @param model
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="/vorgangBestaetigung")
	public String vorgangBestaetigung(
			@RequestParam(value = "hash", required = false) String hash, 
			ModelMap model, 
			HttpServletRequest request,
			HttpServletResponse response) {
		
		try {
			if (StringUtils.isBlank(hash)) throw new BackendControllerException(101, "[hash] fehlt");
			Vorgang vorgang = vorgangDao.findVorgangByHash(hash);
			if (vorgang==null) throw new BackendControllerException(102, "[hash] nicht korrekt");

			for (Verlauf verlauf : vorgang.getVerlauf()) verlauf.getTyp();
			
			if (vorgang.getStatus()!=EnumVorgangStatus.gemeldet)  throw new BackendControllerException(103, "Vorgang wurde bereits bestätigt");

			vorgang.setStatus(EnumVorgangStatus.offen);
						
			verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.vorgangBestaetigung, null, null);
			vorgangDao.merge(vorgang);

			vorgang.setZustaendigkeit(classificationService.calculateZustaendigkeitforVorgang(vorgang).getId());
			vorgang.setZustaendigkeitStatus(EnumZustaendigkeitStatus.zugewiesen);
			
			vorgangDao.merge(vorgang);
			
			return "backend/bestaetigungOk";
			
		} catch (Exception e) {
			return "backend/bestaetigungFehler";
		}
	}


	/**
	 * 
	 * @param vorgang
	 * @param email
	 * @param model
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="/unterstuetzer", method = RequestMethod.POST)
	@ResponseBody
	public void unterstuetzer(
			@RequestParam(value = "vorgang", required = false) Long vorgang, 
			@RequestParam(value = "email", required = false) String email, 
			ModelMap model, 
			HttpServletRequest request,
			HttpServletResponse response) {
		
		try {
			Unterstuetzer unterstuetzer = new Unterstuetzer();
			if (vorgang==null) throw new BackendControllerException(201, "[vorgang] fehlt");
			unterstuetzer.setVorgang(vorgangDao.findVorgang(vorgang));
			if (unterstuetzer.getVorgang()==null) throw new BackendControllerException(202, "[vorgang] nicht korrekt");
			
			if (StringUtils.isBlank(email)) throw new BackendControllerException(203, "[email] fehlt");
			if (!isMaxLength(email, 300)) throw new BackendControllerException(204, "[email] zu lang");
			if (!isEmail(email)) throw new BackendControllerException(205, "[email] nicht korrekt");
			unterstuetzer.setHash(securityService.createHash(unterstuetzer.getVorgang().getId()+email));
			if (vorgangDao.findUnterstuetzer(unterstuetzer.getHash())!=null) throw new BackendControllerException(206, "[email] wurde bereits für den [vorgang] verwendet");
			if (StringUtils.equalsIgnoreCase(unterstuetzer.getVorgang().getAutorEmail(), email)) throw new BackendControllerException(207, "[email] der autor des [vorgang] kann keine unterstützung für den [vorgang] abgeben");
			
			unterstuetzer.setDatum(new Date());

			vorgangDao.persist(unterstuetzer);
			
			sendOk(response, unterstuetzer.getHash());

			mailService.sendUnterstuetzerBestaetigungMail(unterstuetzer, email);
			
		} catch (Exception e) {
			logger.warn(e);
			sendError(response, e);
		}
	}

	
	/**
	 * 
	 * @param hash
	 * @param model
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="/unterstuetzerBestaetigung")
	public String unterstuetzerBestaetigung(
			@RequestParam(value = "hash", required = false) String hash, 
			ModelMap model, 
			HttpServletRequest request,
			HttpServletResponse response) {
		
		try {
			if (StringUtils.isBlank(hash)) throw new BackendControllerException(301, "[hash] fehlt");
			Unterstuetzer unterstuetzer = vorgangDao.findUnterstuetzer(hash);
			if (unterstuetzer==null) throw new BackendControllerException(302, "[hash] nicht korrekt");
			
			if (unterstuetzer.getDatumBestaetigung()!=null)  throw new BackendControllerException(303, "Unterstützer wurde bereits bestätigt");

			unterstuetzer.setDatumBestaetigung(new Date());
			
			verlaufDao.addVerlaufToVorgang(unterstuetzer.getVorgang(), EnumVerlaufTyp.vorgangUnterstuetzerBestaetigung, null, null);
			vorgangDao.merge(unterstuetzer);

			return "backend/bestaetigungOk";
			
		} catch (Exception e) {
			logger.warn(e);
			return "backend/bestaetigungFehler";
		}
	}

	
	/**
	 * 
	 * @param vorgang
	 * @param email
	 * @param model
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="/missbrauchsmeldung", method = RequestMethod.POST)
	@ResponseBody
	public void missbrauchsmeldung(
			@RequestParam(value = "vorgang", required = false) Long vorgang, 
			@RequestParam(value = "text", required = false) String text, 
			@RequestParam(value = "email", required = false) String email, 
			ModelMap model, 
			HttpServletRequest request,
			HttpServletResponse response) {
		
		try {
			Missbrauchsmeldung missbrauchsmeldung = new Missbrauchsmeldung();
			if (vorgang==null) throw new BackendControllerException(401, "[vorgang] fehlt");
			missbrauchsmeldung.setVorgang(vorgangDao.findVorgang(vorgang));
			if (missbrauchsmeldung.getVorgang()==null) throw new BackendControllerException(402, "[vorgang] nicht korrekt");

			if (StringUtils.isBlank(text)) throw new BackendControllerException(403, "[text] fehlt");
			missbrauchsmeldung.setText(text);
			
			if (StringUtils.isBlank(email)) throw new BackendControllerException(404, "[email] fehlt");
			if (!isMaxLength(email, 300)) throw new BackendControllerException(405, "[email] zu lang");
			if (!isEmail(email)) throw new BackendControllerException(406, "[email] nicht korrekt");
			missbrauchsmeldung.setHash(securityService.createHash(missbrauchsmeldung.getVorgang().getId()+email+System.currentTimeMillis()));
			
			missbrauchsmeldung.setDatum(new Date());

			vorgangDao.persist(missbrauchsmeldung);
			
			mailService.sendMissbrauchsmeldungBestaetigungMail(missbrauchsmeldung, email);

			sendOk(response, missbrauchsmeldung.getHash());
		} catch (Exception e) {
			logger.warn(e);
			sendError(response, e);
		}
	}

	
	/**
	 * 
	 * @param hash
	 * @param model
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="/missbrauchsmeldungBestaetigung")
	public String missbrauchsmeldungBestaetigung(
			@RequestParam(value = "hash", required = false) String hash, 
			ModelMap model, 
			HttpServletRequest request,
			HttpServletResponse response) {
		
		try {
			if (StringUtils.isBlank(hash)) throw new BackendControllerException(501, "[hash] fehlt");
			Missbrauchsmeldung missbrauchsmeldung = vorgangDao.findMissbrauchsmeldung(hash);
			if (missbrauchsmeldung==null) throw new BackendControllerException(502, "[hash] nicht korrekt");
			
			if (missbrauchsmeldung.getDatumBestaetigung()!=null)  throw new BackendControllerException(503, "Missbrauchsmeldung wurde bereits bestätigt");

			missbrauchsmeldung.setDatumBestaetigung(new Date());
			
			verlaufDao.addVerlaufToVorgang(missbrauchsmeldung.getVorgang(), EnumVerlaufTyp.vorgangMissbrauchsmeldungBestaetigung, null, null);
			vorgangDao.merge(missbrauchsmeldung);

			return "backend/bestaetigungOk";
			
		} catch (Exception e) {
			logger.warn(e);
			return "backend/bestaetigungFehler";
		}
	}
	

	
	/**
	 * 
	 * @param hash
	 * @param model
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="/vorgangLoeschen")
	public String vorgangloeschen(
			@RequestParam(value = "hash", required = false) String hash, 
			ModelMap model, 
			HttpServletRequest request,
			HttpServletResponse response) {
		
		try {
			if (StringUtils.isBlank(hash)) throw new BackendControllerException(601, "[hash] fehlt");
			Vorgang vorgang = vorgangDao.findVorgangByHash(hash);
			if (vorgang==null) throw new BackendControllerException(602, "[hash] nicht korrekt");
			
			if ((vorgang.getStatus()==EnumVorgangStatus.gemeldet || vorgang.getStatus()==EnumVorgangStatus.offen)
					&& vorgang.getUnterstuetzer().size()==0 && vorgang.getMissbrauchsmeldungen().size()==0)
			{
				vorgang.setStatus(EnumVorgangStatus.geloescht);
				vorgangDao.merge(vorgang);
				
			} else throw new BackendControllerException(103, "Vorgang kann nicht mehr gelöscht werden"); 
			
			return "backend/vorgangLoeschenOk";
			
		} catch (Exception e) {
			return "backend/vorgangLoeschenFehler";
		}
	}

	/**
	 * 
	 * @param vorgang
	 * @param email
	 * @param model
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="/geoRss", method = RequestMethod.POST)
	@ResponseBody
	public void geoRss(
			@RequestParam(value = "oviWkt", required = false) String oviWkt, 
			@RequestParam(value = "probleme", required = false) Boolean probleme, 
			@RequestParam(value = "problemeKategorien", required = false) String problemeKategorien, 
			@RequestParam(value = "ideen", required = false) Boolean ideen, 
			@RequestParam(value = "ideenKategorien", required = false) String ideenKategorien, 
			ModelMap model, 
			HttpServletRequest request,
			HttpServletResponse response) {
		
		try {
			GeoRss geoRss = new GeoRss();
			if (StringUtils.isBlank(oviWkt)) throw new BackendControllerException(701, "[oviWkt] fehlt");
			try {
				geoRss.setOviWkt(oviWkt);
			}catch (Exception e) {
				throw new BackendControllerException(702, "[oviWkt] nicht korrekt", e);
			}
			if (probleme==null) throw new BackendControllerException(703, "[probleme] fehlt");
			geoRss.setProbleme(probleme);
			geoRss.setProblemeKategorien(problemeKategorien);
			if (ideen==null) throw new BackendControllerException(704, "[ideen] fehlt");
			geoRss.setIdeen(ideen);
			geoRss.setIdeenKategorien(ideenKategorien);
			
			vorgangDao.persist(geoRss);
			
			sendOk(response, geoRss.getId()+"");
		} catch (Exception e) {
			logger.warn(e);
			sendError(response, e);
		}
	}
	
	private void sendError(HttpServletResponse response, Exception exception) {

		try {
			response.setHeader("Content-Type", "text/html;charset=UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			PrintWriter writer = response.getWriter();
			writer.print(exception.getMessage());
			response.flushBuffer();
		} catch (Exception e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unused")
	private void sendOk(HttpServletResponse response) {

		try {
			response.setHeader("Content-Type", "text/html;charset=UTF-8");
			response.setStatus(HttpServletResponse.SC_OK);
			response.flushBuffer();
		} catch (Exception e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}

	private void sendOk(HttpServletResponse response, String content) {

		try {
			response.setHeader("Content-Type", "text/html;charset=UTF-8");
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().append(content);
			response.flushBuffer();
		} catch (Exception e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}
	
}
