package org.example.cloudstorage.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.api.dto.request.RefreshTokenDto;
import org.example.cloudstorage.api.dto.request.UserCredentialsDto;
import org.example.cloudstorage.api.dto.response.AccessAndRefreshTokenDto;
import org.example.cloudstorage.core.security.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("sign-in")
    public ResponseEntity<AccessAndRefreshTokenDto> singIn(@Valid @RequestBody UserCredentialsDto userCredentialsDto){
        AccessAndRefreshTokenDto accessAndRefreshTokenDto = authService.signIn(userCredentialsDto);
        return ResponseEntity.ok(accessAndRefreshTokenDto);
    }

    @PostMapping("refresh")
    public ResponseEntity<AccessAndRefreshTokenDto> refresh(@RequestBody RefreshTokenDto refreshTokenDto){
        AccessAndRefreshTokenDto accessAndRefreshTokenDto = authService.refresh(refreshTokenDto);
        return ResponseEntity.ok(accessAndRefreshTokenDto);
    }

}
