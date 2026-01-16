package org.example.cloudstorage.core.mapper;

import io.minio.Result;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.messages.Item;
import org.example.cloudstorage.api.dto.response.ResourceInfoDto;
import org.example.cloudstorage.core.util.ResourcePathUtil;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Component
public class ResourceMapper {

    public ResourceInfoDto toDto(StatObjectResponse response){
        return ResourceInfoDto.builder()
                .path(ResourcePathUtil.getResourcePath(response.object()))
                .name(ResourcePathUtil.getResourceName(response.object()))
                .size((response.size()) == 0 ? null : response.size())
                .type(ResourcePathUtil.getType(response.object()).toString())
                .build();
    }

    public List<ResourceInfoDto> toDtos(Iterable<Result<Item>> results) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        List<ResourceInfoDto> dtos = new ArrayList<>();

        for (Result<Item> r : results) {
            Item item = r.get();
            dtos.add(
                    ResourceInfoDto.builder()
                            .path(ResourcePathUtil.getResourcePath(item.objectName()))
                            .name(ResourcePathUtil.getResourceName(item.objectName()))
                            .size((item.size()) == 0 ? null : item.size())
                            .type(ResourcePathUtil.getType(item.objectName()).toString())
                            .build()
            );
        }

        return dtos;
    }
}
