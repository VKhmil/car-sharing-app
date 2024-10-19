package com.carsharingapp.service;

import com.carsharingapp.dto.user.UserRegistrationRequestDto;
import com.carsharingapp.dto.user.UserResponseDto;
import com.carsharingapp.dto.user.UserUpdateDto;
import com.carsharingapp.exception.RegistrationException;
import org.springframework.security.core.Authentication;

public interface UserService {
    UserResponseDto register(UserRegistrationRequestDto registrationRequestDto)
            throws RegistrationException;

    UserResponseDto updateUserRole(Long id, String role);

    UserResponseDto updateUserInfo(Authentication authentication,
                                   UserUpdateDto userUpdateDto);
}
