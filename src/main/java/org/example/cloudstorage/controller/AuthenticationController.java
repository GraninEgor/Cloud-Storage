package org.example.cloudstorage.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.dto.UserRegisterDto;
import org.example.cloudstorage.service.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/sign-up")
    public ResponseEntity<Map<String, String>> signUp(@RequestBody UserRegisterDto userRegisterDto){
        UserRegisterDto savedUser = authenticationService.save(userRegisterDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("username", savedUser.getUsername()));
    }

    @GetMapping("/me")
    public ResponseEntity getCurrentUser(@AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(Map.of("username", userDetails.getUsername()));
    }

}
