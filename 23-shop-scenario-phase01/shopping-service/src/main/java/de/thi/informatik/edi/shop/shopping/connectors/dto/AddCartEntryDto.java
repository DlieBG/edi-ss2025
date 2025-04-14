package de.thi.informatik.edi.shop.shopping.connectors.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class AddCartEntryDto {
    private UUID cartId;
    private UUID articleId;
    private int count;
    private double price;
    private String name;
}
