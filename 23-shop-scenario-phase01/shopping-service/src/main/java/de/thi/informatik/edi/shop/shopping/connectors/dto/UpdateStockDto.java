package de.thi.informatik.edi.shop.shopping.connectors.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class UpdateStockDto {
    private UUID articleId;
    private int stock;
}
