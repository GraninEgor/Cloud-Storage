package org.example.cloudstorage.core.service.implementation;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.api.dto.response.ResourceInfoDto;
import org.example.cloudstorage.core.exception.InvalidInputDataException;
import org.example.cloudstorage.core.exception.ObjectAlreadyExistsException;
import org.example.cloudstorage.core.exception.ObjectNotFoundException;
import org.example.cloudstorage.core.exception.StorageAccessException;
import org.example.cloudstorage.core.mapper.ResourceMapper;
import org.example.cloudstorage.core.security.CustomUserDetails;
import org.example.cloudstorage.core.service.ResourceService;
import org.example.cloudstorage.core.util.ResourcePathUtil;
import org.example.cloudstorage.core.util.ResourceType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static org.example.cloudstorage.core.util.ResourcePathUtil.isFileNameValid;

@Service
@RequiredArgsConstructor
public class DefaultResourceService implements ResourceService {

    private final MinioClient minioClient;
    private final ResourceMapper resourceMapper;

    @Value("${minio.bucket-name}")
    private String BUCKET_NAME;

    @Override
    public ResourceInfoDto upload(MultipartFile file, String path) {
        if (path != null){
            if (!ResourcePathUtil.getType(path).equals(ResourceType.DIRECTORY)) {
                throw new InvalidInputDataException("Path is not a directory");
            }
            if (!isResourceExists(path)) {
                throw new ObjectNotFoundException("Parent folder doesn't exist");
            }
        }

        if (!isFileNameValid(file.getOriginalFilename())) {
            throw new InvalidInputDataException("Invalid file name");
        }

        if (isResourceExists(buildAbsolutePath(path, file.getOriginalFilename()))) {
            throw new ObjectAlreadyExistsException("Object with same name already exists");
        }

        try {
            minioClient.putObject(PutObjectArgs.builder().bucket(BUCKET_NAME).object(buildAbsolutePath(path, file.getOriginalFilename())).stream(file.getInputStream(), -1, 10485760).build());
        } catch (IOException e) {
            throw new StorageAccessException(e.getMessage());
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
        if (path!=null){
            return getInfo(path + file.getOriginalFilename());
        }
        return getInfo( file.getOriginalFilename());
    }

    @Override
    public ResourceInfoDto getInfo(String path) {
        try {
            StatObjectResponse response = minioClient.statObject(
                    StatObjectArgs.builder().bucket(BUCKET_NAME).object(buildAbsolutePath(path)).build());
            return resourceMapper.toDto(response);
        } catch (MinioException e) {
            throw new StorageAccessException(e.getMessage());
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void delete(String path) {

    }

    @Override
    public InputStreamResource getFile(String path) {
        return null;
    }

    @Override
    public List<ResourceInfoDto> findByQuery(String query) {
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
                            .bucket(BUCKET_NAME)
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

