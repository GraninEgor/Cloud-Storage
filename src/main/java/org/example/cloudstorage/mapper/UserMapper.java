package org.example.cloudstorage.mapper;

import org.example.cloudstorage.database.entity.User;
import org.example.cloudstorage.dto.UserDto;

public class UserMapper {

    public static User toEntity(UserDto userDto){
        return User.builder()
                .username(userDto.getUsername())
                .password(userDto.getPassword())
                .build();
    }

    public static UserDto toDto(User user){
        return UserDto.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .build();
    }
}
