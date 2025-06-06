package com.carsharingapp.service.impl;

import com.carsharingapp.dto.rental.RentalRequestDto;
import com.carsharingapp.dto.rental.RentalResponseDto;
import com.carsharingapp.exception.EntityNotFoundException;
import com.carsharingapp.exception.NoAvailableCarsException;
import com.carsharingapp.exception.RentalIsNotActiveException;
import com.carsharingapp.mapper.rental.RentalMapper;
import com.carsharingapp.model.Car;
import com.carsharingapp.model.Rental;
import com.carsharingapp.model.User;
import com.carsharingapp.repository.car.CarRepository;
import com.carsharingapp.repository.rental.RentalRepository;
import com.carsharingapp.repository.user.UserRepository;
import com.carsharingapp.service.NotificationService;
import com.carsharingapp.service.RentalService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {
    private static final org.slf4j.Logger logger = LoggerFactory
            .getLogger(RentalServiceImpl.class);
    private final RentalRepository rentalRepository;
    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final RentalMapper rentalMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public RentalResponseDto createRental(RentalRequestDto requestDto, Long userId) {
        Car car = findById(requestDto.carId());
        if (car.getInventory() < 1) {
            logger.warn("No available cars for rental with ID: {}", requestDto.carId());
            throw new NoAvailableCarsException("There are no free cars left for rent!");
        }
        updateCarInventory(car, -1);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Can't find user by id: " + userId));

        Rental rental = rentalMapper.toModelWithCarAndUser(requestDto, car, user);
        logger.info("Rental created successfully for userId: {} with carId: {}",
                userId, requestDto.carId());
        notificationService.notifyUserAboutCreatedRental(rental);

        return rentalMapper.toDto(rentalRepository.save(rental));
    }

    @Override
    public List<RentalResponseDto> getAllRentals(Long userId, Pageable pageable) {
        return rentalMapper.toDtoList(rentalRepository
                .getAllByUserId(userId, pageable));
    }

    @Override
    public List<RentalResponseDto> getAllActiveRentals(Long userId, Pageable pageable) {
        return rentalMapper.toDtoList(rentalRepository
                .getAllByUserIdAndActualReturnDateIsNull(userId, pageable));
    }

    @Override
    public List<RentalResponseDto> getAllNotActiveRentals(Long userId, Pageable pageable) {
        return rentalMapper.toDtoList(rentalRepository
                .getAllByUserIdAndActualReturnDateIsNotNull(userId, pageable));
    }

    @Override
    @Transactional
    public RentalResponseDto setActualReturnDate(Long rentalId) {
        Rental rental = findRentalById(rentalId);

        if (rental.getActualReturnDate() != null) {
            throw new RentalIsNotActiveException("This rental has been already returned!");
        }
        rental.setActualReturnDate(LocalDateTime.now());
        if (rental.getActualReturnDate().isAfter(rental.getReturnDateTime())) {
            handleLateReturn(rental);
            notificationService.notifyUserAboutOverdueRental(rental);
        }
        Car car = rental.getCar();
        updateCarInventory(rental.getCar(), 1);
        rentalRepository.save(rental);
        carRepository.save(car);
        logger.info("Rental returned successfully for rentalId: {}", rentalId);
        notificationService.notifyUserAboutNoOverdueRentals();
        return rentalMapper.toDto(rental);
    }

    private Car findById(Long carId) {
        return carRepository.findById(carId).orElseThrow(
                () -> new EntityNotFoundException("Can't find car by id: " + carId)
        );
    }

    private Rental findRentalById(Long rentalId) {
        return rentalRepository.findById(rentalId).orElseThrow(
                () -> new EntityNotFoundException("Can't find rental by id: " + rentalId)
        );
    }

    private void handleLateReturn(Rental rental) {
        logger.info("Fine for late return applied.");
    }

    private void updateCarInventory(Car car, int change) {
        car.setInventory(car.getInventory() + change);
        carRepository.save(car);
    }
}
