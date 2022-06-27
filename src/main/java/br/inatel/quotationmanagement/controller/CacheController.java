package br.inatel.quotationmanagement.controller;

import javax.transaction.Transactional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/stockcache")
public class CacheController {
	
	@DeleteMapping
	@Transactional
	@CacheEvict(value = "stocks", allEntries = true)
	public ResponseEntity<?> cleanCache() {
		//Limpando a lista de ações da cache
		return ResponseEntity.status(204).body("Cache succesfully cleaned");
	}

}
