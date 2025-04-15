package de.thi.informatik.edi.shop.warehouse.connectors.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class UpdateOrderItemDto {
    private UUID articleId;
    private String name;
    private double price;
    private int count;
}
