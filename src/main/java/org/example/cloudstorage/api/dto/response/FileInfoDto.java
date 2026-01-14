package org.example.cloudstorage.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class FileInfoDto {
    private String path;
    private String name;
    private Long size;
    private String type;
}
