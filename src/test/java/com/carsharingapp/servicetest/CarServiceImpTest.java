package com.carsharingapp.servicetest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.carsharingapp.dto.car.CarFilterDto;
import com.carsharingapp.dto.car.CarResponseDto;
import com.carsharingapp.dto.car.RequestCarDto;
import com.carsharingapp.exception.EntityNotFoundException;
import com.carsharingapp.mapper.car.CarMapper;
import com.carsharingapp.model.Car;
import com.carsharingapp.repository.car.CarRepository;
import com.carsharingapp.repository.spec.CarSpecificationBuilder;
import com.carsharingapp.service.impl.CarServiceImp;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
public class CarServiceImpTest {

    @InjectMocks
    private CarServiceImp carService;

    @Mock
    private CarRepository carRepository;

    @Mock
    private CarMapper carMapper;

    @Mock
    private CarSpecificationBuilder carSpecificationBuilder;

    @Test
    @DisplayName("Finds car with valid id")
    void findCar_WithValidId_ShouldReturnCar_Ok() {
        Long id = 1L;
        Car car = createTestCar();
        CarResponseDto carResponseDto = createCarResponseDto();

        when(carRepository.findById(anyLong())).thenReturn(Optional.of(car));
        when(carMapper.toDto(car)).thenReturn(carResponseDto);

        CarResponseDto carDtoByServiceId = carService.findById(id);

        assertThat(carDtoByServiceId).isEqualTo(carResponseDto);
        verify(carRepository, times(1)).findById(id);
        verify(carMapper, times(1)).toDto(car);
        verifyNoMoreInteractions(carRepository, carMapper);
    }

    @Test
    @DisplayName("Using wrong id, method must throw an exception")
    void getById_NonExistingId_ShouldThrowException_NotOk() {
        when(carRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> carService.findById(anyLong()));
    }

    @Test
    @DisplayName("Finds all cars")
    void findAllCars_Ok() {
        List<Car> cars = createTestCarList();
        List<CarResponseDto> carResponseDtos = createCarResponseDtoList();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Car> carPage = new PageImpl<>(cars, pageable, cars.size());

        when(carRepository.findAll(pageable)).thenReturn(carPage);
        when(carMapper.toDtoList(cars)).thenReturn(carResponseDtos);

        List<CarResponseDto> result = carService.findAllCars(pageable);
        assertNotNull(result);
        assertEquals(carResponseDtos.size(), result.size());
        verify(carRepository).findAll(pageable);
        verify(carMapper).toDtoList(cars);
    }

    @Test
    @DisplayName("Search cars by parameters")
    public void searchCars_ByParameters_ShouldReturnCars_WithValidParameters_Ok() {
        CarFilterDto searchParams = createCarFilterDto();

        Specification<Car> specification = Mockito.mock(Specification.class);

        List<Car> books = new ArrayList<>();
        books.add(createCarWithParams(1L, "Model X",
                "Brand X",
                Car.CarBodyType.SEDAN,
                2,
                BigDecimal.valueOf(1.0)));
        books.add(createCarWithParams(2L,
                "Model Y",
                "Brand Y",
                Car.CarBodyType.SUV,
                3,
                BigDecimal.valueOf(2.0)));

        List<CarResponseDto> carResponseDtos = createCarResponseDtoList();

        Mockito.when(carSpecificationBuilder.build(searchParams)).thenReturn(specification);
        Mockito.when(carRepository.findAll(specification)).thenReturn(books);
        Mockito.when(carMapper.toDtoList(books)).thenReturn(carResponseDtos);

        List<CarResponseDto> result = carService.search(searchParams);

        assertNotNull(result);
        assertThat(result.get(0).getModel()).isEqualTo("Model X");
        assertEquals(carResponseDtos.size(), result.size());

        verify(carSpecificationBuilder).build(searchParams);
        verify(carRepository).findAll(specification);
        verify(carMapper).toDtoList(books);
    }

    @Test
    @DisplayName("Saves a new car")
    void saveCar_ShouldReturnCarResponseDto_Ok() {
        RequestCarDto requestCarDto = createCarRequestDto();
        Car car = createTestCar();
        CarResponseDto carResponseDto = createCarResponseDto();

        when(carMapper.toModel(requestCarDto)).thenReturn(car);
        when(carRepository.save(car)).thenReturn(car);
        when(carMapper.toDto(car)).thenReturn(carResponseDto);

        CarResponseDto savedCarDto = carService.save(requestCarDto);

        assertThat(savedCarDto).isEqualTo(carResponseDto);
        verify(carMapper).toModel(requestCarDto);
        verify(carRepository).save(car);
        verify(carMapper).toDto(car);
    }

