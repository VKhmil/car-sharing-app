package com.carsharingapp.service.impl;

import com.carsharingapp.dto.user.UserRegistrationRequestDto;
import com.carsharingapp.dto.user.UserResponseDto;
import com.carsharingapp.dto.user.UserUpdateDto;
import com.carsharingapp.exception.EntityNotFoundException;
import com.carsharingapp.exception.RegistrationException;
import com.carsharingapp.mapper.user.UserMapper;
import com.carsharingapp.model.Role;
import com.carsharingapp.model.User;
import com.carsharingapp.repository.role.RoleRepository;
import com.carsharingapp.repository.user.UserRepository;
import com.carsharingapp.service.UserService;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;

    @Override
    public UserResponseDto register(UserRegistrationRequestDto request)
            throws RegistrationException {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RegistrationException("Email already registered!");
        }
        User user = userMapper.toModel(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        Role userRole = roleRepository.findRoleByName(Role.RoleName.CUSTOMER)
                .orElseThrow(
                        () -> new RegistrationException("Can't find role by name"));
        Set<Role> defaultUserRoleSet = new HashSet<>();
        defaultUserRoleSet.add(userRole);
        user.setRoles(defaultUserRoleSet);
        return userMapper.toUserResponseDto(userRepository.save(user));
    }

    @Override
    public UserResponseDto updateUserRole(Long id, String roleDto) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can't find user by id: " + id)
        );
        Role roleName = roleRepository.findRoleByName(Role.RoleName.valueOf(roleDto)).orElseThrow(
                () -> new EntityNotFoundException("Can't find role name " + roleDto)
        );

        user.getRoles().clear();
        user.getRoles().add(roleName);
        return userMapper.toUserResponseDto(userRepository.save(user));
    }

    @Override
    public UserResponseDto updateUserInfo(
            Authentication authentication,
            UserUpdateDto userUpdateDto) {
        final User user = (User) authentication.getPrincipal();
        userMapper.updateUserFromDto(userUpdateDto, user);
        return userMapper.toUserResponseDto(userRepository.save(user));
    }
}
