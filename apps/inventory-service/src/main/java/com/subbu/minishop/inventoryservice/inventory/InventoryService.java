package com.subbu.minishop.inventoryservice.inventory;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public List<Inventory> getInventory() {
        return inventoryRepository.findAllByOrderByProductIdAsc();
    }

    public Inventory getInventoryForProduct(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> notFound(productId));
    }

    @Transactional
    public Inventory createInventory(CreateInventoryRequest request) {
        if (inventoryRepository.existsByProductId(request.productId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Inventory already exists for product " + request.productId()
            );
        }

        return inventoryRepository.save(new Inventory(
                request.productId(),
                request.availableQuantity(),
                request.reservedQuantity()
        ));
    }

    @Transactional
    public Inventory reserve(InventoryAdjustmentRequest request) {
        Inventory inventory = findForUpdate(request.productId());
        if (inventory.getAvailableQuantity() < request.quantity()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Insufficient available inventory for product " + request.productId()
            );
        }

        try {
            inventory.reserve(request.quantity());
        } catch (ArithmeticException exception) {
            throw quantityLimitExceeded(request.productId());
        }
        return inventory;
    }

    @Transactional
    public Inventory release(InventoryAdjustmentRequest request) {
        Inventory inventory = findForUpdate(request.productId());
        if (inventory.getReservedQuantity() < request.quantity()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Insufficient reserved inventory for product " + request.productId()
            );
        }

        try {
            inventory.release(request.quantity());
        } catch (ArithmeticException exception) {
            throw quantityLimitExceeded(request.productId());
        }
        return inventory;
    }

    private Inventory findForUpdate(Long productId) {
        return inventoryRepository.findByProductIdForUpdate(productId)
                .orElseThrow(() -> notFound(productId));
    }

    private ResponseStatusException notFound(Long productId) {
        return new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Inventory not found for product " + productId
        );
    }

    private ResponseStatusException quantityLimitExceeded(Long productId) {
        return new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Inventory quantity limit exceeded for product " + productId
        );
    }
}

