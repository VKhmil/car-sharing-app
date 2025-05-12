package com.carsharingapp.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carsharingapp.dto.rental.RentalRequestDto;
import com.carsharingapp.dto.rental.RentalResponseDto;
import com.carsharingapp.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SqlGroup({
        @Sql(scripts = {
                "classpath:database/test/clear-data/clear-data.sql"
        }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(scripts = {
                "classpath:database/test/cars/insert-list-of-cars.sql",
                "classpath:database/test/roles/insert-into-roles.sql",
                "classpath:database/test/users/insert-into-users.sql",
                "classpath:database/test/users/insert-into-users_roles.sql",
                "classpath:database/test/rentals/insert-into-rentals.sql",
                "classpath:database/test/payments/insert-into-payments.sql"
        }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(scripts = "classpath:database/test/clear-data/clear-data.sql",
                executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
public class RentalControllerTest {

    private static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @BeforeAll
    static void beforeAll(@Autowired WebApplicationContext applicationContext) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithUserDetails("testuser2@example.com")
    @DisplayName("Create rental")
    void createRental_ValidRequest_ShouldReturnCreatedRental() throws Exception {
        Logger logger = LoggerFactory.getLogger(getClass());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        RentalRequestDto requestDto = new RentalRequestDto(
                2L,
                LocalDateTime.parse("2025-11-12 08:00:00", formatter)
        );

        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        logger.info("Serialized JSON request: {}", jsonRequest);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/rentals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest));
        result.andExpect(status().isOk());
    }

    @Test
    @WithUserDetails("testuser2@example.com")
    @DisplayName("Get all rentals")
    void getAllRentals_ShouldReturnListOfRentals() throws Exception {
        mockMvc.perform(get("/rentals")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id")
                        .value(1L))
                .andExpect(jsonPath("$[0].carBrand")
                        .value("Test brand1"))
                .andExpect(jsonPath("$[0].carModel")
                        .value("Test model1"))
                .andExpect(jsonPath("$[0].rentalDateTime")
                        .value("2024-11-05T15:30:00"))
                .andExpect(jsonPath("$[0].returnDateTime")
                        .value("2024-11-12T15:30:00"))
                .andExpect(jsonPath("$[0].actualReturnDateTime").isEmpty());
    }

    @Test
    @WithUserDetails("testuser2@example.com")
    @DisplayName("Get active rentals")
    void getActiveRentals_ShouldReturnActiveRentals() throws Exception {
        ResultActions result = mockMvc.perform(get("/rentals/active")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$[0].id")
                        .value(3L))
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$[0].carId")
                        .value(1L));
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @DisplayName("Search rentals")
    void searchRentals_ShouldReturnRentalsBasedOnFilters() throws Exception {
        RentalResponseDto rental1 = new RentalResponseDto(
                1L,
                1L,
                "Car Brand",
                "Car Model",
                LocalDateTime.parse("2024-12-01T10:00:00"),
                LocalDateTime.parse("2024-12-01T12:00:00"),
                null);

        mockMvc.perform(get("/rentals/search")
                        .param("user_id", "1")
                        .param("is_active", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails("testuser1@example.com")
    @DisplayName("Return rental")
    void returnRental_ShouldReturnUpdatedRental() throws Exception {
        RentalResponseDto rental = new RentalResponseDto(
                4L,
                1L,
                "Car Brand",
                "Car Model",
                LocalDateTime.parse("2024-12-01T10:00:00"),
                LocalDateTime.parse("2024-12-01T12:00:00"),
                LocalDateTime.parse("2024-12-01T12:00:00"));

        mockMvc.perform(post("/rentals/4/return")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
