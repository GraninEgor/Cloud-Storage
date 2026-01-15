package org.example.cloudstorage.core.service.implementation;

import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.api.dto.request.UserRegistrationDto;
import org.example.cloudstorage.core.database.entity.User;
import org.example.cloudstorage.core.database.repository.UserRepository;
import org.example.cloudstorage.core.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultUserService implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;


    @Override
    public void createUser(UserRegistrationDto userRegistrationDto) {
        User user = User.builder()
                .username(userRegistrationDto.getUsername())
                .password(passwordEncoder.encode(userRegistrationDto.getPassword()))
                .build();

        userRepository.save(user);
    }


}
