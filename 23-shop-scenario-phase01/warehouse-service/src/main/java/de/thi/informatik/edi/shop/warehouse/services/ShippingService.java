package de.thi.informatik.edi.shop.warehouse.services;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.thi.informatik.edi.shop.warehouse.connectors.dto.UpdateOrderDto;
import de.thi.informatik.edi.shop.warehouse.connectors.dto.UpdateOrderStatusDto;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import de.thi.informatik.edi.shop.warehouse.model.Shipping;
import de.thi.informatik.edi.shop.warehouse.repositories.ShippingRepository;

@Service
public class ShippingService {
	
	private ShippingRepository repository;

	public ShippingService(@Autowired ShippingRepository repository) {
		this.repository = repository;
	}

	@SneakyThrows
	@KafkaListener(groupId = "warehouse-service", topics = "order-update")
	private void receiveOrderUpdate(String dtoJson) {
		System.out.println(dtoJson);
		ObjectMapper objectMapper = new ObjectMapper();
		UpdateOrderDto dto = objectMapper.readValue(dtoJson, UpdateOrderDto.class);

		if (dto.getStatus() == UpdateOrderStatusDto.PAYED) {
			Shipping shipping = updateFromOrder(
					dto.getOrderId(),
					dto.getFirstName(),
					dto.getLastName(),
					dto.getStreet(),
					dto.getZipCode(),
					dto.getCity()
			);

			System.out.println(shipping.getId());

			for (var item: dto.getItems())
				addArticleByOrderRef(
						dto.getOrderId(),
						item.getArticleId(),
						item.getCount()
				);
		}
	}


	public Shipping updateFromOrder(UUID orderRef, String firstName, String lastName, String street, String zipCode,
			String city) {
		Shipping shipping = getOrCreateByOrderRef(orderRef);
		shipping.update(orderRef, firstName, lastName, street, zipCode, city);
		this.repository.save(shipping);
		return shipping;
	}

	private Shipping getOrCreateByOrderRef(UUID orderRef) {
		Optional<Shipping> optional = this.repository.findByOrderRef(orderRef);
		if(optional.isEmpty()) {			
			return new Shipping();
		} else {
			return optional.get();
		}
	}
	
	public void addArticlesByOrderRef(UUID orderRef, Consumer<Shipping> addToShipping) {
		Shipping shipping = this.getOrCreateByOrderRef(orderRef);
		addToShipping.accept(shipping);
		this.repository.save(shipping);
	}
	
	public void addArticleByOrderRef(UUID orderRef, UUID article, int count) {
		Shipping shipping = this.getOrCreateByOrderRef(orderRef);
		shipping.addArticle(article, count);
		this.repository.save(shipping);
	}

	public Iterable<Shipping> getShippings() {
		return this.repository.findAll();
	}

	public void doShipping(UUID id) {
		Optional<Shipping> optional = this.repository.findById(id);
		if(optional.isPresent()) {
			Shipping shipping = optional.get();
			shipping.doShipping();

			

			this.repository.save(shipping);
		} else {
			throw new IllegalArgumentException("Unknown shipping for ID " + id);
		}
	}

}
