package de.thi.informatik.edi.shop.checkout.services;

import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.thi.informatik.edi.shop.checkout.services.messages.ArticleAddedToCartMessage;
import de.thi.informatik.edi.shop.checkout.services.messages.CartMessage;
import de.thi.informatik.edi.shop.checkout.services.messages.CreatedCartMessage;
import de.thi.informatik.edi.shop.checkout.services.messages.DeleteArticleFromCartMessage;
import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Flux;

@Service
public class CartMessageConsumerService {
	
	private static Logger logger = LoggerFactory.getLogger(CartMessageConsumerService.class);

	Flux<CartMessage> flux;

	@Value("${kafka.cartTopic:cart}")
	private String topic;
	
	private MessageConsumerService consumer;

	public CartMessageConsumerService(@Autowired MessageConsumerService consumer) {
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
                                .readValue(value, CartMessage.class);
                    } catch (JsonProcessingException e) {
						e.printStackTrace();
                    }

					return new CartMessage();
                });
	}

	public Flux<CreatedCartMessage> getCreatedCartMessages() {
		return this.flux
				.filter(message -> message instanceof CreatedCartMessage)
				.map(message -> (CreatedCartMessage) message);
	}

	public Flux<ArticleAddedToCartMessage> getArticleAddedToCartMessages() {
		return this.flux
				.filter(message -> message instanceof ArticleAddedToCartMessage)
				.map(message -> (ArticleAddedToCartMessage) message);
	}

	public Flux<DeleteArticleFromCartMessage> getDeleteArticleFromCartMessages() {
		return this.flux
				.filter(message -> message instanceof DeleteArticleFromCartMessage)
				.map(message -> (DeleteArticleFromCartMessage) message);
	}
}
