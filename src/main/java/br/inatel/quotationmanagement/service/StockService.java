package br.inatel.quotationmanagement.service;

import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import br.inatel.quotationmanagement.model.Stock;
import lombok.extern.slf4j.Slf4j;

@Service
public class StockService {

	private RestTemplate restTemplate = new RestTemplate();
	private String stockManagerURL;
	
	@Autowired
	public StockService(@Value("${stock-manager.url}") String stockManagerURL) {
		this.stockManagerURL = stockManagerURL;
	}
	
	@Cacheable(value = "stocks")
	public List<Stock> getStockList() {
		//Obtendo lista de ações da API do Gerenciador de ações
		Stock[] stockList = restTemplate.getForObject(stockManagerURL + "/stock", Stock[].class);
		//Lista de ações buscada com sucesso na API do Gerenciador de ações
		return Arrays.asList(stockList);
	}
	
	// @EventListener(ContextRefreshedEvent.class)
	@EventListener(ApplicationReadyEvent.class)
	public void register() {
		//Registrando o Gerenciador de Cotações na API do Gerenciador de Estoque
		HttpHeaders header = new HttpHeaders();
		header.setContentType(MediaType.APPLICATION_JSON);
		
		JSONObject body = new JSONObject();
		body.put("host", "localhost");
		body.put("port", 8081);
		
		HttpEntity<String> request = new HttpEntity<String>(body.toString(), header);

		restTemplate.postForObject(stockManagerURL + "/notification", request, String.class);
		//Gerenciador de cotações registrado com sucesso na API do Gerenciador de ações
	}
	
}
