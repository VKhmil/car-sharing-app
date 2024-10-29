package com.carsharingapp.service;

import com.carsharingapp.model.Car;
import com.carsharingapp.model.Payment;
import com.carsharingapp.model.Rental;
import java.util.Set;

public interface ServiceNotification {
    void sendMessageAboutCreatedRental(Rental rental);

    void sendMessageAboutOverdueRental(Rental rental);

    void sendMessageAboutSuccessPayment(Payment payment, Car car);

    void sendMessageAboutCanceledPayment(Payment payment, Car car);

    void sendScheduledMessageAboutOverdueRentals(Set<Rental> overdueRentals);

    void sendNoRentalsOverdueMessage();
}
