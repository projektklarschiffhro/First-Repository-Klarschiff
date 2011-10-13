package de.fraunhofer.igd.klarschiff.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.format.annotation.DateTimeFormat;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

@SuppressWarnings("serial")
@Entity
@Configurable
public class Vorgang implements Serializable {

	/* --------------- Attribute ----------------------------*/
	
	@Id
	@TableGenerator(
            name="VorgangSequence", 
            table="klarschiff_VorgangSequence",
            initialValue=1,
            allocationSize=1)
    @GeneratedValue(strategy=GenerationType.TABLE, generator="VorgangSequence")
    private Long id;

	@Version
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(style = "S-")
    private Date version;

	@NotNull
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(style = "S-")
	private Date datum;

	@NotNull
	@Enumerated(EnumType.STRING)
	private EnumVorgangTyp typ;

	@Size(max = 300)
    private String betreff;

	@NotNull
	@Enumerated(EnumType.STRING)
	private EnumFreigabeStatus betreffFreigabeStatus = EnumFreigabeStatus.intern;
	
    @Lob
    @Type(type="org.hibernate.type.TextType")
    private String details;

    @NotNull
    @Enumerated(EnumType.STRING)
    private EnumFreigabeStatus detailsFreigabeStatus = EnumFreigabeStatus.intern;

    @Type(type = "org.hibernatespatial.GeometryUserType")
    private Point ovi;
    
    @Size(max = 300)
    private String autorEmail;

	@Size(max = 32)
    private String hash;

    @NotNull
    @Enumerated(EnumType.STRING)
    private EnumVorgangStatus status;
    
    @SuppressWarnings("unused")
	@NotNull
    private EnumVorgangStatus statusOrdinal;
    
	@Size(max = 300)
    private String statusKommentar;
    
    @Type(type="org.hibernate.type.BinaryType") 
    byte[] fotoNormalJpg;

    @Type(type="org.hibernate.type.BinaryType") 
    byte[] fotoThumbJpg;
    
    @Enumerated(EnumType.STRING)
    private EnumFreigabeStatus fotoFreigabeStatus = EnumFreigabeStatus.intern;;
    
    String zustaendigkeit;
    
	@Enumerated(EnumType.STRING)
    EnumZustaendigkeitStatus zustaendigkeitStatus;

