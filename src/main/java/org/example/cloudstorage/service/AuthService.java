package org.example.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.database.entity.User;
import org.example.cloudstorage.database.repository.UserRepository;
import org.example.cloudstorage.dto.UserDto;
import org.example.cloudstorage.exception.UserValidationException;
import org.example.cloudstorage.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${app.min-user-length}")
    private Integer minUsernameLength;

    private final UserRepository userRepository;

    public UserDto save(UserDto userDto) {
        if(userDto.getUsername().length() > minUsernameLength){
            throw new UserValidationException("Длина username должна быть больше " + minUsernameLength);
        }
        User user;
        try {
            user = userRepository.save(UserMapper.toEntity(userDto));
        } catch (IllegalArgumentException e) {
            throw new UserValidationException("Ошибка при сохранении пользователя");
        }

        return UserMapper.toDto(user);
    }
}
