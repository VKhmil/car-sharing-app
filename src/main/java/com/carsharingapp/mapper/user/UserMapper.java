package com.carsharingapp.mapper.user;

import com.carsharingapp.config.MapperConfig;
import com.carsharingapp.dto.user.UserRegistrationRequestDto;
import com.carsharingapp.dto.user.UserResponseDto;
import com.carsharingapp.dto.user.UserUpdateDto;
import com.carsharingapp.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    User toModel(UserRegistrationRequestDto requestDto);

    UserResponseDto toUserResponseDto(User user);

    void updateUserFromDto(UserUpdateDto dto, @MappingTarget User user);
}
