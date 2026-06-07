package com.subbu.minishop.cartservice.cart;

import com.subbu.minishop.cartservice.health.HealthController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class CartControllerTest {

    private CartItemRepository cartItemRepository;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        cartItemRepository = mock(CartItemRepository.class);
        mockMvc = standaloneSetup(
                new CartController(new CartService(cartItemRepository)),
                new HealthController()
        ).build();
    }

    @Test
    void returnsCartItemsForAUser() throws Exception {
        when(cartItemRepository.findByUserIdOrderByCreatedAtAsc(1L)).thenReturn(List.of(
                new CartItem(1L, 2L, 3)
        ));

        mockMvc.perform(get("/api/cart/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[0].productId").value(2))
                .andExpect(jsonPath("$[0].quantity").value(3));
    }

    @Test
    void createsAValidCartItem() throws Exception {
        when(cartItemRepository.save(any(CartItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1,
                                  "productId": 2,
                                  "quantity": 3
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.productId").value(2))
                .andExpect(jsonPath("$.quantity").value(3));
    }

    @Test
    void rejectsAnInvalidCartItem() throws Exception {
        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 0,
                                  "productId": -1,
                                  "quantity": 0
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deletesAnExistingCartItem() throws Exception {
        when(cartItemRepository.existsById(7L)).thenReturn(true);

        mockMvc.perform(delete("/api/cart/items/7"))
                .andExpect(status().isNoContent());

        verify(cartItemRepository).deleteById(7L);
    }

    @Test
    void returnsNotFoundForUnknownCartItem() throws Exception {
        when(cartItemRepository.existsById(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/cart/items/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void clearsAUsersCart() throws Exception {
        mockMvc.perform(delete("/api/cart/1"))
                .andExpect(status().isNoContent());

        verify(cartItemRepository).deleteByUserId(1L);
    }

    @Test
    void rejectsInvalidUserId() throws Exception {
        mockMvc.perform(get("/api/cart/0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsServiceHealth() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
