package com.carsharingapp.dto.payment;

import com.carsharingapp.model.Payment;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PaymentRequestDto(@Positive
                                Long rentalId,
                                @NotNull
                                Payment.PaymentType paymentType
) {
}
