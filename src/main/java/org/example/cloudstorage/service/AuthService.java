package org.example.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.database.entity.User;
import org.example.cloudstorage.database.repository.UserRepository;
import org.example.cloudstorage.dto.UserDto;
import org.example.cloudstorage.exception.UserValidationException;
import org.example.cloudstorage.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    @Value("${app.min-username-length}")
    private int minUsernameLength;

    private final UserRepository userRepository;

    public UserDto save(UserDto userDto) {
        if(userDto.getUsername().length() > minUsernameLength){
            throw new UserValidationException("Длина username должна быть больше " + minUsernameLength);
        }

        if (userRepository.existsByUsername(userDto.getUsername()).isPresent()) {
            throw new UserValidationException("Пользователь с username" + userDto.getUsername() + " уже существует");
        }

        User user;
        try {
            user = userRepository.save(UserMapper.toEntity(userDto));
        } catch (DataIntegrityViolationException e) {
            throw new UserValidationException("Пользователь с username" + userDto.getUsername() + " уже существует"); // если одновременно зарегистрируются доп. проверка
        }

        return UserMapper.toDto(user);
    }
}
