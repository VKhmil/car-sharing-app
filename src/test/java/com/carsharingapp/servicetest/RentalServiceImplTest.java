package com.carsharingapp.servicetest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.carsharingapp.dto.rental.RentalRequestDto;
import com.carsharingapp.dto.rental.RentalResponseDto;
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
import com.carsharingapp.service.impl.RentalServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class RentalServiceImplTest {

    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private CarRepository carRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RentalMapper rentalMapper;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private RentalServiceImpl rentalService;

    @Test
    @DisplayName("Return all rentals to user")
    void getAllRentals_shouldReturnAllRentalsForUser() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        List<Rental> rentals = List.of(createRental());
        List<RentalResponseDto> rentalDtos = List.of(createRentalResponseDto());
        when(rentalRepository.getAllByUserId(userId, pageable)).thenReturn(rentals);
        when(rentalMapper.toDtoList(rentals)).thenReturn(rentalDtos);

        List<RentalResponseDto> result = rentalService.getAllRentals(userId, pageable);

        assertEquals(rentalDtos, result);
    }

    @Test
    @DisplayName("Get all active rentals")
    void getAllActiveRentals_shouldReturnOnlyActiveRentalsForUser() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        List<Rental> activeRentals = List.of(createRental());
        List<RentalResponseDto> activeRentalDtos = List.of(createRentalResponseDto());

        when(rentalRepository
                .getAllByUserIdAndActualReturnDateIsNull(userId, pageable))
                .thenReturn(activeRentals);
        when(rentalMapper.toDtoList(activeRentals)).thenReturn(activeRentalDtos);

        List<RentalResponseDto> result = rentalService
                .getAllActiveRentals(userId, pageable);

        assertEquals(activeRentalDtos, result);
    }

    @Test
    @DisplayName("Get all non active rentals")
    void getAllNotActiveRentals_shouldReturnOnlyNotActiveRentalsForUser() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Rental rental = createRental();
        rental.setActualReturnDate(LocalDateTime.now());
        List<Rental> notActiveRentals = List.of(rental);
        List<RentalResponseDto> notActiveRentalDtos = List.of(createRentalResponseDto());

        when(rentalRepository
                .getAllByUserIdAndActualReturnDateIsNotNull(userId, pageable))
                .thenReturn(notActiveRentals);
        when(rentalMapper.toDtoList(notActiveRentals)).thenReturn(notActiveRentalDtos);

        List<RentalResponseDto> result = rentalService
                .getAllNotActiveRentals(userId, pageable);

        assertEquals(notActiveRentalDtos, result);
    }

    @Test
    @DisplayName("Create rental when car is available")
    void createRental_whenCarInventoryIsAvailable_shouldCreateRental() {
        Car car = createCar();
        Rental rental = createRental();
        User user = createUser();
        rental.setUser(user);
        RentalRequestDto requestDto = createRentalRequestDto();
        RentalResponseDto responseDto = createRentalResponseDto();

        when(carRepository.findById(car.getId())).thenReturn(Optional.of(car));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(rentalMapper.toModelWithCarAndUser(requestDto, car, user)).thenReturn(rental);
        when(rentalRepository.save(rental)).thenReturn(rental);
        when(rentalMapper.toDto(rental)).thenReturn(responseDto);

        RentalResponseDto actual = rentalService.createRental(requestDto, user.getId());

        assertEquals(responseDto, actual);
        verify(notificationService).notifyUserAboutCreatedRental(rental);
    }

    @Test
    @DisplayName("Throw exception when non active cars left")
    void createRental_whenNoAvailableCars_shouldThrowNoAvailableCarsException() {
        RentalRequestDto requestDto = createRentalRequestDto();
        Car car = createCar();
        car.setInventory(0);

        when(carRepository.findById(requestDto.carId())).thenReturn(Optional.of(car));

        assertThrows(NoAvailableCarsException.class, () -> rentalService
                .createRental(requestDto,
                        1L));
    }

    @Test
    @DisplayName("Set active return date")
    void setActualReturnDate_whenRentalIsAlreadyReturned_shouldThrowRentalIsNotActiveException() {
        Rental rental = createRental();
        rental.setActualReturnDate(LocalDateTime.now());

        when(rentalRepository.findById(rental.getId())).thenReturn(Optional.of(rental));

        assertThrows(RentalIsNotActiveException.class,
                () -> rentalService
                        .setActualReturnDate(rental.getId()));
    }

    private Car createCar() {
        Car car = new Car();
        car.setId(1L);
        car.setBrand("Toyota");
        car.setModel("Corolla");
        car.setCarBodyType(Car.CarBodyType.SEDAN);
        car.setInventory(3);
        car.setDailyFee(BigDecimal.valueOf(50));
        car.setDeleted(false);
        return car;
    }

    private Rental createRental() {
        Rental rental = new Rental();
        rental.setId(1L);
        rental.setCar(createCar());
        rental.setRentalDateTime(LocalDateTime.now());
        rental.setReturnDateTime(LocalDateTime.now().plusDays(3));
        return rental;
    }

    private User createUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        return user;
    }

    private RentalRequestDto createRentalRequestDto() {
        return new RentalRequestDto(1L, LocalDateTime.now().plusDays(3));
    }

    private RentalResponseDto createRentalResponseDto() {
        return new RentalResponseDto(1L, 1L, "Toyota", "Corolla", LocalDateTime.now(),
                LocalDateTime.now().plusDays(3), null);
    }
}
