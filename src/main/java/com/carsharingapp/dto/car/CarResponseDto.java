package com.carsharingapp.dto.car;

import com.carsharingapp.model.Car;
import java.math.BigDecimal;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CarResponseDto {
    private Long id;
    private String model;
    private String brand;
    private BigDecimal dailyFee;
    private Car.CarBodyType carBodyType;
    private int inventory;
}
