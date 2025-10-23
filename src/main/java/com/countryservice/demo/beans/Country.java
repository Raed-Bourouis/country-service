package com.countryservice.demo.beans;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity

@Table(name="country")
public class Country {
	
	@Id
	@Column(name="id")
	private int idCountry;
	@Column(name="name_country")
	private String name;
	@Column(name="capital_name")
	private String Capital;
	public Country(int idCountry, String name, String capital) {
	    this.idCountry = idCountry;
	    this.name = name;
	    this.Capital = capital;
	}
	public Country() {
	    // constructeur vide nécessaire pour Hibernate
	}

	public int getIdCountry() {
		return idCountry;
	}
	public void setIdCountry(int idCountry) {
		this.idCountry = idCountry;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCapital() {
		return Capital;
	}
	public void setCapital(String capital) {
		Capital = capital;
	}
	

}
