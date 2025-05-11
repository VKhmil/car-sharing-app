package com.carsharingapp.controller;

import com.carsharingapp.dto.rental.RentalRequestDto;
import com.carsharingapp.dto.rental.RentalResponseDto;
import com.carsharingapp.model.User;
import com.carsharingapp.service.RentalService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Create a rental",
            description = "Create a new rental with car ID and return time")
    public RentalResponseDto createRental(
            @RequestBody @Valid RentalRequestDto requestDto,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return rentalService.createRental(requestDto, user.getId());
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get all rentals", description = "Get all user's rentals (paginated)")
    public List<RentalResponseDto> getAll(
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return rentalService.getAllRentals(user.getId(), pageable);
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get active rentals", description = "Get all user's active rentals")
    public List<RentalResponseDto> getAllActiveRentals(
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return rentalService.getAllActiveRentals(user.getId(), pageable);
    }

    @GetMapping("/inactive")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get inactive rentals", description = "Get all user's inactive rentals")
    public List<RentalResponseDto> getAllInactiveRentals(
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return rentalService.getAllNotActiveRentals(user.getId(), pageable);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Search rentals by user and status",
            description = "Search rentals using userId and isActive flag (default true)")
    public List<RentalResponseDto> searchRentals(
            @RequestParam(name = "user_id") Long userId,
            @RequestParam(name = "is_active", defaultValue = "true") boolean isActive,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        return isActive
                ? rentalService.getAllActiveRentals(userId, pageable)
                : rentalService.getAllNotActiveRentals(userId, pageable);
    }

    @PostMapping("/{id}/return")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Return rental", description = "Set actual return date for a rental")
    public RentalResponseDto returnRental(@PathVariable @Positive Long id) {
        return rentalService.setActualReturnDate(id);
    }
}
