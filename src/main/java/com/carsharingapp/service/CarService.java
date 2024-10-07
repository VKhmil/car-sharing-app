package com.carsharingapp.service;

import com.carsharingapp.dto.car.CarResponseDto;
import com.carsharingapp.dto.car.RequestCarDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface CarService {

    CarResponseDto findById(Long id);

    List<CarResponseDto> findAllCars(Pageable pageable);

    CarResponseDto save(RequestCarDto requestCarDto);

    CarResponseDto update(Long id, RequestCarDto requestCarDto);

    void deleteById(Long id);
}
