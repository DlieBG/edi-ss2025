package de.thi.informatik.edi.shop.checkout.connectors.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class UpdateOrderItemDto {
    private UUID articleId;
    private String name;
    private double price;
    private int count;
}
