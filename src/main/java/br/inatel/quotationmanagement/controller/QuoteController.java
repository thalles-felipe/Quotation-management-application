package br.inatel.quotationmanagement.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import br.inatel.quotationmanagement.config.validation.ErrorFormDto;
import br.inatel.quotationmanagement.controller.dto.OperationDto;
import br.inatel.quotationmanagement.controller.form.OperationForm;
import br.inatel.quotationmanagement.model.Operation;
import br.inatel.quotationmanagement.model.Quote;
import br.inatel.quotationmanagement.model.Stock;
import br.inatel.quotationmanagement.repository.OperationRepository;
import br.inatel.quotationmanagement.repository.QuoteRepository;
import br.inatel.quotationmanagement.service.StockService;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/quote")
public class QuoteController {

	@Autowired
	private OperationRepository operationRepository;
	@Autowired
	private QuoteRepository quoteRepository;
	@Autowired
	private StockService stockService;

	@PostMapping
	@Transactional
	public ResponseEntity<?> create(@RequestBody @Valid OperationForm form, UriComponentsBuilder uriBuilder) {
		// Início da criação de uma nova operação de cotações
		List<Stock> stockList = stockService.getStockList();
		List<String> stockIdList = stockList.stream().map(Stock::getId).collect(Collectors.toList());

		if (!stockIdList.contains(form.getStockId())) {
			// Stock Id enviado não registrado no API do Stock Manager
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ErrorFormDto("stockId", "There isn't a stock with this id : " + form.getStockId()));
		}

		if (!form.isQuotesDatesValid()) {
			// Quotação com data inválida encontrada
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorFormDto("quotes", "Invalid quote date"));
		}

		if (!form.isQuotesValuesValid()) {
			// Quotação com valor inválido encontrada
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new ErrorFormDto("quotes", "Invalid quote value"));
		}

		for (Map.Entry<String, String> quoteEntry : form.getQuotes().entrySet()) {
			Quote existingQuote = quoteRepository.findByOperationStockIdAndDate(form.getStockId(),
					LocalDate.parse(quoteEntry.getKey()));

			if (existingQuote != null) {
				// Quotação com data já registrada encontrada
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(new ErrorFormDto("quotes", "Already exist a quote registered on the date "
								+ quoteEntry.getKey() + " for the stock : " + form.getStockId()));
			}
		}

		Operation operation = new Operation();
		operation.setStockId(form.getStockId());

		List<Quote> quotes = form.generateQuoteList(operation);
		operation.setQuotes(quotes);

		operation = operationRepository.save(operation);
		// Quotação Operação criada com sucesso
		URI uri = uriBuilder.path("/stocks/{id}").buildAndExpand(form.getStockId()).toUri();
		return ResponseEntity.created(uri).body(new OperationDto(operation));
	}

	@GetMapping("/{stockId}")
	public ResponseEntity<?> getByStockId(@PathVariable("stockId") String stockId) {
		// Busca da operação de quotação com a Id requisitada começou
		List<Operation> operationList = operationRepository.findByStockId(stockId);

		if (operationList.isEmpty()) {
			// Quotas com o Stock Id não encontradas
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ErrorFormDto("stockId", "No quotes found for the stock : " + stockId));
		}
		// Operações de quotação com o ID de estoque solicitado buscado com sucesso
		return ResponseEntity.ok(OperationDto.convertToList(operationList));
	}

	@GetMapping()
	public ResponseEntity<?> list() {
		// A busca de todas as operações de quotação iniciada
		List<Operation> operationList = operationRepository.findAll();

		if (operationList.isEmpty()) {
			// Nenhuma operação de cotação registrada no banco de dados ainda
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ErrorFormDto("", "No operations registered on database"));
		}
		// Todas as operações de cotação buscadas com sucesso
		return ResponseEntity.ok(OperationDto.convertToList(operationList));
	}

}
