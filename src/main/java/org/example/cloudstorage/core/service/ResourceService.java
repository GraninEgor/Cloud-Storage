package org.example.cloudstorage.core.service;

import org.example.cloudstorage.api.dto.response.FileInfoDto;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ResourceService {
    FileInfoDto upload(MultipartFile file, String path);
    FileInfoDto getInfo(String path);
    void delete(String path);
    InputStreamResource getFile(String path);
    List<FileInfoDto> findByQuery(String query);
}
