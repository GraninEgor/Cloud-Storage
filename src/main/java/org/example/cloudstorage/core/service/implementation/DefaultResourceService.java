package org.example.cloudstorage.core.service.implementation;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.api.dto.response.FileInfoDto;
import org.example.cloudstorage.core.exception.InvalidPathException;
import org.example.cloudstorage.core.exception.ObjectAlreadyExistsException;
import org.example.cloudstorage.core.exception.ObjectNotFoundException;
import org.example.cloudstorage.core.exception.StorageAccessException;
import org.example.cloudstorage.core.security.CustomUserDetails;
import org.example.cloudstorage.core.service.ResourceService;
import org.example.cloudstorage.core.util.FilePathUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultResourceService implements ResourceService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Override
    public FileInfoDto upload(MultipartFile file, String path) {
        if (!FilePathUtil.getType(path).equals("DIRECTORY")) {
            throw new InvalidPathException("Path is not a directory");
        }

        if (isResourceExists(buildAbsolutePath(path, file.getName()))) {
            throw new ObjectAlreadyExistsException("Object with same name already exists");
        }

        if (!isResourceExists(path)) {
            throw new ObjectNotFoundException("Parent folder didn't exist");
        }

        try {
            minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).object(getUserPrefix() + file.getName()).stream(file.getInputStream(), -1, 10485760).build());
            return getInfo(buildAbsolutePath(path, file.getName()));
        } catch (Exception e) {
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

    private String setFileName(String name) {
        return getUserPrefix() + name;
    }

    private String getUserPrefix() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new AuthenticationCredentialsNotFoundException("User not authenticated");
        }

        return "user-%s-files/".formatted(userDetails.getUsername());
    }

    private boolean isResourceExists(String absolutePath) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(absolutePath)
                            .build()
            );
            return true;
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                return false;
            }
            throw new StorageAccessException("MinIO error while checking object existence");
        } catch (Exception e) {
            throw new StorageAccessException("Unexpected error while checking object existence");
        }
    }

    private String buildAbsolutePath(String path){
        return getUserPrefix() + path;
    }

    private String buildAbsolutePath(String path, String fileName){
        return getUserPrefix() + path + fileName;
    }



}

