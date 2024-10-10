package com.carsharingapp.service;

import com.carsharingapp.dto.car.CarFilterDto;
import com.carsharingapp.dto.car.CarResponseDto;
import com.carsharingapp.dto.car.RequestCarDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface CarService {

    CarResponseDto findById(Long id);

    List<CarResponseDto> findAllCars(Pageable pageable);

    List<CarResponseDto> search(CarFilterDto carFilterDto);

    CarResponseDto save(RequestCarDto requestCarDto);

    List<CarResponseDto> saveAll(List<RequestCarDto> requestCarDtos);

    CarResponseDto update(Long id, RequestCarDto requestCarDto);

    void deleteById(Long id);
}
