package com.carsharingapp.dto.car;

import com.carsharingapp.model.Car;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RequestCarDto {
    @NotBlank
    private String model;
    @NotBlank
    private String brand;
    @Positive
    private BigDecimal dailyFee;
    @NotNull
    private Car.CarBodyType carBodyType;
    @Positive
    private int inventory;
}
