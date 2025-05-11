package com.carsharingapp.repository;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.carsharingapp.model.Car;
import com.carsharingapp.model.Rental;
import com.carsharingapp.model.User;
import com.carsharingapp.repository.rental.RentalRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;
import org.testng.annotations.Test;

@DataJpaTest
@AutoConfigureTestDatabase(replace =
        AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = {
        "classpath:database.test/users/insert-into-roles.sql",
        "classpath:database.test/users/insert-into-users_roles.sql",
        "classpath:database.test/users/insert-into-users.sql",
        "classpath:database.test/cars/insert-into-cars.sql",
        "classpath:database.test/rentals/insert-into-rentals"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {
        "classpath:database.test/users/delete-into-roles.sql",
        "classpath:database.test/users/delete-into-users_roles.sql",
        "classpath:database.test/users/delete-into-users.sql",
        "classpath:database.test/cars/delete-into-cars.sql",
        "classpath:database.test/rentals/delete-into-rentals"
}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class RentalRepoTest {

    @Autowired
    private RentalRepository rentalRepo;

    @Autowired
    private EntityManager entityManager;

    @Test
    public void getAllByUserIdAndActualReturnDateTimeIsNull_ShouldReturnCorrectRentals() {
        Pageable pageable = PageRequest.of(0, 10);
        Long userId = 1L;
        List<Rental> result = rentalRepo
                .getAllByUserIdAndActualReturnDateIsNull(userId, pageable);

        assertThat(result).hasSize(2);
        assertThat(result).extracting("actualReturnDate").containsOnlyNulls();
    }

    @Test
    public void getAllByUserIdAndActualReturnDateTimeIsNotNull_ShouldReturnCorrectRentals() {
        Pageable pageable = PageRequest.of(0, 10);
        Long userId = 1L;
        List<Rental> result = rentalRepo
                .getAllByUserIdAndActualReturnDateIsNotNull(userId, pageable);

        assertThat(result).hasSize(2);
        assertThat(result).extracting("actualReturnDate").doesNotContainNull();
    }

    private List<Rental> createRentalsWithNullActualReturnDate(User user, Car car) {
        Rental rental1 = new Rental();
        rental1.setUser(user);
        rental1.setCar(car);
        rental1.setRentalDateTime(LocalDateTime.of(2024,
                11,
                10,
                10,
                0,
                0,
                0));
        rental1.setActualReturnDate(null);

        Rental rental2 = new Rental();
        rental2.setUser(user);
        rental2.setCar(car);
        rental2.setRentalDateTime(LocalDateTime.of(
                2024,
                11,
                20,
                8, 0,
                0,
                0));
        rental2.setActualReturnDate(null);

        return List.of(rental1, rental2);
    }

    private List<Rental> createRentalsWithNotNullActualReturnDate(
            User user, Car car) {
        Rental rental1 = new Rental();
        rental1.setUser(user);
        rental1.setCar(car);
        rental1.setRentalDateTime(LocalDateTime.of(2024,
                11,
                12,
                15,
                30,
                0,
                0));
        rental1.setActualReturnDate(LocalDateTime.now().minusDays(1));

        Rental rental2 = new Rental();
        rental2.setUser(user);
        rental2.setCar(car);
        rental2.setRentalDateTime(LocalDateTime.of(
                2024,
                11,
                22,
                14, 0,
                0,
                0));
        rental2.setActualReturnDate(LocalDateTime.now().minusDays(1));

        return List.of(rental1, rental2);
    }

    private User createUser() {
        User user = new User();
        user.setEmail("testuser1@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setDeleted(false);
        return user;
    }

    private Car createCar() {
        Car car = new Car();
        car.setBrand("Test model");
        car.setModel("Test brand");
        car.setInventory(1);
        car.setCarBodyType(Car.CarBodyType.SUV);
        car.setDailyFee(new BigDecimal(1));
        return car;
    }
}
