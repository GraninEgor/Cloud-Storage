package org.example.cloudstorage.core.security.service;

import org.example.cloudstorage.api.dto.request.RefreshTokenDto;
import org.example.cloudstorage.api.dto.request.UserCredentialsDto;
import org.example.cloudstorage.api.dto.response.AccessAndRefreshTokenDto;

public interface AuthService {
    AccessAndRefreshTokenDto signIn(UserCredentialsDto userCredentialsDto);
    AccessAndRefreshTokenDto refresh(RefreshTokenDto refreshTokenDto);
}
