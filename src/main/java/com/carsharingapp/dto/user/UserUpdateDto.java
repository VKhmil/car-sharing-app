package com.carsharingapp.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserUpdateDto {
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
}
