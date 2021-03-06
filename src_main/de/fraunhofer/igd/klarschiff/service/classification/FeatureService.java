package de.fraunhofer.igd.klarschiff.service.classification;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import weka.core.FastVector;
import weka.core.Instance;
import de.fraunhofer.igd.klarschiff.dao.KategorieDao;
import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.service.geo.GeoService;
import de.fraunhofer.igd.klarschiff.service.security.Role;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.vo.EnumZustaendigkeitStatus;
import de.fraunhofer.igd.klarschiff.vo.Kategorie;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;
import de.fraunhofer.igd.klarschiff.vo.VorgangFeatures;


/**
 * Der FeatureService dient zum Initialisieren und Berechnen der Features, so wie sie vom Klassifikator f�r das Initialisieren, Trainieren
 * und Klassifizieren ben�tigt werden.
 * Bei der Berechnung der Features werden ggf. nicht �nderbare Featurewerte persistiert. 
 * @author Stefan Audersch (Fraunhofer IGD)
 * @author Marcus Kr�ller (Fraunhofer IGD)
 */
@Service
public class FeatureService {
	static final Logger logger = Logger.getLogger(FeatureService.class);
	
	@Autowired
	KategorieDao kategorieDao;
	
	@Autowired
	VorgangDao vorgangDao;
	
	@Autowired
	GeoService geoService;
	
	@Autowired
	SecurityService securityService;
	
