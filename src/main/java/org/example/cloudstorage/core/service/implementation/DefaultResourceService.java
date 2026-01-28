package org.example.cloudstorage.core.service.implementation;

import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.MinioException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
        if (!path.equals("/")){
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
        if (!path.equals("/")){
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
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(String path) {
        if (!isResourceExists(buildAbsolutePath(path))){
            throw new ObjectNotFoundException("Object doesn't exist");
        }

        try {
            switch (ResourcePathUtil.getType(path)) {
                case DIRECTORY -> {
                    List<String> resourcePaths = getDirectoryRecoursesPaths(path);

                    List<DeleteObject> objects = new LinkedList<>();

                    for(String resourcePath : resourcePaths){
                        objects.add(new DeleteObject(resourcePath));
                    }

                    minioClient.removeObjects(RemoveObjectsArgs.builder().bucket(BUCKET_NAME).objects(objects).build());
                }
                case FILE -> {
                    minioClient.removeObject(RemoveObjectArgs.builder().bucket(BUCKET_NAME).object(buildAbsolutePath(path)).build());
                }
            }
        } catch (MinioException e) {
            throw new StorageAccessException(e.getMessage());
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public InputStreamResource getResource(String path) {
        if (!isResourceExists(buildAbsolutePath(path))){
            throw new ObjectNotFoundException("Object doesn't exist");
        }

        try {
            switch (ResourcePathUtil.getType(path)) {
                case DIRECTORY -> {
                    List<String> recoursesPaths = getDirectoryRecoursesPaths(path);

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
                        for (String resourcePath : recoursesPaths) {
                            try (InputStream is = minioClient.getObject(
                                    GetObjectArgs.builder()
                                            .bucket(BUCKET_NAME)
                                            .object(resourcePath)
                                            .build())) {

                                ZipEntry entry = new ZipEntry(resourcePath);
                                zipOutputStream.putNextEntry(entry);

                                byte[] buffer = new byte[4096];
                                int len;
                                while ((len = is.read(buffer)) > 0) {
                                    zipOutputStream.write(buffer, 0, len);
                                }
                                zipOutputStream.closeEntry();
                            }
                        }
                    }
                    return new InputStreamResource(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
                }
                case FILE -> {
                    InputStream file = minioClient.getObject(
                            GetObjectArgs.builder()
                                    .bucket(BUCKET_NAME)
                                    .object(buildAbsolutePath(path))
                                    .build());

                    return new InputStreamResource(file);
                }
                default -> throw new IllegalArgumentException("Unknown resource type: " + path);
            }
        } catch (MinioException e) {
            throw new StorageAccessException(e.getMessage());
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ResourceInfoDto> findByQuery(String query) {
        List<ResourceInfoDto> userResourcesPaths = getDirectoryResources("/", true);

        List<ResourceInfoDto> result = new ArrayList<>();

        userResourcesPaths.forEach(p -> {
            if(p.getName().contains(query)){
                result.add(p);
            }
        });

        return result;
    }

    @Override
    public ResourceInfoDto changeResourcePath(String from, String to) {
        if (!ResourcePathUtil.getType(from).equals(ResourceType.FILE) ){
            throw new InvalidInputDataException("The path to the object is not absolute");
        }

        if (!ResourcePathUtil.getType(to).equals(ResourceType.FILE) ){
            throw new InvalidInputDataException("The path to the object is not absolute");
        }

        if (!isResourceExists(buildAbsolutePath(from))){
            throw new ObjectNotFoundException("Object doesn't exist");
        }

        try{
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(BUCKET_NAME)
                            .object(buildAbsolutePath(to))
                            .source(
                                    CopySource.builder()
                                            .bucket(BUCKET_NAME)
                                            .object(buildAbsolutePath(from))
                                            .build())
                            .build());
        } catch (MinioException e) {
            throw new StorageAccessException(e.getMessage());
        } catch (Exception e){
            throw new RuntimeException(e);
        }

        return getInfo(to);
    }

    @Override
    public ResourceInfoDto createDirectory(String path) {
        if (!ResourcePathUtil.getType(path).equals(ResourceType.DIRECTORY)){
            throw new InvalidInputDataException("Path is not a directory");
        }

        try {
            minioClient.putObject(
                    PutObjectArgs.builder().bucket(BUCKET_NAME).object(buildAbsolutePath(path)).stream(
                                    new ByteArrayInputStream(new byte[] {}), 0, -1)
                            .build());
            return getInfo(path);
        } catch (MinioException e) {
            throw new StorageAccessException(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ResourceInfoDto> getDirectoryResources(String path, boolean recursively) {
        if (!ResourcePathUtil.getType(path).equals(ResourceType.DIRECTORY)){
            throw new InvalidInputDataException("Path is not a directory");
        }

        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(BUCKET_NAME)
                        .prefix(buildAbsolutePath(path))
                        .recursive(recursively)
                        .build());

        try {
            return resourceMapper.toDtos(results);
        } catch (MinioException e) {
            throw new StorageAccessException(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> getDirectoryRecoursesPaths(String path){
        List<String> resourcesPaths = new LinkedList<>();

        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder().bucket(BUCKET_NAME).prefix(buildAbsolutePath(path)).recursive(true).build());

        try {
            for(Result<Item> result : results){
                resourcesPaths.add(result.get().objectName());
            }
        } catch (MinioException e) {
            throw new StorageAccessException(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return resourcesPaths;
    }


    private String getUserPrefix() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new AuthenticationCredentialsNotFoundException("User not authenticated");
        }

        return "user-%s-files".formatted(userDetails.getUsername());
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
        if (path.equals("/")){
            return getUserPrefix() + path;
        }
        else{
            return getUserPrefix() + "/" + path;
        }
    }

    private String buildAbsolutePath(String path, String fileName){
        if (path.equals("/")){
            return getUserPrefix() + path + fileName;
        }
        else{
            return getUserPrefix() + "/" + path + fileName;
        }
    }



}

