package org.example.cloudstorage.core.service.implementation;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.api.dto.response.FileInfoDto;
import org.example.cloudstorage.core.security.CustomUserDetails;
import org.example.cloudstorage.core.service.FileService;
import org.example.cloudstorage.core.util.FilePathUtil;
import org.springframework.core.io.InputStreamResource;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultFileService implements FileService {

    private final MinioClient minioClient;

    @Override
    public FileInfoDto upload(MultipartFile file, String path) {
        if(!FilePathUtil.isValidPath(path) || FilePathUtil.getType(path).equals("DIRECTORY")){
            throw new InvalidInputDataException("Invalid path");
        }

        if(isPathToResourceExists(path)){
            throw new ObjectAlreadyExistsException("already exists");
        }

        if(!isPathToResourceExists(FilePathUtil.getResourcePath(path))){
            throw new ObjectNotFoundException("Parent folder didn't exist");
        }

        try {
            minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).object(getUserPrefix() + file.getName()).stream(file.getInputStream(), -1, 10485760).build());
            return getInfo(getUserPrefix() + path + file.getName());
        }
        catch (Exception e) {
            throw new RuntimeException();
        }
    }

    @Override
    public FileInfoDto getInfo(String path) {
        return null;
    }

    @Override
    public void delete(String path) {

    }

    @Override
    public InputStreamResource getFile(String path) {
        return null;
    }

    @Override
    public List<FileInfoDto> findByQuery(String query) {
        return List.of();
    }

    private String setFileName(String name){
        return getUserPrefix() + name;
    }

    private String getUserPrefix() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new AuthenticationCredentialsNotFoundException("User not authenticated");
        }

        return "user-%s-files/".formatted(userDetails.getUsername());
    }
}
