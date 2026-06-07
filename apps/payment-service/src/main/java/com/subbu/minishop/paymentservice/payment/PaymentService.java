package com.subbu.minishop.paymentservice.payment;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public List<Payment> getPayments() {
        return paymentRepository.findAllByOrderByCreatedAtDesc();
    }

    public Payment getPayment(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> notFound(id));
    }

    public List<Payment> getPaymentsForOrder(Long orderId) {
        return paymentRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
    }

    @Transactional
    public Payment createPayment(CreatePaymentRequest request) {
        return paymentRepository.save(new Payment(
                request.orderId(),
                request.userId(),
                request.amount().setScale(2, RoundingMode.HALF_UP),
                PaymentStatus.SUCCESS,
                request.paymentMethod().strip().toUpperCase(Locale.ROOT)
        ));
    }

    @Transactional
    public Payment refund(Long id) {
        Payment payment = paymentRepository.findByIdForUpdate(id)
                .orElseThrow(() -> notFound(id));

        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            return payment;
        }
        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only successful payments can be refunded"
            );
        }

        payment.refund();
        return payment;
    }

    private ResponseStatusException notFound(Long id) {
        return new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Payment not found: " + id
        );
    }
}

