package com.carsharingapp.dto.payment;

import com.carsharingapp.model.Payment;
import java.math.BigDecimal;
import java.net.URL;

public record PaymentResponseDto(
        Long id,
        Payment.PaymentStatus status,
        Payment.PaymentType type,
        URL sessionUrl,
        String sessionId,
        BigDecimal amountToPay
) {
}
