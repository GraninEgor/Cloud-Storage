package org.example.cloudstorage.core.mapper;

import io.minio.StatObjectResponse;
import org.example.cloudstorage.api.dto.response.ResourceInfoDto;
import org.example.cloudstorage.core.util.ResourcePathUtil;
import org.springframework.stereotype.Component;

@Component
public class ResourceMapper {

    public ResourceInfoDto toDto(StatObjectResponse response){
        return ResourceInfoDto.builder()
                .name(ResourcePathUtil.getResourceName(response.object()))
                .path(ResourcePathUtil.getResourcePath(response.object()))
                .size(response.size())
                .type(ResourcePathUtil.getType(response.object()).toString())
                .build();
    }
}
