package com.carsharingapp.controller;

import com.carsharingapp.dto.car.CarFilterDto;
import com.carsharingapp.dto.car.CarResponseDto;
import com.carsharingapp.dto.car.RequestCarDto;
import com.carsharingapp.service.CarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Cars management", description = "Endpoint for managing cars")
@RestController
@RequestMapping(value = "/cars")
@RequiredArgsConstructor
public class CarController {
    private final CarService carService;

    @GetMapping
    @Operation(summary = "Find all cars",
            description = "Find all cars, uses pagination, and sorting")
    public List<CarResponseDto> getAllCars(Pageable pageable) {
        return carService.findAllCars(pageable);
    }

    @GetMapping("/search")
    @Operation(summary = "Searching a cars by parameter",
            description = "Searching a cars by parameter dynamically")
    public List<CarResponseDto> search(CarFilterDto carFilterDto) {
        return carService.search(carFilterDto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find car by id",
            description = "Find car in DB, and return DTO of current car")
    public CarResponseDto getCarById(@PathVariable Long id) {
        return carService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Saves a list of cars in the database",
            description = "Accepts a list of car data transfer objects (DTOs) "
                    + "and saves each car to the database. Returns a list of saved car response "
                    + "DTOs with their assigned IDs and other details.")
    public CarResponseDto saveCar(@RequestBody @Valid RequestCarDto requestCarDto) {
        return carService.save(requestCarDto);
    }

    @PostMapping("/save-multiple")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Saves list of cars in DB")
    public List<CarResponseDto> saveCars(@RequestBody @Valid List<RequestCarDto> cars) {
        return carService.saveAll(cars);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(description = "update car",
            summary = "Update car and return response DTO of updated car "
                    + "Checks if a field you entered is valid")
    public CarResponseDto updateCar(@PathVariable Long id,
                                    @RequestBody @Valid RequestCarDto requestCarDto) {
        return carService.update(id, requestCarDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete car by id",
            description = "Delete car by soft delete")
    public void deleteById(@PathVariable Long id) {
        carService.deleteById(id);
    }
}
