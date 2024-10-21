package com.carsharingapp.mapper.rental;

import com.carsharingapp.config.MapperConfig;
import com.carsharingapp.dto.rental.RentalRequestDto;
import com.carsharingapp.dto.rental.RentalResponseDto;
import com.carsharingapp.model.Car;
import com.carsharingapp.model.Rental;
import com.carsharingapp.model.User;
import java.time.LocalDateTime;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface RentalMapper {
    @Mapping(source = "car.id", target = "carId")
    @Mapping(source = "car.brand", target = "carBrand")
    @Mapping(source = "car.model", target = "carModel")
    RentalResponseDto toDto(Rental rental);

    @Mapping(source = "car.id", target = "carId")
    @Mapping(source = "car.brand", target = "carBrand")
    @Mapping(source = "car.model", target = "carModel")
    List<RentalResponseDto> toDtoList(List<Rental> rentals);

    @Mapping(target = "car", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "rentalDateTime", ignore = true)
    Rental toModel(RentalRequestDto requestDto);

    default Rental toModelWithCarAndUser(RentalRequestDto requestDto, Car car, User user) {
        Rental rental = toModel(requestDto);
        rental.setCar(car);
        rental.setUser(user);
        rental.setRentalDateTime(LocalDateTime.now());
        return rental;
    }
}
