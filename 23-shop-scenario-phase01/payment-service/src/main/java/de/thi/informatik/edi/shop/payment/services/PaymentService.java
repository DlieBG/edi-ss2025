package de.thi.informatik.edi.shop.payment.services;

import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.thi.informatik.edi.shop.payment.connectors.KafkaProducer;
import de.thi.informatik.edi.shop.payment.connectors.dto.UpdateOrderDto;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import de.thi.informatik.edi.shop.payment.model.Payment;
import de.thi.informatik.edi.shop.payment.model.PaymentStatus;
import de.thi.informatik.edi.shop.payment.repositories.PaymentRepository;

@Service
public class PaymentService {

	private PaymentRepository payments;

	private KafkaProducer producer;

	public PaymentService(@Autowired PaymentRepository payments, @Autowired KafkaProducer producer) {
		this.payments = payments;
		this.producer = producer;
	}

	@SneakyThrows
	@KafkaListener(groupId = "payment-service", topics = "order-update")
	private void receiveOrderUpdate(String dtoJson) {
		ObjectMapper objectMapper = new ObjectMapper();
		UpdateOrderDto dto = objectMapper.readValue(dtoJson, UpdateOrderDto.class);

		Payment payment = getOrCreateByOrderRef(dto.getOrderId());

		updatePrice(
				payment.getId(),
				dto.getPrice()
		);

		updateData(
				payment.getId(),
				dto.getFirstName(),
				dto.getLastName(),
				dto.getStreet(),
				dto.getZipCode(),
				dto.getCity()
		);
	}

	public Payment findById(UUID id) {
		Optional<Payment> optional = this.payments.findById(id);
		if (optional.isEmpty()) {
			throw new IllegalArgumentException("Unknown payment with ID " + id.toString());
		}
		return optional.get();
	}

	public Payment getOrCreateByOrderRef(UUID orderRef) {
		Optional<Payment> optional = this.payments.findByOrderRef(orderRef);
		Payment payment;
		if (optional.isEmpty()) {
			payment = new Payment(orderRef);
		} else {
			payment = optional.get();
		}
		this.payments.save(payment);
		return payment;
	}

	public void pay(UUID id) {
		Payment payment = this.findById(id);
		PaymentStatus before = payment.getStatus();
		payment.pay();

		this.producer.updatePayment(payment);

		this.payments.save(payment);
	}

	public void updatePrice(UUID id, double price) {
		Payment payment = this.findById(id);
		PaymentStatus before = payment.getStatus();
		payment.setPrice(price);
		this.payments.save(payment);
	}

	public void updateData(UUID id, String firstName, String lastName, String street, String zipCode, String city) {
		Payment payment = this.findById(id);
		payment.update(firstName, lastName, street, zipCode, city);
		this.payments.save(payment);
	}

}
