package com.subbu.minishop.inventoryservice.inventory;

import com.subbu.minishop.inventoryservice.health.HealthController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class InventoryControllerTest {

    private InventoryRepository inventoryRepository;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        inventoryRepository = mock(InventoryRepository.class);
        mockMvc = standaloneSetup(
                new InventoryController(new InventoryService(inventoryRepository)),
                new HealthController()
        ).build();
    }

    @Test
    void returnsAllInventoryOrderedByProduct() throws Exception {
        when(inventoryRepository.findAllByOrderByProductIdAsc()).thenReturn(List.of(
                inventory(1L, 1L, 10, 2)
        ));

        mockMvc.perform(get("/api/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(1))
                .andExpect(jsonPath("$[0].availableQuantity").value(10))
                .andExpect(jsonPath("$[0].reservedQuantity").value(2));
    }

    @Test
    void returnsInventoryForAProduct() throws Exception {
        when(inventoryRepository.findByProductId(2L)).thenReturn(Optional.of(
                inventory(2L, 2L, 25, 0)
        ));

        mockMvc.perform(get("/api/inventory/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(2))
                .andExpect(jsonPath("$.availableQuantity").value(25));
    }

    @Test
    void createsInventory() throws Exception {
        when(inventoryRepository.existsByProductId(1L)).thenReturn(false);
        when(inventoryRepository.save(any(Inventory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/api/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": 1,
                                  "availableQuantity": 10,
                                  "reservedQuantity": 0
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.availableQuantity").value(10))
                .andExpect(jsonPath("$.reservedQuantity").value(0));
    }

    @Test
    void rejectsDuplicateProductInventory() throws Exception {
        when(inventoryRepository.existsByProductId(1L)).thenReturn(true);

        mockMvc.perform(post("/api/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": 1,
                                  "availableQuantity": 10,
                                  "reservedQuantity": 0
                                }
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void reservesAvailableInventory() throws Exception {
        when(inventoryRepository.findByProductIdForUpdate(1L)).thenReturn(Optional.of(
                inventory(1L, 1L, 10, 1)
        ));

        mockMvc.perform(post("/api/inventory/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": 1,
                                  "quantity": 2
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableQuantity").value(8))
                .andExpect(jsonPath("$.reservedQuantity").value(3));
    }

    @Test
    void rejectsReservationBeyondAvailableInventory() throws Exception {
        when(inventoryRepository.findByProductIdForUpdate(1L)).thenReturn(Optional.of(
                inventory(1L, 1L, 1, 0)
        ));

        mockMvc.perform(post("/api/inventory/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": 1,
                                  "quantity": 2
                                }
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void releasesReservedInventory() throws Exception {
        when(inventoryRepository.findByProductIdForUpdate(1L)).thenReturn(Optional.of(
                inventory(1L, 1L, 8, 3)
        ));

        mockMvc.perform(post("/api/inventory/release")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": 1,
                                  "quantity": 2
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableQuantity").value(10))
                .andExpect(jsonPath("$.reservedQuantity").value(1));
    }

    @Test
    void rejectsReleaseBeyondReservedInventory() throws Exception {
        when(inventoryRepository.findByProductIdForUpdate(1L)).thenReturn(Optional.of(
                inventory(1L, 1L, 8, 1)
        ));

        mockMvc.perform(post("/api/inventory/release")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": 1,
                                  "quantity": 2
                                }
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsInvalidInventoryRequest() throws Exception {
        mockMvc.perform(post("/api/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": 0,
                                  "availableQuantity": -1,
                                  "reservedQuantity": -1
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsInvalidAdjustmentRequest() throws Exception {
        mockMvc.perform(post("/api/inventory/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": 1,
                                  "quantity": 0
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsNotFoundForUnknownProduct() throws Exception {
        when(inventoryRepository.findByProductId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/inventory/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsInvalidProductId() throws Exception {
        mockMvc.perform(get("/api/inventory/0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsServiceHealth() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    private Inventory inventory(
            Long id,
            Long productId,
            Integer availableQuantity,
            Integer reservedQuantity
    ) {
        Inventory inventory = new Inventory(
                productId,
                availableQuantity,
                reservedQuantity
        );
        ReflectionTestUtils.setField(inventory, "id", id);
        ReflectionTestUtils.setField(
                inventory,
                "updatedAt",
                Instant.parse("2026-06-07T00:00:00Z")
        );
        return inventory;
    }
}

