package de.thi.informatik.edi.shop.checkout.services;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.thi.informatik.edi.shop.checkout.services.messages.ShippingMessage;
import reactor.core.publisher.Flux;

@Service
public class ShippingMessageConsumerService {

	private static Logger logger = LoggerFactory.getLogger(ShippingMessageConsumerService.class);

	Flux<ShippingMessage> flux;

	@Value("${kafka.shippingTopic:shipping}")
	private String topic;
	
    private final MessageConsumerService consumer;

    public ShippingMessageConsumerService(@Autowired MessageConsumerService consumer) {
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
								.readValue(value, ShippingMessage.class);
					} catch (JsonProcessingException e) {
						e.printStackTrace();
					}

					return new ShippingMessage();
				});
	}

	public Flux<ShippingMessage> getShippedShippingMessages() {
		return this.flux
				.filter(message -> message.getStatus().equals("SHIPPED"));
	}
}
