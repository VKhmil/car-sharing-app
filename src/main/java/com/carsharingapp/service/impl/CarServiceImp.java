package com.carsharingapp.service.impl;

import com.carsharingapp.dto.car.CarFilterDto;
import com.carsharingapp.dto.car.CarResponseDto;
import com.carsharingapp.dto.car.RequestCarDto;
import com.carsharingapp.exception.EntityNotFoundException;
import com.carsharingapp.mapper.CarMapper;
import com.carsharingapp.model.Car;
import com.carsharingapp.repository.CarRepository;
import com.carsharingapp.repository.spec.CarSpecificationBuilder;
import com.carsharingapp.service.CarService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CarServiceImp implements CarService {
    private final CarRepository carRepository;
    private final CarMapper carMapper;
    private final CarSpecificationBuilder carSpecificationBuilder;

    @Override
    public CarResponseDto findById(Long id) {
        Car car = carRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can't find car by ID: " + id)
        );
        return carMapper.toDto(car);
    }

    @Override
    public List<CarResponseDto> findAllCars(Pageable pageable) {
        return carRepository.findAll(pageable).stream()
                .map(carMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CarResponseDto> search(CarFilterDto carFilterDto) {
        Specification<Car> build = carSpecificationBuilder
                .build(carFilterDto);
        return carMapper.toDtoList(carRepository.findAll(build));
    }

    @Override
    public CarResponseDto save(RequestCarDto requestCarDto) {
        Car car = carMapper.toModel(requestCarDto);
        return carMapper.toDto(carRepository.save(car));
    }

    @Override
    @Transactional
    public CarResponseDto update(Long id, RequestCarDto requestCarDto) {
        Car car = carRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can't find car by ID: " + id)
        );
        carMapper.updateFromDto(requestCarDto, car);
        return carMapper.toDto(carRepository.save(car));
    }

    @Override
    public void deleteById(Long id) {
        carRepository.deleteById(id);
    }
}
