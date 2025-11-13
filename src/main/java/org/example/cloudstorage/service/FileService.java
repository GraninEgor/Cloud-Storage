package org.example.cloudstorage.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.dto.FileInfoDto;
import org.example.cloudstorage.exception.*;
import org.example.cloudstorage.util.FilePathUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;


    private String getUserPrefix(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return "user-%s-files/".formatted(authentication.getName());
        }
        else throw new UserIsNotAuthenticated();
    }

    public FileInfoDto upload(MultipartFile file, String path){
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

    public FileInfoDto getInfo(String path) {
        if(!FilePathUtil.isValidPath(path)){
            throw new InvalidInputDataException("Invalid path");
        }
        try{
            StatObjectResponse response =
                    minioClient.statObject(
                            StatObjectArgs.builder().bucket(bucketName).object(getUserPrefix() + path).build());
            return new FileInfoDto(FilePathUtil.getResourceName(path), FilePathUtil.getResourcePath(path), response.size(), FilePathUtil.getType(path));
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                throw new ObjectNotFoundException("object not found");
            }
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
        catch (Exception e){
            throw new RuntimeException();
        }
    }

    public void delete(String path) {
        if(!FilePathUtil.isValidPath(path)){
            throw new InvalidInputDataException("Invalid path");
        }
        try{
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(getUserPrefix() + path).build());
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                throw new ObjectNotFoundException("object not found");
            }
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
        catch (Exception e){
            throw new RuntimeException();
        }
    }

    public InputStreamResource getFile(String path) {
        if(!FilePathUtil.isValidPath(path)){
            throw new InvalidInputDataException("Invalid path");
        }
        try {
            InputStream file = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(getUserPrefix() + path)
                            .build());

            return new InputStreamResource(file);
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                throw new ObjectNotFoundException("object not found");
            }
            throw new RuntimeException();
        }
        catch (Exception e){
            throw new RuntimeException();
        }
    }

    public List<FileInfoDto> findByQuery(String query) {
        if (!FilePathUtil.isValidQuery(query)) {
            throw new InvalidInputDataException("Invalid query");
        }

        List<FileInfoDto> infoDtos = new ArrayList<>();

        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(getUserPrefix())
                        .recursive(true)
                        .build());

        results.forEach(
                itemResult -> {
                    try {
                        Item item = itemResult.get();
                        if(FilePathUtil.getResourceName(item.objectName()).equals(query)){
                            infoDtos.add(new FileInfoDto(
                                    FilePathUtil.getResourceName(item.objectName()),
                                    FilePathUtil.getResourcePath(item.objectName()),
                                    item.size(),
                                    FilePathUtil.getType(item.objectName())
                                    )
                            );
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
               );

        return infoDtos;
    }

    public List<FileInfoDto> getFolderResources(String path){
        if(!FilePathUtil.isValidPath(path) || FilePathUtil.getType(path).equals("FILE")){
            throw new InvalidInputDataException("Invalid path");
        }

        List<FileInfoDto> infoDtos = new ArrayList<>();

        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(getUserPrefix() + path)
                        .recursive(false)
                        .build());

        results.forEach(
                itemResult -> {
                    try {
                        Item item = itemResult.get();
                        infoDtos.add(new FileInfoDto(
                                        FilePathUtil.getResourceName(item.objectName()),
                                        FilePathUtil.getResourcePath(item.objectName()),
                                        item.size(),
                                        FilePathUtil.getType(item.objectName())
                                )
                        );
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        );
        return infoDtos;
    }

    public FileInfoDto createFolder(String path){
        if(!FilePathUtil.isValidPath(path) || FilePathUtil.getResourceName(path).equals("FILE")){
            throw new InvalidInputDataException("Invalid path");
        }

        if(isPathToResourceExists(path)){
            throw new ObjectAlreadyExistsException("directory already exists");
        }

        if(!isPathToResourceExists(FilePathUtil.getResourcePath(path))){
            throw new ObjectNotFoundException("Parent folder didn't exist");
        }


        try {
            minioClient.putObject(
                    PutObjectArgs.builder().bucket(bucketName).object(getUserPrefix() + path).stream(
                                    new ByteArrayInputStream(new byte[] {}), 0, -1)
                            .build());
        }
        catch (Exception e){
            throw new RuntimeException();
        }
        return getInfo(path);
    }


    private Boolean isPathToResourceExists(String path){
        try {
            minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(getUserPrefix() + path)
                            .build());
            return true;
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                return false;
            }
            throw new RuntimeException();
        }
        catch (Exception e){
            throw new RuntimeException();
        }
    }


}
