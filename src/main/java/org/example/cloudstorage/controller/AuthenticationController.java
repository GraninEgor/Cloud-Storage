package org.example.cloudstorage.controller;

import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.database.entity.User;
import org.example.cloudstorage.dto.UserDto;
import org.example.cloudstorage.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthService authService;

    @PostMapping("/sign-up")
    public ResponseEntity<Map<String, String>> singUp(@RequestBody UserDto userDto){
        UserDto savedUser = authService.save(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("username", savedUser.getUsername()));
    }


}
