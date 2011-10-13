package de.fraunhofer.igd.klarschiff.vo;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Configurable;

@Configurable
@Entity
public class Trashmail {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String pattern;

	@PersistenceContext
    transient EntityManager entityManager;
	
	

	/* --------------- GET + SET ----------------------------*/

	public Long getId() {
        return this.id;
    }

	public void setId(Long id) {
        this.id = id;
    }

	public String getPattern() {
        return this.pattern;
    }

	public void setPattern(String pattern) {
        this.pattern = pattern;
    }
}
