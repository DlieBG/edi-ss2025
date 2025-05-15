package de.thi.informatik.edi.shop.checkout.services;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.List;
import java.util.Properties;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.thi.informatik.edi.shop.checkout.services.messages.PaymentMessage;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import reactor.core.publisher.Flux;

@Service
public class PaymentMessageConsumerService {

	private static Logger logger = LoggerFactory.getLogger(PaymentMessageConsumerService.class);

	Flux<PaymentMessage> flux;

	@Value("${kafka.paymentTopic:payment}")
	private String topic;
	
	private MessageConsumerService consumer;

	public PaymentMessageConsumerService(@Autowired MessageConsumerService consumer) {
		this.consumer = consumer;
	}

	@PostConstruct
	private void init() {
		this.flux = this.consumer.register(topic)
				.map(message -> {
					String value = message.value();
					logger.info("Received message " + value);
					try {
						return new ObjectMapper()
								.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
								.readValue(value, PaymentMessage.class);
					} catch (JsonProcessingException e) {
						e.printStackTrace();
					}

					return new PaymentMessage();
				});
	}

	public Flux<PaymentMessage> getPayedPaymentMessages() {
		return this.flux
				.filter(message -> message.getStatus().equals("PAYED"));
	}

	public Flux<PaymentMessage> getPayablePaymentMessages() {
		return this.flux
				.filter(message -> message.getStatus().equals("PAYABLE"));
	}

	public Flux<PaymentMessage> getUnknownPaymentMessages() {
		return this.flux
				.filter(message -> !message.getStatus().equals("PAYED"))
				.filter(message -> !message.getStatus().equals("PAYABLE"));
	}
}
