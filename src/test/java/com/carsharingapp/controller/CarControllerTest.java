package com.carsharingapp.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carsharingapp.dto.car.CarFilterDto;
import com.carsharingapp.dto.car.CarResponseDto;
import com.carsharingapp.dto.car.RequestCarDto;
import com.carsharingapp.model.Car;
import com.carsharingapp.service.telegram.BotInitializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "classpath:database/test/cars/insert-list-of-cars.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/test/cars/delete-cars.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class CarControllerTest {

    private static MockMvc mockMvc;

    @Mock
    private BotInitializer botInitializer;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(@Autowired WebApplicationContext applicationContext) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @DisplayName("Create Car")
    void createCar_ValidRequest_ShouldSaveCar_Ok() throws Exception {
        RequestCarDto createCarDto = requestCarDto();

        MvcResult result = mockMvc
                .perform(post("/cars")
                        .content(objectMapper
                                .writeValueAsString(createCarDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        CarResponseDto actualDto = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                CarResponseDto.class);

        Assertions.assertNotNull(actualDto);
        Assertions.assertEquals(createCarDto.getModel(),
                actualDto.getModel());
        Assertions.assertEquals(createCarDto.getBrand(),
                actualDto.getBrand());
    }

    @Test
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    @DisplayName("Get all cars")
    void getAllCars_ShouldReturnCarsList_Ok() throws Exception {
        MvcResult result = mockMvc.perform(get("/cars")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        CarResponseDto[] actual = objectMapper.readValue(
                result.getResponse()
                        .getContentAsString(),
                CarResponseDto[].class);
        Assertions.assertTrue(actual.length > 0);
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @DisplayName("Update Car")
    void updateCar_ValidId_ShouldReturnUpdatedCar_Ok() throws Exception {
        long carId = 1L;
        RequestCarDto updateCarDto = updateCarRequestDto();
        CarResponseDto expectedDto = createExpectedCarDto(carId, updateCarDto);

        String jsonRequest = objectMapper.writeValueAsString(updateCarDto);

        MvcResult result = mockMvc.perform(put("/cars/"
                        + carId)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Assertions.assertEquals(200, result
                .getResponse()
                .getStatus());

        CarResponseDto actualDto = objectMapper.readValue(result
                        .getResponse()
                        .getContentAsString(),
                CarResponseDto.class);
        Assertions.assertEquals(expectedDto, actualDto);
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @DisplayName("Delete Car by ID")
    void deleteCarById_ValidId_ShouldReturnNoContent_Ok() throws Exception {
        long carId = 1L;

        mockMvc.perform(delete("/cars/" + carId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    @DisplayName("Search cars by filters (GET request)")
    void searchCars_ValidFilters_ShouldReturnFilteredCars_Ok() throws Exception {
        CarFilterDto carFilterDto = carFilterDto();

        MvcResult result = mockMvc.perform(get("/cars/search")
                        .param("models", carFilterDto.models()[0])
                        .param("brands", carFilterDto.brands()[0])
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        CarResponseDto[] actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                CarResponseDto[].class);

        Assertions.assertNotNull(actual);
        Assertions.assertTrue(actual.length > 0, "Expected at least one car matching the filter.");
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @DisplayName("Save multiple cars")
    void saveMultipleCars_ShouldReturnSavedCarsList_Ok() throws Exception {
        List<RequestCarDto> carListDto = createCarListDto();

        MvcResult result = mockMvc.perform(post("/cars/save-multiple")
                        .content(objectMapper.writeValueAsString(carListDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        CarResponseDto[] actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                CarResponseDto[].class);
        Assertions.assertEquals(carListDto.size(), actual.length);
    }

    private RequestCarDto requestCarDto() {
        return new RequestCarDto()
                .setModel("Test model1")
                .setBrand("Test brand1")
                .setInventory(1)
                .setCarBodyType(Car.CarBodyType.SUV)
                .setDailyFee(new BigDecimal("1.00"));
    }

    private CarResponseDto createExpectedCarDto(long carId, RequestCarDto requestDto) {
        return new CarResponseDto()
                .setId(carId)
                .setModel(requestDto.getModel())
                .setBrand(requestDto.getBrand())
                .setInventory(requestDto.getInventory())
                .setCarBodyType(requestDto.getCarBodyType())
                .setDailyFee(requestDto.getDailyFee());
    }

    private RequestCarDto updateCarRequestDto() {
        RequestCarDto dto = new RequestCarDto()
                .setModel("Updated model")
                .setBrand("Updated brand")
                .setInventory(3)
                .setCarBodyType(Car.CarBodyType.SEDAN)
                .setDailyFee(new BigDecimal("2.00"));

        System.out.println("Generated DTO: " + dto);
        return dto;
    }

    private CarFilterDto carFilterDto() {
        return new CarFilterDto(
                new String[]{"Test brand2"},
                new String[]{"Test model2"},
                null,
                null,
                null);
    }

    private List<RequestCarDto> createCarListDto() {
        List<RequestCarDto> carListDto = new ArrayList<>();
        carListDto.add(new RequestCarDto()
                .setModel("Test model1")
                .setBrand("Test brand1")
                .setCarBodyType(Car.CarBodyType.SUV)
                        .setInventory(1)
                .setDailyFee(new BigDecimal("1.00")));
        carListDto.add(new RequestCarDto()
                .setModel("Test model1")
                .setBrand("Test brand2")
                .setInventory(2)
                .setCarBodyType(Car.CarBodyType.SEDAN)
                .setDailyFee(new BigDecimal("2.00")));
        return carListDto;
    }
}
