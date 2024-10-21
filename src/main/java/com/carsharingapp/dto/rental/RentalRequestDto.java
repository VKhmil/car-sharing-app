package com.carsharingapp.dto.rental;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

public record RentalRequestDto(
        @Positive
        Long carId,
        @NotNull
        LocalDateTime returnDateTime
) {
}
