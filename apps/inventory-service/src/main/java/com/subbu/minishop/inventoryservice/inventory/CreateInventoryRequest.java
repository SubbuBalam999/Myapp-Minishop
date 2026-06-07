package com.subbu.minishop.inventoryservice.inventory;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateInventoryRequest(
        @NotNull @Positive Long productId,
        @NotNull @PositiveOrZero Integer availableQuantity,
        @NotNull @PositiveOrZero Integer reservedQuantity
) {
}

