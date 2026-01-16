package org.example.cloudstorage.core.service;

import org.example.cloudstorage.api.dto.response.ResourceInfoDto;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ResourceService {
    ResourceInfoDto upload(MultipartFile file, String path);
    ResourceInfoDto getInfo(String path);
    void delete(String path);
    InputStreamResource getFile(String path);
    List<ResourceInfoDto> findByQuery(String query);
    ResourceInfoDto createDirectory(String path);
    List<ResourceInfoDto> getDirectoryResources(String path);
}
