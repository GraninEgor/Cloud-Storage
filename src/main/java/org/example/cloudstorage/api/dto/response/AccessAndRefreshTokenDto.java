package org.example.cloudstorage.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccessAndRefreshTokenDto {
    private String accessToken;
    private String refreshToker;
}
