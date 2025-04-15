package de.thi.informatik.edi.shop.shopping.connectors.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class RemoveCartEntryDto {
    private UUID cartId;
    private UUID articleId;
}
