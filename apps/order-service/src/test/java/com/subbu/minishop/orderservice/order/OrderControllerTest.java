package com.subbu.minishop.orderservice.order;

import com.subbu.minishop.orderservice.health.HealthController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class OrderControllerTest {

    private OrderRepository orderRepository;
    private OrderItemRepository orderItemRepository;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        orderItemRepository = mock(OrderItemRepository.class);
        mockMvc = standaloneSetup(
                new OrderController(new OrderService(orderRepository, orderItemRepository)),
                new HealthController()
        ).build();
    }

    @Test
    void returnsAllOrdersWithItems() throws Exception {
        Order order = order(1L, 1L, "1999.98");
        OrderItem item = item(1L, 1L, 1L, "Laptop", 2, "999.99");
        when(orderRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(order));
        when(orderItemRepository.findByOrderIdInOrderByOrderIdAscIdAsc(List.of(1L)))
                .thenReturn(List.of(item));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("CREATED"))
                .andExpect(jsonPath("$[0].items[0].productName").value("Laptop"));
    }

    @Test
    void returnsAnOrderById() throws Exception {
        Order order = order(2L, 1L, "129.99");
        OrderItem item = item(2L, 2L, 2L, "Keyboard", 1, "129.99");
        when(orderRepository.findById(2L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderIdOrderByIdAsc(2L))
                .thenReturn(List.of(item));

        mockMvc.perform(get("/api/orders/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.items[0].productName").value("Keyboard"));
    }

    @Test
    void returnsOrdersForAUser() throws Exception {
        Order order = order(3L, 7L, "299.99");
        when(orderRepository.findByUserIdOrderByCreatedAtDesc(7L))
                .thenReturn(List.of(order));
        when(orderItemRepository.findByOrderIdInOrderByOrderIdAscIdAsc(List.of(3L)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/orders/user/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(7));
    }

    @Test
    void createsOrderAndCalculatesTotal() throws Exception {
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "id", 10L);
            ReflectionTestUtils.setField(order, "createdAt", Instant.parse("2026-06-07T00:00:00Z"));
            return order;
        });
        when(orderItemRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<OrderItem> items = invocation.getArgument(0);
            ReflectionTestUtils.setField(items.get(0), "id", 20L);
            return items;
        });

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1,
                                  "items": [
                                    {
                                      "productId": 1,
                                      "productName": "Laptop",
                                      "quantity": 2,
                                      "price": 999.99
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.totalAmount").value(1999.98))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.items[0].orderId").value(10));
    }

    @Test
    void rejectsInvalidOrder() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 0,
                                  "items": []
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsNotFoundForUnknownOrder() throws Exception {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/orders/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsInvalidOrderId() throws Exception {
        mockMvc.perform(get("/api/orders/0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsServiceHealth() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    private Order order(Long id, Long userId, String totalAmount) {
        Order order = new Order(userId, new BigDecimal(totalAmount), OrderStatus.CREATED);
        ReflectionTestUtils.setField(order, "id", id);
        ReflectionTestUtils.setField(order, "createdAt", Instant.parse("2026-06-07T00:00:00Z"));
        return order;
    }

    private OrderItem item(
            Long id,
            Long orderId,
            Long productId,
            String productName,
            Integer quantity,
            String price
    ) {
        OrderItem item = new OrderItem(
                orderId,
                productId,
                productName,
                quantity,
                new BigDecimal(price)
        );
        ReflectionTestUtils.setField(item, "id", id);
        return item;
    }
}
