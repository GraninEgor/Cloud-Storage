package org.example.cloudstorage.core.service;

import org.example.cloudstorage.api.dto.request.UserRegistrationDto;

public interface  UserService {
    void createUser(UserRegistrationDto userRegistrationDto);
}
