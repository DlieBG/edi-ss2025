package de.thi.informatik.edi.shop.checkout.connectors.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class UpdateOrderDto {
    private UUID orderId;
    private double price;
    private String firstName;
    private String lastName;
    private String street;
    private String zipCode;
    private String city;
}
