package org.example.cloudstorage.service;

import io.minio.*;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.dto.FileInfoDto;
import org.example.cloudstorage.exception.AppException;
import org.example.cloudstorage.exception.InvalidPathException;
import org.example.cloudstorage.exception.ObjectNotFoundException;
import org.example.cloudstorage.util.FilePathUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
public class FileService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    public FileInfoDto upload(MultipartFile file, String path) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).object(file.getName()).stream(file.getInputStream(), -1, 10485760 ).build());
        return null;
    }

    public FileInfoDto getInfo(String path) throws ServerException, InsufficientDataException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException, ErrorResponseException {
        if(!FilePathUtil.isValid(path)){
            throw new InvalidPathException("Invalid path");
        }
        try{
            StatObjectResponse response =
                    minioClient.statObject(
                            StatObjectArgs.builder().bucket(bucketName).object(path).build());
            return new FileInfoDto(FilePathUtil.getResourceName(path), FilePathUtil.getPath(path), response.size(), FilePathUtil.getType(path));
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                throw new ObjectNotFoundException("object not found");
            }
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public void delete(String path) throws ServerException, InsufficientDataException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException, ErrorResponseException {
        if(!FilePathUtil.isValid(path)){
            throw new InvalidPathException("Invalid path");
        }
        try{
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(path).build());
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                throw new ObjectNotFoundException("object not found");
            }
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public InputStreamResource getFile(String path) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        if(!FilePathUtil.isValid(path)){
            throw new InvalidPathException("Invalid path");
        }
        try {
            InputStream file = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build());

            return new InputStreamResource(file);
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                throw new ObjectNotFoundException("object not found");
            }
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
