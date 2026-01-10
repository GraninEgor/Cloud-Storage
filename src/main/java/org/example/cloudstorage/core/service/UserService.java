package org.example.cloudstorage.core.service;

import org.example.cloudstorage.api.dto.request.UserCredentialsDto;
import org.example.cloudstorage.api.dto.request.UserRegistrationDto;
import org.example.cloudstorage.api.dto.response.AccessAndRefreshTokenDto;

public interface UserService {
    void createUser(UserRegistrationDto userRegistrationDto);
}
