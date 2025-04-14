package de.thi.informatik.edi.shop.checkout.connectors.dto;

import de.thi.informatik.edi.shop.checkout.model.ShoppingOrderStatus;

public enum UpdateOrderStatusDto {
    CREATED, PLACED, PAYED, SHIPPED, DELIVERED;

    public static UpdateOrderStatusDto fromShoppingOrderStatus(ShoppingOrderStatus shoppingOrderStatus) {
        return switch (shoppingOrderStatus) {
            case CREATED -> CREATED;
            case PLACED -> PLACED;
            case PAYED -> PAYED;
            case SHIPPED -> SHIPPED;
            case DELIVERED -> DELIVERED;
        };
    }
}
