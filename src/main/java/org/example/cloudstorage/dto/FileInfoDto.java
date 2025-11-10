package org.example.cloudstorage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileInfoDto {
    private String path;
    private String name;
    private String size;
    private String type;
}
