package de.thi.informatik.edi.shop.checkout.connectors;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    @Bean
    public NewTopic orderUpdateTopic() {
        return TopicBuilder.name("order-update")
                .partitions(10)
                .replicas(1)
                .build();
    }

}
