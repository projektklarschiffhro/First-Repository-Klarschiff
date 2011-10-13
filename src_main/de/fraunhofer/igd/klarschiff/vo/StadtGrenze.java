package de.fraunhofer.igd.klarschiff.vo;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

@Entity
@Configurable
@Table
public class StadtGrenze {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Type(type = "org.hibernatespatial.GeometryUserType")
    private Polygon grenze;

	/* --------------- transient ----------------------------*/
    
    @Transient
    private static GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 25833);
    
    @Transient
    private static WKTReader wktReader = new WKTReader(geometryFactory);
    
    @Transient
    private static WKTWriter wktWriter = new WKTWriter();

    @Transient
    public void setGrenzeWkt(String grenzeWkt) throws Exception {
    	grenze = (StringUtils.isBlank(grenzeWkt)) ? null : (Polygon)wktReader.read(grenzeWkt);
    }

    @Transient
    public String getGrenzeWkt() {
    	return (grenze==null) ? null : wktWriter.write(grenze);
    }
    
	/* --------------- GET + SET ----------------------------*/
    
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Polygon getGrenze() {
		return grenze;
	}

	public void setGrenze(Polygon grenze) {
		this.grenze = grenze;
	}
	
	
}
