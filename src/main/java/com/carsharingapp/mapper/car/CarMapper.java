package com.carsharingapp.mapper.car;

import com.carsharingapp.config.MapperConfig;
import com.carsharingapp.dto.car.CarResponseDto;
import com.carsharingapp.dto.car.RequestCarDto;
import com.carsharingapp.model.Car;
import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@Mapper(config = MapperConfig.class)
public interface CarMapper {

    CarResponseDto toDto(Car car);

    Car toModel(RequestCarDto requestCarDto);

    List<CarResponseDto> toDtoList(List<Car> book);

    List<Car> toModelList(List<RequestCarDto> requestCarDtos);

    void updateFromDto(RequestCarDto requestCarDto, @MappingTarget Car car);

    default Page<CarResponseDto> toDtoPage(Page<Car> cars) {
        List<CarResponseDto> dtos = cars.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, cars.getPageable(), cars.getTotalElements());
    }
}
