package com.carsharingapp.repository;

import com.carsharingapp.dto.car.CarFilterDto;
import org.springframework.data.jpa.domain.Specification;

public interface SpecificationBuilder<T> {

    Specification<T> build(CarFilterDto carFilterDto);
}
