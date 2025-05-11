package com.carsharingapp.servicetest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.carsharingapp.model.Car;
import com.carsharingapp.model.Payment;
import com.carsharingapp.model.Rental;
import com.carsharingapp.model.User;
import com.carsharingapp.service.impl.NotificationServiceImpl;
import com.carsharingapp.service.telegram.CarSharingBot;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {
    @Mock
    private CarSharingBot carSharingBot;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void shouldNotifyUserAboutCreatedRental() {
        Rental rental = createTestRental(createTestCar());
        notificationService.notifyUserAboutCreatedRental(rental);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        String expectedMessage = String.format(
                "Hello, %s! 🙌\nYou have successfully rented the following car:\n🚗 "
                        + "Model: %s, Brand: %s, Type: %s\nYour rental starts on: ⌛ %s, "
                        + "and you should return the car by: ⏳ %s. "
                        + "Remember, a fine may apply if you don't return on time! 💸",
                rental.getUser().getFirstName(),
                rental.getCar().getModel(),
                rental.getCar().getBrand(),
                rental.getCar().getCarBodyType(),
                rental.getRentalDateTime().format(formatter),
                rental.getReturnDateTime().format(formatter)
        );

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(carSharingBot, times(1)).sendMessage(messageCaptor.capture());

        String actualMessage = messageCaptor.getValue();

        assertThat(actualMessage).isEqualToIgnoringWhitespace(expectedMessage);
    }

    @Test
    void shouldNotifyUserAboutOverdueRental() {
        Rental rental = createTestRental(createTestCar());
        rental.setActualReturnDate(LocalDateTime.now().plusDays(3));

        notificationService.notifyUserAboutOverdueRental(rental);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        String expectedMessage = String.format(
                "🚨 Reminder: You should have returned the car: 🚗 "
                        + "Model: %s, Brand: %s, Type: %s (Expected Return: 🕛 %s), "
                        + "but you returned it on 🕑 %s.",
                rental.getCar().getModel(),
                rental.getCar().getBrand(),
                rental.getCar().getCarBodyType(),
                rental.getReturnDateTime().format(formatter),
                rental.getActualReturnDate().format(formatter)
        );

        verify(carSharingBot, times(1)).sendMessage(expectedMessage);
    }

    @Test
    void shouldNotifyUserAboutSuccessfulPayment() {
        Payment payment = createTestPayment();
        Car car = createTestCar();

        notificationService.notifyUserAboutSuccessfulPayment(payment, car);

        String expectedMessage = String.format(
                Locale.US,
                "✅ Payment Successful! You have successfully paid for the rental of: "
                        + "🚗 Model: %s, Brand: %s, Type: %s. Total amount: $%.1f.",
                car.getModel(),
                car.getBrand(),
                car.getCarBodyType(),
                payment.getAmountToPay()
        );

        verify(carSharingBot, times(1)).sendMessage(expectedMessage);
    }

    @Test
    void shouldNotifyUserAboutCanceledPayment() {
        Payment payment = createTestPayment();
        Car car = createTestCar();

        notificationService.notifyUserAboutCanceledPayment(payment, car);

        String expectedMessage = String.format(
                Locale.US,
                "❌ Payment Failed! Unfortunately, "
                        + "your payment for the rental of: 🚗 "
                        + "Model: %s, Brand: %s, Type: %s has failed. "
                        + "Please try again. Amount: $%.1f.",
                car.getModel(),
                car.getBrand(),
                car.getCarBodyType(),
                payment.getAmountToPay()
        );

        verify(carSharingBot, times(1)).sendMessage(expectedMessage);
    }

    @Test
    void shouldNotifyUserAboutNoOverdueRentals() {
        notificationService.notifyUserAboutNoOverdueRentals();

        verify(carSharingBot, times(1)).sendMessage("🌞 "
                + "Great news! You have no overdue rentals today!");
    }

    private Car createTestCar() {
        Car car = new Car();
        car.setModel("Model S");
        car.setBrand("Tesla");
        car.setCarBodyType(Car.CarBodyType.SEDAN);
        car.setDailyFee(BigDecimal.valueOf(1.0));
        car.setInventory(1);
        return car;
    }

    private Rental createTestRental(Car car) {
        final Rental rental = new Rental();
        User user = new User();
        user.setEmail("john.doe@mail.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        rental.setCar(car);
        rental.setUser(user);
        rental.setRentalDateTime(LocalDateTime.now());
        rental.setReturnDateTime(LocalDateTime.now().plusDays(2));
        return rental;
    }

    private Payment createTestPayment() {
        Payment payment = new Payment();
        payment.setAmountToPay(BigDecimal.valueOf(100.00));
        return payment;
    }
}
