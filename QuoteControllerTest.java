package br.inatel.quotationmanagement.controller;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import br.inatel.quotationmanagement.model.Stock;
import br.inatel.quotationmanagement.service.StockService;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class QuoteControllerTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private StockService stockService;
	private List<Stock> stockList;
	
	@BeforeEach
	public void beforeEach() {
		this.stockList = new ArrayList<>();
		this.stockList.add(new Stock("petr4", "Petrobras PN"));
		this.stockList.add(new Stock("vale5", "Vale do Rio Doce PN"));
	}
	
	@Test
	void shouldCreateNewOperation() throws Exception {
		Mockito.when(stockService.getStockList()).thenReturn(stockList);
		
		JSONObject quotes = new JSONObject();
		quotes.put("2021-01-04", "09");
		quotes.put("2021-02-05", "08");
		quotes.put("2021-03-06", "07");

		JSONObject body = new JSONObject();
		body.put("stockId", "petr4");
		body.put("quotes", quotes);
		
		mockMvc.perform(
				MockMvcRequestBuilders.post("/quote").content(body.toString()).contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().is(201))
				.andExpect(MockMvcResultMatchers.jsonPath("$.stockId").value("petr4"));
	}
	
	@Test
	void shouldNotCreateNewOperation() throws Exception {
		Mockito.when(stockService.getStockList()).thenReturn(stockList);
		
		JSONObject quotes = new JSONObject();
		quotes.put("2021-01-01", "09");
		quotes.put("2021-02-02", "08");
		quotes.put("2021-03-03", "07");

		JSONObject body = new JSONObject();
		body.put("stockId", "wrong6");
		body.put("quotes", quotes);
		
		mockMvc.perform(
				MockMvcRequestBuilders.post("/quote").content(body.toString()).contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().is(404))
				.andExpect(MockMvcResultMatchers.jsonPath("$.error").value("There isn't a stock with this id : wrong6"));
	}
	
	@Test
	void shouldListByStockId() throws Exception {
		Mockito.when(stockService.getStockList()).thenReturn(stockList);

		mockMvc.perform(MockMvcRequestBuilders.get("/quote/petr4").contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().is(200))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].stockId").value("petr4"));
	}
	
	@Test
	void shouldListAll() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/quote").contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().is(200));
	}
	
	@Test
	void shouldNotListByStockId() throws Exception {
		Mockito.when(stockService.getStockList()).thenReturn(stockList);

		mockMvc.perform(MockMvcRequestBuilders.get("/quote/wrong6").contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().is(404))
				.andExpect(MockMvcResultMatchers.jsonPath("$.field").value("stockId"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.error").value("No quotes found for the stock : wrong6"));
	}

}