    @Test
    @DisplayName("Updates an existing car successfully")
    void updateCar_WithValidId_ShouldUpdateCar_Ok() {
        Long id = 1L;
        RequestCarDto requestCarDto = createCarRequestDto();
        Car existingCar = createTestCar();

        when(carRepository.findById(id)).thenReturn(Optional.of(existingCar));

        carService.update(id, requestCarDto);

        verify(carRepository).findById(id);
        verify(carMapper).updateFromDto(requestCarDto, existingCar);
        verify(carRepository).save(existingCar);
    }

    @Test
    @DisplayName("Updating non-existing car should throw an exception")
    void updateCar_NonExistingId_ShouldThrowException_NotOk() {
        Long id = 999L;
        RequestCarDto requestCarDto = createCarRequestDto();

        Assertions.assertThrows(EntityNotFoundException.class,
                () -> carService.update(id, requestCarDto));

        verify(carRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deletes a car by id")
    void deleteCar_ById_ShouldCallRepositoryDelete_Ok() {
        Long id = 1L;

        carService.deleteById(id);

        verify(carRepository).deleteById(id);
    }

    private Car createTestCar() {
        Car car = new Car();
        car.setModel("Model");
        car.setBrand("Brand");
        car.setCarBodyType(Car.CarBodyType.SUV);
        car.setInventory(1);
        car.setDailyFee(BigDecimal.valueOf(1.0));
        return car;
    }

    private RequestCarDto createCarRequestDto() {
        return new RequestCarDto()
                .setModel("Model")
                .setBrand("Brand")
                .setCarBodyType(Car.CarBodyType.SUV)
                .setInventory(1)
                .setDailyFee(BigDecimal.valueOf(1.0));
    }

    private CarResponseDto createCarResponseDto() {
        return new CarResponseDto()
                .setModel("Model")
                .setBrand("Brand")
                .setCarBodyType(Car.CarBodyType.SUV)
                .setInventory(1)
                .setDailyFee(BigDecimal.valueOf(1.0));
    }

    private List<Car> createTestCarList() {
        Car car1 = new Car();
        car1.setModel("Model X");
        car1.setBrand("Brand X");
        car1.setCarBodyType(Car.CarBodyType.SUV);
        car1.setInventory(2);
        car1.setDailyFee(BigDecimal.valueOf(1.0));

        Car car2 = new Car();
        car2.setModel("Model Y");
        car2.setBrand("Brand Y");
        car2.setCarBodyType(Car.CarBodyType.SEDAN);
        car2.setInventory(3);
        car2.setDailyFee(BigDecimal.valueOf(2.0));

        return List.of(car1, car2);
    }

    private List<CarResponseDto> createCarResponseDtoList() {
        return List.of(
                new CarResponseDto()
                        .setModel("Model X")
                        .setBrand("Brand X")
                        .setCarBodyType(Car.CarBodyType.SEDAN)
                        .setInventory(1)
                        .setDailyFee(BigDecimal.valueOf(1.0)),
                new CarResponseDto()
                        .setModel("Model Y")
                        .setBrand("Brand Y")
                        .setCarBodyType(Car.CarBodyType.SUV)
                        .setInventory(2)
                        .setDailyFee(BigDecimal.valueOf(2.0))
        );
    }

    private CarFilterDto createCarFilterDto() {
        return new CarFilterDto(
                new String[]{"Model X", "Model Y"},
                new String[]{"Brand X", "Brand Y"},
                new String[]{"Sedan", "SUV"},
                new String[]{"1.0","2.0"},
                new String[]{"2", "3"}
        );
    }

    private Car createCarWithParams(Long id, String model, String brand, Car.CarBodyType carBodyType, Integer inventory, BigDecimal dailyFee) {
        Car car = createTestCar();
        car.setId(id);
        car.setModel(model);
        car.setBrand(brand);
        car.setCarBodyType(carBodyType);
        car.setInventory(inventory);
        car.setDailyFee(dailyFee);
        return car;
    }
}
