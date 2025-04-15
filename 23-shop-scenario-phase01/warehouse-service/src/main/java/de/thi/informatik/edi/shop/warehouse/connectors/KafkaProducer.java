package de.thi.informatik.edi.shop.warehouse.connectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.thi.informatik.edi.shop.warehouse.connectors.dto.PickItemDto;
import de.thi.informatik.edi.shop.warehouse.connectors.dto.UpdateShippingDto;
import de.thi.informatik.edi.shop.warehouse.model.Shipping;
import de.thi.informatik.edi.shop.warehouse.model.ShippingItem;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KafkaProducer {
    @Autowired
    private KafkaTemplate<String, String> template;

    @SneakyThrows
    public void updateShipping(Shipping shipping) {
        UpdateShippingDto dto = new UpdateShippingDto(
                shipping.getOrderRef()
        );

        ObjectMapper objectMapper = new ObjectMapper();
        String dtoJson = objectMapper.writeValueAsString(dto);

        this.template.send("shipping-update", dtoJson);
    }

    @SneakyThrows
    public void pickItems(List<ShippingItem> items) {
        List<PickItemDto> dto = items.stream().map(
                item -> new PickItemDto(
                        item.getArticle(),
                        item.getCount()
                )
        ).toList();

        ObjectMapper objectMapper = new ObjectMapper();
        String dtoJson = objectMapper.writeValueAsString(dto);

        this.template.send("items-pick", dtoJson);
    }
}
