package com.subbu.minishop.productservice.product;

import com.subbu.minishop.productservice.health.HealthController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
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

class ProductControllerTest {

    private ProductRepository productRepository;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        mockMvc = standaloneSetup(
                new ProductController(new ProductService(productRepository)),
                new HealthController()
        ).build();
    }

    @Test
    void returnsAllProducts() throws Exception {
        when(productRepository.findAll()).thenReturn(List.of(
                new Product("Mechanical Keyboard", "Compact keyboard", new BigDecimal("89.99"), 12)
        ));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Mechanical Keyboard"))
                .andExpect(jsonPath("$[0].price").value(89.99))
                .andExpect(jsonPath("$[0].stockQuantity").value(12));
    }

    @Test
    void createsAValidProduct() throws Exception {
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "USB-C Hub",
                                  "description": "Seven-port USB-C hub",
                                  "price": 49.99,
                                  "stockQuantity": 25
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("USB-C Hub"))
                .andExpect(jsonPath("$.price").value(49.99))
                .andExpect(jsonPath("$.stockQuantity").value(25));
    }

    @Test
    void rejectsAnInvalidProduct() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "description": "Invalid product",
                                  "price": -1.00,
                                  "stockQuantity": -2
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsAProductById() throws Exception {
        when(productRepository.findById(1L)).thenReturn(Optional.of(
                new Product("Webcam", "1080p webcam", new BigDecimal("59.99"), 8)
        ));

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Webcam"));
    }

    @Test
    void returnsNotFoundForUnknownProduct() throws Exception {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsServiceHealth() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
