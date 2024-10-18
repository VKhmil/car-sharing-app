package com.carsharingapp.dto.car;

public record CarFilterDto(
        String [] brands,
        String [] models,
        String [] carBodyTypes,
        String [] dailyFees,
        String [] inventories
) {}
