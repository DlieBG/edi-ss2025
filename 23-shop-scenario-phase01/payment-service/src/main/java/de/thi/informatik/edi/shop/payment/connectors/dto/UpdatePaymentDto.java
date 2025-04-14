package de.thi.informatik.edi.shop.payment.connectors.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class UpdatePaymentDto {
    private UUID orderId;
}
