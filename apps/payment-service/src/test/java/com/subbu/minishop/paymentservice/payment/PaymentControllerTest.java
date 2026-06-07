package com.subbu.minishop.paymentservice.payment;

import com.subbu.minishop.paymentservice.health.HealthController;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class PaymentControllerTest {

    private PaymentRepository paymentRepository;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        mockMvc = standaloneSetup(
                new PaymentController(new PaymentService(paymentRepository)),
                new HealthController()
        ).build();
    }

    @Test
    void returnsAllPaymentsNewestFirst() throws Exception {
        when(paymentRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(
                payment(1L, 1L, 1L, "299.99", PaymentStatus.SUCCESS, "CARD")
        ));

        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].orderId").value(1))
                .andExpect(jsonPath("$[0].status").value("SUCCESS"));
    }

    @Test
    void returnsPaymentById() throws Exception {
        when(paymentRepository.findById(2L)).thenReturn(Optional.of(
                payment(2L, 3L, 4L, "129.99", PaymentStatus.SUCCESS, "CARD")
        ));

        mockMvc.perform(get("/api/payments/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(3))
                .andExpect(jsonPath("$.userId").value(4));
    }

    @Test
    void returnsPaymentsForOrder() throws Exception {
        when(paymentRepository.findByOrderIdOrderByCreatedAtDesc(7L))
                .thenReturn(List.of(
                        payment(3L, 7L, 1L, "999.99", PaymentStatus.SUCCESS, "CARD")
                ));

        mockMvc.perform(get("/api/payments/order/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value(7))
                .andExpect(jsonPath("$[0].amount").value(999.99));
    }

    @Test
    void createsSuccessfulPayment() throws Exception {
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            ReflectionTestUtils.setField(payment, "id", 10L);
            ReflectionTestUtils.setField(
                    payment,
                    "createdAt",
                    Instant.parse("2026-06-07T00:00:00Z")
            );
            return payment;
        });

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderId": 1,
                                  "userId": 1,
                                  "amount": 299.99,
                                  "paymentMethod": "card"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.paymentMethod").value("CARD"))
                .andExpect(jsonPath("$.amount").value(299.99));
    }

    @Test
    void rejectsInvalidPayment() throws Exception {
        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderId": 0,
                                  "userId": -1,
                                  "amount": 0,
                                  "paymentMethod": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refundsSuccessfulPayment() throws Exception {
        Payment payment = payment(
                4L,
                8L,
                2L,
                "299.99",
                PaymentStatus.SUCCESS,
                "CARD"
        );
        when(paymentRepository.findByIdForUpdate(4L))
                .thenReturn(Optional.of(payment));

        mockMvc.perform(post("/api/payments/4/refund"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REFUNDED"));
    }

    @Test
    void refundIsIdempotent() throws Exception {
        Payment payment = payment(
                5L,
                8L,
                2L,
                "299.99",
                PaymentStatus.REFUNDED,
                "CARD"
        );
        when(paymentRepository.findByIdForUpdate(5L))
                .thenReturn(Optional.of(payment));

        mockMvc.perform(post("/api/payments/5/refund"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REFUNDED"));
    }

    @Test
    void rejectsRefundForUnsuccessfulPayment() throws Exception {
        Payment payment = payment(
                6L,
                9L,
                2L,
                "299.99",
                PaymentStatus.FAILED,
                "CARD"
        );
        when(paymentRepository.findByIdForUpdate(6L))
                .thenReturn(Optional.of(payment));

        mockMvc.perform(post("/api/payments/6/refund"))
                .andExpect(status().isConflict());
    }

    @Test
    void returnsNotFoundForUnknownPayment() throws Exception {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/payments/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsNotFoundForUnknownRefund() throws Exception {
        when(paymentRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/payments/99/refund"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsInvalidPaymentId() throws Exception {
        mockMvc.perform(get("/api/payments/0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsInvalidOrderId() throws Exception {
        mockMvc.perform(get("/api/payments/order/0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsServiceHealth() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    private Payment payment(
            Long id,
            Long orderId,
            Long userId,
            String amount,
            PaymentStatus status,
            String paymentMethod
    ) {
        Payment payment = new Payment(
                orderId,
                userId,
                new BigDecimal(amount),
                status,
                paymentMethod
        );
        ReflectionTestUtils.setField(payment, "id", id);
        ReflectionTestUtils.setField(
                payment,
                "createdAt",
                Instant.parse("2026-06-07T00:00:00Z")
        );
        return payment;
    }
}

