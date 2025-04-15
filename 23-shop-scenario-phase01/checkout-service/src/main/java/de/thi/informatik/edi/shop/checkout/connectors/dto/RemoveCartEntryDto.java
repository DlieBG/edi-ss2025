package de.thi.informatik.edi.shop.checkout.connectors.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class RemoveCartEntryDto {
    private UUID cartId;
    private UUID articleId;
}
