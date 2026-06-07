package com.subbu.minishop.orderservice.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record CreateOrderRequest(
        @NotNull @Positive Long userId,
        @NotEmpty List<@Valid CreateOrderItemRequest> items
) {
}
