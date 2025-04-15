package de.thi.informatik.edi.shop.warehouse.connectors.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class PickItemDto {
    private UUID articleId;
    private int count;
}
