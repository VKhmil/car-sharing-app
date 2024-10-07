package com.carsharingapp.dto.car;

import com.carsharingapp.model.Car;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class RequestCarDto {
    private String model;
    private String brand;
    private BigDecimal dailyFee;
    private Car.CarBodyType carBodyType;
    private int inventory;
}
