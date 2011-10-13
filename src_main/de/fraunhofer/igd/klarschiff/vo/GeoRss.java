package de.fraunhofer.igd.klarschiff.vo;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

@Configurable
@Entity
public class GeoRss {

	/* --------------- Attribute ----------------------------*/

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

    @Type(type = "org.hibernatespatial.GeometryUserType")
    private MultiPolygon ovi;
	
	private boolean probleme;
	
	private String problemeKategorien;
	
	private boolean ideen;
	
	private String ideenKategorien;

	/* --------------- transient ----------------------------*/

    @Transient
    private static GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 25833);
    
    @Transient
    private static WKTReader wktReader = new WKTReader(geometryFactory);
    
    @Transient
    private static WKTWriter wktWriter = new WKTWriter();

    @Transient
    public void setOviWkt(String oviWkt) throws Exception {
    	ovi = (StringUtils.isBlank(oviWkt)) ? null : (MultiPolygon)wktReader.read(oviWkt);
    }

    @Transient
    public String getOviWkt() {
    	return (ovi==null) ? null : wktWriter.write(ovi);
    }
    /* --------------- GET + SET ----------------------------*/
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public MultiPolygon getOvi() {
		return ovi;
	}

	public void setOvi(MultiPolygon ovi) {
		this.ovi = ovi;
	}

	public boolean getProbleme() {
		return probleme;
	}

	public void setProbleme(boolean probleme) {
		this.probleme = probleme;
	}

	public String getProblemeKategorien() {
		return problemeKategorien;
	}

	public void setProblemeKategorien(String problemeKategorien) {
		this.problemeKategorien = problemeKategorien;
	}

	public boolean getIdeen() {
		return ideen;
	}

	public void setIdeen(boolean ideen) {
		this.ideen = ideen;
	}

	public String getIdeenKategorien() {
		return ideenKategorien;
	}

	public void setIdeenKategorien(String ideenKategorien) {
		this.ideenKategorien = ideenKategorien;
	}
}
