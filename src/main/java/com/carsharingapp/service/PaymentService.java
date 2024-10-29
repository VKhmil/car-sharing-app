package com.carsharingapp.service;

import com.carsharingapp.dto.payment.PaymentRequestDto;
import com.carsharingapp.dto.payment.PaymentResponseDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface PaymentService {
    List<PaymentResponseDto> getPayments(Long userId, Pageable pageable);

    List<PaymentResponseDto> getPaymentsByStatus(Long userId, String status, Pageable pageable);

    PaymentResponseDto createPaymentSession(PaymentRequestDto request);

    PaymentResponseDto getSuccessfulPayment(String sessionId);

    PaymentResponseDto getCancelledPayment(String sessionId);
}
