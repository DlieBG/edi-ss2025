package de.thi.informatik.edi.shop.checkout.services;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.*;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
public class MessageConsumerService {
	private static final Logger logger = LoggerFactory.getLogger(ShippingMessageConsumerService.class);

	Sinks.Many<ConsumerRecord<String, String>> many = Sinks.many().multicast().onBackpressureBuffer();
	Flux<ConsumerRecord<String, String>> flux;

	@Value("${kafka.servers:localhost:9092}")
	private String servers;
	@Value("${kafka.group:checkout}")
	private String group;

	private final Set<String> topics;
	private KafkaConsumer<String, String> consumer;
	private boolean running;
	private boolean hasToUpdate;
	private final TaskExecutor executor;
	
	public MessageConsumerService(@Autowired TaskExecutor executor) {
		this.executor = executor;
		this.running = false;
		this.topics = new HashSet<>();
	}
	
	@PostConstruct
	private void init() throws UnknownHostException {
		Properties config = new Properties();
		config.put("client.id", InetAddress.getLocalHost().getHostName() + "-consumer");
		config.put("bootstrap.servers", servers);
		config.put("group.id", group);
		config.put("key.deserializer", StringDeserializer.class.getName());
		config.put("value.deserializer", StringDeserializer.class.getName());
		logger.info("Connect to " + servers + " as " + config.getProperty("client.id") + "@" + group);
		this.consumer = new KafkaConsumer<>(config);
	}

	private void start() {
		if(!this.running) {
			logger.info("Start consumer thread");
			this.running = true;
			this.executor.execute(() -> {
				while (running) {
					if(hasToUpdate) {
						logger.info("Update subscription to " + this.topics);
						consumer.subscribe(new ArrayList<>(this.topics));
						hasToUpdate = false;
					}
					try {
						if(!consumer.subscription().isEmpty()) {
							ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
							records.forEach(many::tryEmitNext);
							consumer.commitSync();
						} else {
							Thread.sleep(1000);
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			});

			this.flux = many.asFlux();
		}
	}

	public synchronized Flux<ConsumerRecord<String, String>> register(String topic) {
		this.topics.add(topic);
		this.hasToUpdate = true;
		logger.info("Add subscribe for " + topic);
		this.start();

		return this.flux
				.filter(message -> message.topic().equals(topic));
	}

	@PreDestroy
	private void shutDown() {
		this.running = false;
	}
}
