package de.thi.informatik.edi.shop.checkout.connectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.thi.informatik.edi.shop.checkout.connectors.dto.UpdateOrderDto;
import de.thi.informatik.edi.shop.checkout.connectors.dto.UpdateOrderItemDto;
import de.thi.informatik.edi.shop.checkout.connectors.dto.UpdateOrderStatusDto;
import de.thi.informatik.edi.shop.checkout.model.ShoppingOrder;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {
    @Autowired
    private KafkaTemplate<String, String> template;

    @SneakyThrows
    public void updateOrder(ShoppingOrder order) {
        UpdateOrderDto dto = new UpdateOrderDto(
                order.getId(),
                order.getPrice(),
                order.getFirstName(),
                order.getLastName(),
                order.getStreet(),
                order.getZipCode(),
                order.getCity(),
                UpdateOrderStatusDto.fromShoppingOrderStatus(order.getStatus()),
                order.getItems().stream().map(
                        item -> new UpdateOrderItemDto(
                                item.getArticle(),
                                item.getName(),
                                item.getPrice(),
                                item.getCount()
                        )
                ).toList()
        );

        ObjectMapper objectMapper = new ObjectMapper();
        String dtoJson = objectMapper.writeValueAsString(dto);

        this.template.send("order-update", dtoJson);
    }
}
