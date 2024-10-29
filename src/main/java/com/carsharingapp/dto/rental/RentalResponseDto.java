package com.carsharingapp.dto.rental;

import java.time.LocalDateTime;

public record RentalResponseDto(
        Long id,
        Long carId,
        String carBrand,
        String carModel,
        LocalDateTime rentalDateTime,
        LocalDateTime returnDateTime,
        LocalDateTime actualReturnDateTime
) {
}
