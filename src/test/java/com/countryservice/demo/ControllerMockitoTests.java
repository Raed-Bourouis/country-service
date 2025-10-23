package com.countryservice.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.countryservice.demo.beans.Country;
import com.countryservice.demo.controllers.CountryController;
import com.countryservice.demo.services.CountryService;

@SpringBootTest(classes= {ControllerMockitoTests.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ControllerMockitoTests {
	
	@Mock
	CountryService countryService;
	
	@InjectMocks
	CountryController countryController;
	
	public List<Country> mycountries;
	Country country;
	
	@Test
	@Order(1)
	public void test_getAllCountries() {
		mycountries = new ArrayList<Country>();
		mycountries.add(new Country(1,"India","Delhi"));
		mycountries.add(new Country(2,"USA","Washington"));		
		
		when(countryService.getAllCountries()).thenReturn(mycountries);//Mocking
		ResponseEntity<List<Country>> res = countryController.getCountries();
		
		assertEquals(HttpStatus.FOUND, res.getStatusCode());//si c
		assertEquals(2, res.getBody().size());
		
	}
	
	@Test
	@Order(2)
	public void test_getCountryByID() {
		country = new Country(1,"India","Delhi");
		
		int countryID=1;
		when(countryService.getCountryById(countryID)).thenReturn(country);//Mocking
		ResponseEntity<Country> res = countryController.getCountryById(countryID);
		
		assertEquals(HttpStatus.FOUND, res.getStatusCode());
		assertEquals(1, res.getBody().getIdCountry());
		
	}
	
	@Test
	@Order(3)
	public void test_getCountryByName() {
		country = new Country(1,"India","Delhi");
		
		String countryName="India";
		when(countryService.getCountryByName(countryName)).thenReturn(country);//Mocking
		ResponseEntity<Country> res = countryController.getCountryByName(countryName);
		
		assertEquals(HttpStatus.FOUND, res.getStatusCode());
		assertEquals(countryName, res.getBody().getName());
		
	}
	
	@Test
	@Order(4)
	public void test_addCountry() {
		country = new Country(3,"Germany","Berlin");
		
		when(countryService.addCountry(country)).thenReturn(country);//Mocking
		ResponseEntity<Country> res = countryController.addCountry(country);
		
		assertEquals(HttpStatus.CREATED, res.getStatusCode());
		assertEquals(country, res.getBody());
		
	}
	
	@Test
	@Order(5)
	public void test_updateCountry() {
	    int countryID = 3;

	    // Objet avant la mise à jour
	    Country existingCountry = new Country(countryID, "OldName", "OldCapital");

	    // Objet après la mise à jour
	    Country updatedCountry = new Country(countryID, "Japan", "Tokyo");

	    // Mocks
	    when(countryService.getCountryById(countryID)).thenReturn(existingCountry);
	    when(countryService.updateCountry(any(Country.class))).thenReturn(updatedCountry);

	    // Appel du contrôleur
	    ResponseEntity<Country> res = countryController.updateCountry(countryID, updatedCountry);

	    // Vérifications
	    assertEquals(HttpStatus.OK, res.getStatusCode());
	    assertEquals("Japan", res.getBody().getName());
	    assertEquals("Tokyo", res.getBody().getCapital());
	}

	
	@Test
	@Order(6)
	public void test_deleteCountry() {
		country = new Country(3,"Japan","Tokyo");
		int countryID = 3;
		
		when(countryService.getCountryById(countryID)).thenReturn(country);//Mocking
		
		ResponseEntity<Country> res = countryController.deleteCountry(countryID);
		
		assertEquals(HttpStatus.OK, res.getStatusCode());
		
	}

}
