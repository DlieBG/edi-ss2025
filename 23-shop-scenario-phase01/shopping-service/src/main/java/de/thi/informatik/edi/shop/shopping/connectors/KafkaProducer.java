package de.thi.informatik.edi.shop.shopping.connectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.thi.informatik.edi.shop.shopping.connectors.dto.AddCartEntryDto;
import de.thi.informatik.edi.shop.shopping.connectors.dto.RemoveCartEntryDto;
import de.thi.informatik.edi.shop.shopping.model.CartEntry;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class KafkaProducer {
    @Autowired
    private KafkaTemplate<String, String> template;

    @SneakyThrows
    public void addCartEntry(UUID cartId, CartEntry entry) {
        AddCartEntryDto dto = new AddCartEntryDto(
                cartId,
                entry.getId(),
                entry.getCount(),
                entry.getPrice(),
                entry.getName()
        );

        ObjectMapper objectMapper = new ObjectMapper();
        String dtoJson = objectMapper.writeValueAsString(dto);

        this.template.send("shopping-cart-add", dtoJson);
    }

    @SneakyThrows
    public void removeCartEntry(UUID cartId, CartEntry entry) {
        RemoveCartEntryDto dto = new RemoveCartEntryDto(
                cartId,
                entry.getId()
        );

        ObjectMapper objectMapper = new ObjectMapper();
        String dtoJson = objectMapper.writeValueAsString(dto);

        this.template.send("shopping-cart-remove", dtoJson);
    }
}
