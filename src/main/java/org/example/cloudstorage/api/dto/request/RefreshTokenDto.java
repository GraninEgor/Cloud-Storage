package org.example.cloudstorage.api.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RefreshTokenDto {
    String refreshToken;
}
