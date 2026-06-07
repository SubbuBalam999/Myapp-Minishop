package com.subbu.minishop.inventoryservice.inventory;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record InventoryAdjustmentRequest(
        @NotNull @Positive Long productId,
        @NotNull @Positive Integer quantity
) {
}

