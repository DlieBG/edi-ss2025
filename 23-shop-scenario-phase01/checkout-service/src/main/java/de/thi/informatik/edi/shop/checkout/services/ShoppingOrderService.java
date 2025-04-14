package de.thi.informatik.edi.shop.checkout.services;

import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.thi.informatik.edi.shop.checkout.connectors.KafkaProducer;
import de.thi.informatik.edi.shop.checkout.connectors.dto.AddCartEntryDto;
import de.thi.informatik.edi.shop.checkout.connectors.dto.RemoveCartEntryDto;
import de.thi.informatik.edi.shop.checkout.connectors.dto.UpdatePaymentDto;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import de.thi.informatik.edi.shop.checkout.model.ShoppingOrder;
import de.thi.informatik.edi.shop.checkout.model.ShoppingOrderStatus;
import de.thi.informatik.edi.shop.checkout.repositories.ShoppingOrderRepository;
import jakarta.annotation.PostConstruct;

@Service
public class ShoppingOrderService {
	private ShoppingOrderRepository orders;
	private KafkaProducer producer;

	public ShoppingOrderService(@Autowired ShoppingOrderRepository orders, @Autowired KafkaProducer producer) {
		this.orders = orders;
		this.producer = producer;
	}
	
	@PostConstruct
	private void init() {

	}

	@SneakyThrows
    @KafkaListener(groupId = "checkout-service", topics = "shopping-cart-add")
	private void receiveAddShoppingCard(String dtoJson) {
		ObjectMapper objectMapper = new ObjectMapper();
		AddCartEntryDto dto = objectMapper.readValue(dtoJson, AddCartEntryDto.class);

		addItemToOrderByCartRef(
				dto.getCartId(),
				dto.getArticleId(),
				dto.getName(),
				dto.getPrice(),
				dto.getCount()
		);
	}

	@SneakyThrows
	@KafkaListener(groupId = "checkout-service", topics = "shopping-cart-remove")
	private void receiveRemoveShoppingCard(String dtoJson) {
		ObjectMapper objectMapper = new ObjectMapper();
		RemoveCartEntryDto dto = objectMapper.readValue(dtoJson, RemoveCartEntryDto.class);

		deleteItemFromOrderByCartRef(
				dto.getCartId(),
				dto.getArticleId()
		);
	}

	@SneakyThrows
	@KafkaListener(groupId = "checkout-service", topics = "payment-update")
	private void receivePaymentUpdate(String dtoJson) {
		ObjectMapper objectMapper = new ObjectMapper();
		UpdatePaymentDto dto = objectMapper.readValue(dtoJson, UpdatePaymentDto.class);

		updateOrderIsPayed(dto.getOrderId());
	}
	
	public void addItemToOrderByCartRef(UUID cartRef, UUID article, String name, double price, int count) {
		ShoppingOrder order = this.getOrCreate(cartRef);
		order.addItem(article, name, price, count);
		this.orders.save(order);
	}
	
	public void deleteItemFromOrderByCartRef(UUID cartRef, UUID article) {
		ShoppingOrder order = this.getOrCreate(cartRef);
		order.removeItem(article);
		this.orders.save(order);
	}

	private Optional<ShoppingOrder> findByCartRef(UUID cartRef) {
		return this.orders.findByCartRef(cartRef);
	}

	public ShoppingOrder createOrderWithCartRef(UUID cartRef) {
		ShoppingOrder order = this.getOrCreate(cartRef);
		this.orders.save(order);
		return order;
	}

	private ShoppingOrder getOrCreate(UUID cartRef) {
		Optional<ShoppingOrder> orderOption = this.findByCartRef(cartRef);
		ShoppingOrder order;
		if(orderOption.isEmpty()) {
			order = new ShoppingOrder(cartRef);
		} else {
			order = orderOption.get();
		}
		return order;
	}

	public void updateOrder(UUID id, String firstName, String lastName, String street, String zipCode,
			String city) {
		ShoppingOrder order = findById(id);
		order.update(firstName, lastName, street, zipCode, city);
		this.orders.save(order);
	}

	private ShoppingOrder findById(UUID id) {
		Optional<ShoppingOrder> optional = this.orders.findById(id);
		if(optional.isEmpty()) {
			throw new IllegalArgumentException("Unknown order with id " + id.toString());
		}
		ShoppingOrder order = optional.get();
		return order;
	}

	public void placeOrder(UUID id) {
		ShoppingOrder order = findById(id);
		if(order.getStatus().equals(ShoppingOrderStatus.CREATED)) {
			order.setStatus(ShoppingOrderStatus.PLACED);

			this.producer.updateOrder(order);

			this.orders.save(order);
		}
	}

	public ShoppingOrder find(UUID id) {
		return this.findById(id);
	}

	public void updateOrderIsPayed(UUID id) {
		ShoppingOrder order = this.findById(id);
		if(order.getStatus() == ShoppingOrderStatus.PLACED) {
			order.setStatus(ShoppingOrderStatus.PAYED);
			this.orders.save(order);
		}
	}

	public void updateOrderIsShipped(UUID id) {
		ShoppingOrder order = this.findById(id);
		if(order.getStatus() == ShoppingOrderStatus.PAYED) {
			order.setStatus(ShoppingOrderStatus.SHIPPED);
			this.orders.save(order);
		}
	}
}
