package org.example.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.database.entity.User;
import org.example.cloudstorage.database.repository.UserRepository;
import org.example.cloudstorage.dto.UserRegisterDto;
import org.example.cloudstorage.exception.UserValidationException;
import org.example.cloudstorage.exception.UsernameAlreadyExistsException;
import org.example.cloudstorage.mapper.UserRegisterMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthenticationService implements UserDetailsService {
    @Value("${app.min-username-length}")
    private int minUsernameLength;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserRegisterDto save(UserRegisterDto userRegisterDto) {
        if(userRegisterDto.getUsername().length() < minUsernameLength){
            throw new UserValidationException("Длина username должна быть больше " + minUsernameLength);
        }

        if (userRepository.findUserByUsername(userRegisterDto.getUsername()).isPresent()) {
            throw new UsernameAlreadyExistsException("Пользователь с username" + userRegisterDto.getUsername() + " уже существует");
        }

        User user = UserRegisterMapper.toEntity(userRegisterDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        try {
            User savedUser = userRepository.save(user);
            return UserRegisterMapper.toDto(savedUser);
        } catch (DataIntegrityViolationException e) {
            throw new UsernameAlreadyExistsException("Пользователь с username" + userRegisterDto.getUsername() + " уже существует"); // если одновременно зарегистрируются доп. проверка
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findUserByUsername(username)
                .map(user -> org.springframework.security.core.userdetails.User
                        .withUsername(user.getUsername())
                        .password(user.getPassword())
                        .authorities("USER")
                        .build()
                )
                .orElseThrow(() -> new UsernameNotFoundException("Не удалось получить user " + username));
    }
}