	String delegiertAn;
	
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "vorgang")
    private List<Kommentar> kommentare = new ArrayList<Kommentar>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "vorgang")
    private List<Verlauf> verlauf = new ArrayList<Verlauf>();

    @ManyToOne
    private Kategorie kategorie;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "vorgang")
    private List<Unterstuetzer> unterstuetzer = new ArrayList<Unterstuetzer>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "vorgang")
    @OrderBy("datum ASC")
    private List<Missbrauchsmeldung> missbrauchsmeldungen = new ArrayList<Missbrauchsmeldung>();
    
    @NotNull
	@Enumerated(EnumType.STRING)
    EnumPrioritaet prioritaet;
    
    @NotNull
    EnumPrioritaet prioritaetOrdinal;
    
    Boolean archiviert;
    
	/* --------------- transient ----------------------------*/
    
    @Transient
    private static GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 25833);
    
    @Transient
    private static WKTReader wktReader = new WKTReader(geometryFactory);
    
    @Transient
    private static WKTWriter wktWriter = new WKTWriter();

    @Transient
    public void setOviWkt(String oviWkt) throws Exception {
    	ovi = (StringUtils.isBlank(oviWkt)) ? null : (Point)wktReader.read(oviWkt);
    }

    @Transient
    public String getOviWkt() {
    	return (ovi==null) ? null : wktWriter.write(ovi);
    }
    
    @Transient
    public boolean getFotoExists() {
    	return (fotoNormalJpg!=null);
    }
    
	
	/* --------------- GET + SET ----------------------------*/


	public Long getId() {
        return this.id;
    }

	public void setId(Long id) {
        this.id = id;
    }

	public Date getVersion() {
        return this.version;
    }

	public void setVersion(Date version) {
        this.version = version;
    }


	public String getBetreff() {
        return this.betreff;
    }

	public void setBetreff(String betreff) {
        this.betreff = betreff;
    }

	public String getDetails() {
        return this.details;
    }

	public void setDetails(String details) {
        this.details = details;
    }

	public EnumFreigabeStatus getBetreffFreigabeStatus() {
        return this.betreffFreigabeStatus;
    }

	public void setBetreffFreigabeStatus(EnumFreigabeStatus betreffFreigabeStatus) {
        this.betreffFreigabeStatus = betreffFreigabeStatus;
    }

	public EnumFreigabeStatus getDetailsFreigabeStatus() {
        return this.detailsFreigabeStatus;
    }

	public void setDetailsFreigabeStatus(EnumFreigabeStatus detailsFreigabeStatus) {
        this.detailsFreigabeStatus = detailsFreigabeStatus;
    }

	public Point getOvi() {
        return this.ovi;
    }

	public void setOvi(Point ovi) {
        this.ovi = ovi;
    }

	public EnumVorgangTyp getTyp() {
        return this.typ;
    }

	public void setTyp(EnumVorgangTyp typ) {
        this.typ = typ;
    }

	public Date getDatum() {
        return this.datum;
    }

	public void setDatum(Date datum) {
        this.datum = datum;
    }

	public String getAutorEmail() {
        return this.autorEmail;
    }

	public void setAutorEmail(String autorEmail) {
        this.autorEmail = autorEmail;
    }

	public List<Kommentar> getKommentare() {
        return this.kommentare;
    }

	public void setKommentare(List<Kommentar> kommentare) {
        this.kommentare = kommentare;
    }

	public List<Verlauf> getVerlauf() {
        return this.verlauf;
    }

	public void setVerlauf(List<Verlauf> verlauf) {
        this.verlauf = verlauf;
    }

	public Kategorie getKategorie() {
        return this.kategorie;
    }

	public void setKategorie(Kategorie kategorie) {
        this.kategorie = kategorie;
    }

	public List<Unterstuetzer> getUnterstuetzer() {
        return this.unterstuetzer;
    }

	public void setUnterstuetzer(List<Unterstuetzer> unterstuetzer) {
        this.unterstuetzer = unterstuetzer;
    }

	public String getZustaendigkeit() {
		return zustaendigkeit;
	}

	public void setZustaendigkeit(String zustaendigkeit) {
		this.zustaendigkeit = zustaendigkeit;
	}

	public void setZustaendigkeitStatus(EnumZustaendigkeitStatus zustaendigkeitStatus) {
		this.zustaendigkeitStatus = zustaendigkeitStatus;
	}

	public EnumVorgangStatus getStatus() {
		return status;
	}

	public void setStatus(EnumVorgangStatus status) {
		this.status = status;
		this.statusOrdinal = status;
	}

	public EnumZustaendigkeitStatus getZustaendigkeitStatus() {
		return zustaendigkeitStatus;
	}

	public EnumPrioritaet getPrioritaet() {
		return prioritaet;
	}

	public void setPrioritaet(EnumPrioritaet prioritaet) {
		this.prioritaet = prioritaet;
		this.prioritaetOrdinal = prioritaet;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public byte[] getFotoNormalJpg() {
		return fotoNormalJpg;
	}

	public void setFotoNormalJpg(byte[] fotoNormalJpg) {
		this.fotoNormalJpg = fotoNormalJpg;
	}

	public byte[] getFotoThumbJpg() {
		return fotoThumbJpg;
	}

	public void setFotoThumbJpg(byte[] fotoThumbJpg) {
		this.fotoThumbJpg = fotoThumbJpg;
	}

	public EnumFreigabeStatus getFotoFreigabeStatus() {
		return fotoFreigabeStatus;
	}

	public void setFotoFreigabeStatus(EnumFreigabeStatus fotoFreigabeStatus) {
		this.fotoFreigabeStatus = fotoFreigabeStatus;
	}

	public List<Missbrauchsmeldung> getMissbrauchsmeldungen() {
		return missbrauchsmeldungen;
	}

	public void setMissbrauchsmeldungen(List<Missbrauchsmeldung> missbrauchsmeldungen) {
		this.missbrauchsmeldungen = missbrauchsmeldungen;
	}

	public String getStatusKommentar() {
		return statusKommentar;
	}

	public void setStatusKommentar(String statusKommentar) {
		this.statusKommentar = statusKommentar;
	}

	public String getDelegiertAn() {
		return delegiertAn;
	}

	public void setDelegiertAn(String delegiertAn) {
		this.delegiertAn = delegiertAn;
	}

	public Boolean getArchiviert() {
		return archiviert;
	}

	public void setArchiviert(boolean archiviert) {
		this.archiviert = archiviert;
	}



}
