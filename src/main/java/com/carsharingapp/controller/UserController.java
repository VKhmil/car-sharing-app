package com.carsharingapp.controller;

import com.carsharingapp.dto.role.RoleRequestDto;
import com.carsharingapp.dto.user.UserResponseDto;
import com.carsharingapp.dto.user.UserUpdateDto;
import com.carsharingapp.mapper.user.UserMapper;
import com.carsharingapp.model.User;
import com.carsharingapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User management", description = "Endpoints for managing users")
@RestController
@RequestMapping("/users")
@Validated
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    @Operation(summary = "Update user's role",
            description = "Manager can update user's role (param - new role)")
    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/{id}/role")
    public UserResponseDto updateUserRole(@PathVariable @Positive Long id,
                                          @RequestBody RoleRequestDto roleDto) {
        return userService.updateUserRole(id, roleDto.role());
    }

    @Operation(summary = "Get user's info",
            description = "Get user's firstname, lastname and email")
    @GetMapping("/me")
    public UserResponseDto getUserInfo(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return userMapper.toUserResponseDto(user);
    }

    @Operation(summary = "Update user's info",
            description = "Update user's firstName and lastName")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'MANAGER')")
    @PatchMapping("/update")
    public UserResponseDto updateUserProfile(
            @RequestBody @Valid UserUpdateDto requestDto,
            Authentication authentication) {
        return userService.updateUserInfo(authentication, requestDto);
    }
}
