package com.carsharingapp.service.impl;

import com.carsharingapp.model.Car;
import com.carsharingapp.model.Payment;
import com.carsharingapp.model.Rental;
import com.carsharingapp.service.NotificationService;
import com.carsharingapp.service.telegram.CarSharingBot;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {
    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final String RENTAL_MESSAGE_PREFIX =
            "Hello, %s! 🙌";
    private static final String RENTAL_INFO_PREFIX =
            "You have successfully rented the following car:";
    private static final String PAYMENT_SUCCESS_PREFIX =
            "✅ Payment Successful!";
    private static final String PAYMENT_CANCEL_PREFIX =
            "❌ Payment Failed!";
    private static final String OVERDUE_RENTAL_PREFIX =
            "🚨 Reminder: You should have returned the car:";
    private static final String OVERDUE_RENTALS_MESSAGE =
            "⏰ Important: You have overdue rentals. Please return them as soon as possible:";
    private static final String NO_OVERDUE_MESSAGE =
            "🌞 Great news! You have no overdue rentals today!";

    private final CarSharingBot carSharingBot;

    @Override
    public void notifyUserAboutCreatedRental(Rental rental) {
        String carInfo = formatCarInfo(rental.getCar());
        String dateInfo = formatDate(rental.getRentalDateTime(), rental.getReturnDateTime());
        String name = String.format(RENTAL_MESSAGE_PREFIX, rental.getUser().getFirstName());
        String message = String.join(System.lineSeparator(),
                name, RENTAL_INFO_PREFIX,
                carInfo,
                dateInfo);
        carSharingBot.sendMessage(message);
    }

    @Override
    public void notifyUserAboutOverdueRental(Rental rental) {
        Car car = rental.getCar();
        String message = String.format(
                "%s %s (Expected Return: 🕛 %s), but you returned it on 🕑 %s.",
                OVERDUE_RENTAL_PREFIX,
                formatCarInfo(car),
                formatDate(rental.getReturnDateTime()),
                formatDate(rental.getActualReturnDate())
        );
        carSharingBot.sendMessage(message);
    }

    @Override
    public void notifyUserAboutSuccessfulPayment(Payment payment, Car car) {
        String message = String.format(
                "%s You have successfully paid for the rental of: %s. Total amount: $%s.",
                PAYMENT_SUCCESS_PREFIX,
                formatCarInfo(car),
                payment.getAmountToPay()
        );
        carSharingBot.sendMessage(message);
    }

    @Override
    public void notifyUserAboutCanceledPayment(Payment payment, Car car) {
        String message = String.format(
                "%s Unfortunately, your payment for the rental of: "
                        + "%s has failed. Please try again. Amount: $%s.",
                PAYMENT_CANCEL_PREFIX,
                formatCarInfo(car),
                payment.getAmountToPay()
        );
        carSharingBot.sendMessage(message);
    }

    @Override
    public void sendScheduledOverdueRentalsNotification(Set<Rental> overdueRentals) {
        String message = String.format("%s %s", OVERDUE_RENTALS_MESSAGE,
                formatOverdueRentals(overdueRentals));
        carSharingBot.sendMessage(message);
    }

    @Override
    public void notifyUserAboutNoOverdueRentals() {
        carSharingBot.sendMessage(NO_OVERDUE_MESSAGE);
    }

    public String formatOverdueRentals(Set<Rental> overdueRentals) {
        StringBuilder sb = new StringBuilder();
        for (Rental rental : overdueRentals) {
            sb.append("Car: ")
                    .append(formatCarInfo(rental.getCar()))
                    .append(System.lineSeparator());
        }
        return sb.toString();
    }

    private String formatCarInfo(Car car) {
        return String.format(
                "🚗 Model: %s, Brand: %s, Type: %s",
                car.getModel(),
                car.getBrand(),
                car.getCarBodyType().name()
        );
    }

    private String formatDate(LocalDateTime dateTime) {
        return dateTime.format(formatter);
    }

    private String formatDate(LocalDateTime rentalDateTime, LocalDateTime returnDateTime) {
        String formattedRentalDateTime = rentalDateTime.format(formatter);
        String formattedReturnDateTime = returnDateTime.format(formatter);
        return String.format(
                "Your rental starts on: ⌛ %s, and you should return the car by: ⏳ %s. "
                        + "Remember, a fine may apply if you don't return on time! 💸",
                formattedRentalDateTime,
                formattedReturnDateTime
        );
    }
}
