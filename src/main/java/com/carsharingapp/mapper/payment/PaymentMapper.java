package com.carsharingapp.mapper.payment;

import com.carsharingapp.dto.payment.PaymentRequestDto;
import com.carsharingapp.dto.payment.PaymentResponseDto;
import com.carsharingapp.mapper.rental.RentalMapper;
import com.carsharingapp.model.Payment;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = RentalMapper.class)
public interface PaymentMapper {

    PaymentResponseDto toDto(Payment payment);

    Payment toEntity(PaymentRequestDto paymentDto);

    List<PaymentResponseDto> toDtoList(List<Payment> payments);
}
