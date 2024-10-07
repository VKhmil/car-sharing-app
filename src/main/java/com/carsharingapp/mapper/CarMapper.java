package com.carsharingapp.mapper;

import com.carsharingapp.config.MapperConfig;
import com.carsharingapp.dto.car.CarResponseDto;
import com.carsharingapp.dto.car.RequestCarDto;
import com.carsharingapp.model.Car;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface CarMapper {

    CarResponseDto toDto(Car car);

    Car toModel(RequestCarDto requestCarDto);

    List<CarResponseDto> toDtoList(List<Car> book);

    void updateFromDto(RequestCarDto requestCarDto, @MappingTarget Car car);
}