	List<String> bewirtschaftungFeatures;
	List<String> flaechenFeatures;
	
	
	/**
	 * Initialisiert den Klassifikatorkontext mit den f�r die Klassifikation verwendeten Features.
	 * @param classificationContext Kontext, der initialisiert werden soll
	 */
	@SuppressWarnings("unchecked")
	public void initClassificationContext(ClassificationContext classificationContext) {
		FastVector attributes = new FastVector();
		
		//Zust�ndigkeit
		List<String> zustaendigkeiten = new ArrayList<String>();
		for(Role zustaendigkeit : securityService.getAllZustaendigkeiten(false)) zustaendigkeiten.add(zustaendigkeit.getId());
		Attribute classAttribute = Attribute.createClassAttribute("zustaendigkeit", zustaendigkeiten);
		attributes.addElement(classAttribute);
		
		//Kategorien
		List<String> kategorien = new ArrayList<String>();
		for(Kategorie kategoerie : kategorieDao.getKategorien()) kategorien.add(kategoerie.getId()+"");
		attributes.addElement(Attribute.createAttribute("kategorie", kategorien, true));

		//Bewirtschaftung vom WFS
		for(String b : bewirtschaftungFeatures) {
		attributes.appendElements(Attribute.createGeoAttributes(
				"geo_bewirtschaftung_"+b, 
				"igd:bewirtschaftung", 
				"bewirtschafter", 
				b, 
				"the_geom", 
				false));
		}
		
		//Fl�chentypen vom WFS
		for(String f : flaechenFeatures) {
			attributes.appendElements(Attribute.createGeoAttributes(
					"geo_"+f, 
					"igd:"+f, 
					"the_geom", 
					false));
		}
		Map<String, Attribute> attributMap = new HashMap<String, Attribute>();
		for (Enumeration<Attribute> iter=attributes.elements(); iter.hasMoreElements(); ) {
			Attribute attribute = iter.nextElement();
			attributMap.put(attribute.getName(), attribute);
		}
		classificationContext.setAttributes(attributes);
		classificationContext.setAttributMap(attributMap);
		classificationContext.setClassAttribute(classAttribute);
	}

	
	/**
	 * Ermittelt f�r einen Vorgang die Features f�r den Klassifikator. Featurewerte, die nicht �nderbar sind und f�r den Vorgang 
	 * bereits gespeichert wurden, werden nicht neu berechnet, sondern aus der DB gelesen.
	 * Nach der Berechnung der Features werden nicht �nderbare Features in der DB gespeichert.
	 * @param vorgang Vorgang, f�r den die Features berechnet werden sollen
	 * @param inclClassAttribute Soll der Wert der aktuellen Zust�ndigkeit ebenfalls mit in die Features aufgenommen werden (z.B. f�r ein Trainingsset)?
	 * @param ctx Klassifikatorkontext
	 * @return berechnete Features
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Instance createFeature(Vorgang vorgang, boolean inclClassAttribute, ClassificationContext ctx) throws Exception {
		Instance instance = new Instance(ctx.getAttributes().size());
		
		VorgangFeatures vorgangFeatures = vorgangDao.findVorgangFeatures(vorgang);

		for (Enumeration<Attribute> iter=ctx.getAttributes().elements(); iter.hasMoreElements(); )
		{
			Attribute attribute = iter.nextElement();
			if (vorgangFeatures!=null && !attribute.isUpdateble()) {
				//Wert ggf. aus vorgangFeatures nehmen 
				if (vorgangFeatures.getFeatures().containsKey(attribute.getName())) {
					if (attribute.isNominal()) {
						instance.setValue(attribute, vorgangFeatures.getFeatures().get(attribute.getName()));
					} else {
						instance.setValue(attribute, Double.parseDouble(vorgangFeatures.getFeatures().get(attribute.getName())));
					}
				}
			} else {
				//Wert neu berechnen
				if (attribute.getName().equals("kategorie")) {
					instance.setValue(attribute, vorgang.getKategorie().getId()+"");
				} else if (attribute.getName().equals("zustaendigkeit")) {
					if (inclClassAttribute) {
						if (vorgang.getZustaendigkeitStatus()==EnumZustaendigkeitStatus.akzeptiert)
							instance.setValue(attribute, vorgang.getZustaendigkeit()+"");
						else throw new Exception("Zust�ndigkeit des Vorganges kann nicht als Feature mit aufgenommen werden, da die Zust�ndigkeit noch nicht akzeptiert ist.");
					}
				} else if (attribute.isGeoAttribute()){
					Double value = geoService.calculateFeature(vorgang.getOvi(), attribute);
					if (value!=null) instance.setValue(attribute, value);
				} else throw new Exception("Unbekanntes Feature: "+attribute.getName());
			}
		}
		
		if (vorgangFeatures==null) {
			//vorgangFeatures neu anlegen
			vorgangFeatures = new VorgangFeatures();
			vorgangFeatures.setVorgang(vorgang);
			//einzelne Werte in vorgangFeatures ablegen
			for (Enumeration<Attribute> iter=ctx.getAttributes().elements(); iter.hasMoreElements(); )
			{
				Attribute attribute = iter.nextElement();
				if (!attribute.isUpdateble && !instance.isMissing(attribute))
					vorgangFeatures.getFeatures().put(attribute.getName(), (attribute.isNominal()) ? instance.stringValue(attribute) : instance.value(attribute)+"");
			}
			//vorgangFeatures speichern
			vorgangDao.persist(vorgangFeatures);
		}
		
		return instance;
	}

	
	/**
	 * Erzeugt eine Liste von Features nur auf Basis der Kategorie. Der Klassifikator wird bis zu einer bestimmten Trainingsmenge zus�tzlich
	 * mit initialen Zust�ndigkeiten, die f�r die einzelnen Kategorien definiert werden k�nnen, trainiert. Die Erzeugung der Features
	 * f�r dieses Trainingsset erfolgt auf Basis dieser Funktion.
	 * @param kategorie Kategorie, f�r die die Features ermittelt werden sollen
	 * @param inclClassAttribute Soll die Zust�ndigkeit mit in die Features aufgenommen werden?
	 * @param ctx Klassifikatorkontext
	 * @return Liste mit Features (eine Kategorie kann initial f�r mehrere Zust�ndigkeiten gedacht sein)
	 */
	public List<Instance> createFeature(Kategorie kategorie, boolean inclClassAttribute, ClassificationContext ctx) {
	
		List<Instance> instances = new ArrayList<Instance>(); 
			
		for (String zustaendigkeit : kategorie.getInitialZustaendigkeiten()) {
			try {
				Instance instance = new Instance(ctx.getAttributes().size());
				instance.setValue(ctx.getAttributMap().get("kategorie") ,kategorie.getId()+"");
				if (inclClassAttribute) instance.setValue(ctx.getAttributMap().get("zustaendigkeit"), zustaendigkeit);
				instances.add(instance);
			} catch (Exception e) {
				logger.error("Initiale Zust�ndigkeit ("+zustaendigkeit+") f�r die Kategorie ("+kategorie.getName()+") ist nicht g�ltig.");
			}
		}
		return instances;
	}
	
	/* --------------- GET + SET ----------------------------*/
	
	public List<String> getBewirtschaftungFeatures() {
		return bewirtschaftungFeatures;
	}


	public void setBewirtschaftungFeatures(List<String> bewirtschaftungFeatures) {
		this.bewirtschaftungFeatures = bewirtschaftungFeatures;
	}


	public List<String> getFlaechenFeatures() {
		return flaechenFeatures;
	}


	public void setFlaechenFeatures(List<String> flaechenFeatures) {
		this.flaechenFeatures = flaechenFeatures;
	}


}
