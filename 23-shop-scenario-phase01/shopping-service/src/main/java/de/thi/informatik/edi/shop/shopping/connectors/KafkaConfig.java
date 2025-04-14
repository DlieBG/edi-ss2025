package de.thi.informatik.edi.shop.shopping.connectors;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    @Bean
    public NewTopic shoppingCartAddTopic() {
        return TopicBuilder.name("shopping-cart-add")
                .partitions(10)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic shoppingCartRemoveTopic() {
        return TopicBuilder.name("shopping-cart-remove")
                .partitions(10)
                .replicas(1)
                .build();
    }
}
