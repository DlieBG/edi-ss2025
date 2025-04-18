package de.thi.informatik.edi.shop.shopping.services;

import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.thi.informatik.edi.shop.shopping.connectors.dto.UpdateStockDto;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import de.thi.informatik.edi.shop.shopping.model.Stock;
import de.thi.informatik.edi.shop.shopping.repositories.StockRepository;

@Service
public class StockService {
	private StockRepository stocks;
	public StockService(@Autowired StockRepository stocks) {
		this.stocks = stocks;
	}

	@SneakyThrows
	@KafkaListener(groupId = "shopping-service", topics = "stock-update")
	private void receiveStockUpdate(String dtoJson) {
		ObjectMapper objectMapper = new ObjectMapper();
		UpdateStockDto dto = objectMapper.readValue(dtoJson, UpdateStockDto.class);

		updateStock(
				dto.getArticleId(),
				dto.getStock()
		);
	}
	
	private Stock getOrCreate(UUID article) {
		Optional<Stock> optional = this.stocks.findById(article);
		Stock stock;
		if(optional.isEmpty()) {
			stock = new Stock(article);
		} else {
			stock = optional.get();
		}
		return stock;
	}
	
	public void updateStock(UUID article, double value) {
		Stock stock = this.getOrCreate(article);
		stock.setStock(value);
		this.stocks.save(stock);
	}
	
	public Iterable<Stock> list() {
		return this.stocks.findAll();
	}
}
