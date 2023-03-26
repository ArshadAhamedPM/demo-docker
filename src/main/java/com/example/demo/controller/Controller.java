package com.example.demo.controller;

import java.io.Serializable;
import java.time.Duration;
import java.util.List;

import javax.xml.crypto.Data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Country;
import com.example.demo.repo.CountryRepo;

import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;

@RestController
public class Controller {

	// redis-cli to start redis servier

	@Autowired
	CountryRepo countryRepo;

	@GetMapping("/countries-org")
	public List<Country> findAll() {
		return countryRepo.findAll();
	}

	@GetMapping("/country/{id}")
	@Cacheable(key = "#id", value = "country")
	public Country findById(@PathVariable String id) {
		System.out.println("Calling by id -" + id);
		return countryRepo.findById(Integer.parseInt(id)).get();
	}

	@PostMapping("/country/update")
	@CachePut(key = "#country.id", value = "country")
	public Country update(@RequestBody Country country) {
		System.out.println("Updating country " + country.toString());
		countryRepo.save(country);
		return country;

	}

//	@GetMapping(value="/feed",produces = "text/event-stream")
//	public Flux<List<Country>> getList(){
//		 return Flux.interval(Duration.ofSeconds(1)).flatMap(ignore->countryRepo.findAll()); // emit a value every second
//		         
//		
//	}

	@GetMapping(value = "/countries", produces = "text/event-stream")
	public Flux<Country> getCountries() {
		System.out.println("calling flux");
		return Flux.interval(Duration.ofSeconds(5)).flatMap(ignore -> Flux.fromIterable(countryRepo.findAll()))
				.buffer(1) // buffer 100 elements and emit a list of Countries
				.flatMapIterable(list -> list).doOnComplete(()->System.out.println("Completed")); // flatten the list to a stream of Countries
	}
	
	@CrossOrigin(origins = "http://localhost:4200")
	@GetMapping(value = "/demo", produces = "text/event-stream")
	public Flux<Country> demo() {
	    System.out.println("calling demo");
	    return Flux.interval(Duration.ofSeconds(10))
	        .flatMap(ignore -> Flux.fromIterable(countryRepo.findAll()));
	}

}
