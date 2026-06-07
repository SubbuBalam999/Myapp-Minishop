package com.subbu.minishop.orderservice.order;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateOrderItemRequest(
        @NotNull @Positive Long productId,
        @NotBlank @Size(max = 255) String productName,
        @NotNull @Positive Integer quantity,
        @NotNull @DecimalMin(value = "0.00") BigDecimal price
) {
}
