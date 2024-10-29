package com.carsharingapp.controller;

import com.carsharingapp.dto.rental.RentalRequestDto;
import com.carsharingapp.dto.rental.RentalResponseDto;
import com.carsharingapp.model.User;
import com.carsharingapp.service.RentalService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rentals")
public class RentalController {
    private final RentalService rentalService;

    @Operation(summary = "Create new rental",
            description = "Creating a new rental. Params: carId, returnDateTime")
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping
    public RentalResponseDto createRental(
            @RequestBody RentalRequestDto requestDto,
            @AuthenticationPrincipal User user) {
        return rentalService.createRental(requestDto, user.getId());
    }

    @Operation(summary = "Get all rentals",
            description = "Get all user rentals (Pageable default: page = 0, size = 10)")
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping
    public List<RentalResponseDto> getAll(
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            @AuthenticationPrincipal User user) {
        return rentalService.getAllRentals(user.getId(), pageable);
    }

    @Operation(summary = "Get active or inactive rentals",
            description = "Get active or inactive user rentals based on the path. "
                    + "(Pageable default: page = 0, size = 10)")
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/active")
    public List<RentalResponseDto> getAllActiveRentals(
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            @AuthenticationPrincipal User user) {
        return rentalService.getAllActiveRentals(user.getId(), pageable);
    }

    @GetMapping("/inactive")
    public List<RentalResponseDto> getAllInactiveRentals(
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            @AuthenticationPrincipal User user) {
        return rentalService.getAllNotActiveRentals(user.getId(), pageable);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/search")
    @Operation(summary = "Search rentals",
            description = "Search rentals using userId and activities, "
                   + "default is_active = true (Pageable default: page = 0, size = 10)")
    public List<RentalResponseDto> searchRentals(
            @RequestParam(name = "user_id") Long userId,
            @RequestParam(name = "is_active", defaultValue = "true") boolean isActive,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        return isActive
                ? rentalService.getAllActiveRentals(userId, pageable)
                : rentalService.getAllNotActiveRentals(userId, pageable);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/{id}/return")
    @Operation(summary = "Return rental by id",
            description = "Returning rental by setting actual return date")
    public RentalResponseDto returnRental(@PathVariable @Positive Long id) {
        return rentalService.setActualReturnDate(id);
    }
}
