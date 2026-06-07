package com.subbu.minishop.inventoryservice.inventory;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public List<Inventory> getInventory() {
        return inventoryService.getInventory();
    }

    @GetMapping("/{productId}")
    public Inventory getInventoryForProduct(@PathVariable Long productId) {
        return inventoryService.getInventoryForProduct(
                requirePositive(productId, "Product ID")
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Inventory createInventory(
            @Valid @RequestBody CreateInventoryRequest request
    ) {
        return inventoryService.createInventory(request);
    }

    @PostMapping("/reserve")
    public Inventory reserve(
            @Valid @RequestBody InventoryAdjustmentRequest request
    ) {
        return inventoryService.reserve(request);
    }

    @PostMapping("/release")
    public Inventory release(
            @Valid @RequestBody InventoryAdjustmentRequest request
    ) {
        return inventoryService.release(request);
    }

    private Long requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    fieldName + " must be greater than zero"
            );
        }
        return value;
    }
}

