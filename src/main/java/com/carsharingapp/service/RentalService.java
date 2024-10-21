package com.carsharingapp.service;

import com.carsharingapp.dto.rental.RentalRequestDto;
import com.carsharingapp.dto.rental.RentalResponseDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface RentalService {
    RentalResponseDto createRental(RentalRequestDto requestDto, Long userId);

    List<RentalResponseDto> getAllRentals(Long userId, Pageable pageable);

    List<RentalResponseDto> getAllActiveRentals(Long userId, Pageable pageable);

    List<RentalResponseDto> getAllNotActiveRentals(Long userId, Pageable pageable);

    RentalResponseDto setActualReturnDate(Long rentalId);
}
