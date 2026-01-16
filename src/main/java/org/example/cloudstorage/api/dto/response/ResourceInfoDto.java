package org.example.cloudstorage.api.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResourceInfoDto {
    private String path;
    private String name;
    private Long size;
    private String type;
}
