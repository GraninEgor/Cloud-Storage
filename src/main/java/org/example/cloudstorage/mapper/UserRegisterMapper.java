package org.example.cloudstorage.mapper;

import org.example.cloudstorage.database.entity.User;
import org.example.cloudstorage.dto.UserRegisterDto;

public class UserRegisterMapper {

    public static User toEntity(UserRegisterDto userRegisterDto){
        return User.builder()
                .username(userRegisterDto.getUsername())
                .password(userRegisterDto.getPassword())
                .build();
    }

    public static UserRegisterDto toDto(User user){
        return UserRegisterDto.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .build();
    }
}
