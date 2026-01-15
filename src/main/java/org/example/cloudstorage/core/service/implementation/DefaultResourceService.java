package org.example.cloudstorage.core.service.implementation;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.api.dto.response.FileInfoDto;
import org.example.cloudstorage.core.exception.InvalidInputDataException;
import org.example.cloudstorage.core.exception.ObjectAlreadyExistsException;
import org.example.cloudstorage.core.exception.ObjectNotFoundException;
import org.example.cloudstorage.core.exception.StorageAccessException;
import org.example.cloudstorage.core.security.CustomUserDetails;
import org.example.cloudstorage.core.service.ResourceService;
import org.example.cloudstorage.core.util.FilePathUtil;
import org.example.cloudstorage.core.util.ResourceType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;

import static org.example.cloudstorage.core.util.FilePathUtil.isFileNameValid;

@Service
@RequiredArgsConstructor
public class DefaultResourceService implements ResourceService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Override
    public FileInfoDto upload(MultipartFile file, String path) {
        if (!Objects.equals(path, "/")){
            if (!FilePathUtil.getType(path).equals(ResourceType.DIRECTORY)) {
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
            minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).object(buildAbsolutePath(path, file.getOriginalFilename())).stream(file.getInputStream(), -1, 10485760).build());
        } catch (IOException e) {
            throw new StorageAccessException();
        } catch (Exception e){
            throw new RuntimeException("File input stream Error");
        }

        return getInfo(buildAbsolutePath(path, file.getName()));
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

