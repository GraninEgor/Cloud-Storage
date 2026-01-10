package org.example.cloudstorage.core.security.service;

import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.api.dto.request.RefreshTokenDto;
import org.example.cloudstorage.api.dto.request.UserCredentialsDto;
import org.example.cloudstorage.api.dto.response.AccessAndRefreshTokenDto;
import org.example.cloudstorage.core.database.entity.User;
import org.example.cloudstorage.core.database.repository.UserRepository;
import org.example.cloudstorage.core.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultAuthService implements AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AccessAndRefreshTokenDto signIn(UserCredentialsDto userCredentialsDto){
        User user = userRepository.findByUsername(userCredentialsDto.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if(!passwordEncoder.matches(userCredentialsDto.getPassword(), user.getPassword())){
            throw new BadCredentialsException("Email or password is not correct");
        }

        return jwtService.generateAuthTokens(user.getUsername());
    }

    @Override
    public AccessAndRefreshTokenDto refresh(RefreshTokenDto refreshTokenDto){
        if (refreshTokenDto == null || !jwtService.validateJwtToken(refreshTokenDto.getRefreshToken())){
            throw new BadCredentialsException("Invalid refresh token");
        }

        User user = userRepository.findByUsername(jwtService.getUsernameFromToken(refreshTokenDto.getRefreshToken()))
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        return jwtService.refresh(user.getUsername(), refreshTokenDto.getRefreshToken());
    }


}
