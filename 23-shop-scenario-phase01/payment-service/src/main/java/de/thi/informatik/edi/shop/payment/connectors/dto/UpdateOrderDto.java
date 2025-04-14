package de.thi.informatik.edi.shop.payment.connectors.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class UpdateOrderDto {
    private UUID orderId;
    private double price;
    private String firstName;
    private String lastName;
    private String street;
    private String zipCode;
    private String city;
}
