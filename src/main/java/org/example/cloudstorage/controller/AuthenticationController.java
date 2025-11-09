package org.example.cloudstorage.controller;

import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.dto.UserDto;
import org.example.cloudstorage.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthenticationController {

    private final UserService userService;

    @PostMapping("/sign-up")
    public ResponseEntity<Map<String, String>> singUp(@RequestBody UserDto userDto){                                                                 
        UserDto savedUser = userService.save(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("username", savedUser.getUsername()));
    }

}
