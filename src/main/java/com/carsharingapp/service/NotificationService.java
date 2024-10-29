package com.carsharingapp.service;

import com.carsharingapp.model.Car;
import com.carsharingapp.model.Payment;
import com.carsharingapp.model.Rental;
import java.util.Set;

public interface NotificationService {
    void notifyUserAboutCreatedRental(Rental rental);

    void notifyUserAboutOverdueRental(Rental rental);

    void notifyUserAboutSuccessfulPayment(Payment payment, Car car);

    void notifyUserAboutCanceledPayment(Payment payment, Car car);

    void sendScheduledOverdueRentalsNotification(Set<Rental> overdueRentals);

    void notifyUserAboutNoOverdueRentals();

}
