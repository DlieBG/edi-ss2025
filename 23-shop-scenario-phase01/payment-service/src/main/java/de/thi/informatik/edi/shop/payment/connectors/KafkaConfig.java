package de.thi.informatik.edi.shop.payment.connectors;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    @Bean
    public NewTopic paymentUpdateTopic() {
        return TopicBuilder.name("payment-update")
                .partitions(10)
                .replicas(1)
                .build();
    }
}
