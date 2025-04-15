package de.thi.informatik.edi.shop.payment.connectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.thi.informatik.edi.shop.payment.connectors.dto.UpdatePaymentDto;
import de.thi.informatik.edi.shop.payment.model.Payment;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {
    @Autowired
    private KafkaTemplate<String, String> template;

    @SneakyThrows
    public void updatePayment(Payment payment) {
        UpdatePaymentDto dto = new UpdatePaymentDto(
                payment.getOrderRef()
        );

        ObjectMapper objectMapper = new ObjectMapper();
        String dtoJson = objectMapper.writeValueAsString(dto);

        this.template.send("payment-update", dtoJson);
    }
}
