package org.example.cloudstorage.service;

import io.minio.*;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.dto.FileInfoDto;
import org.example.cloudstorage.util.FilePathFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
public class FileService {

    private final MinioClient minioClient;

    public FileInfoDto upload(MultipartFile file) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.putObject(PutObjectArgs.builder().bucket("my-bucket").object("test-object").stream(file.getInputStream(), -1, 10485760 ).build());
        return null;
    }

    public FileInfoDto getInfo(String path) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        StatObjectResponse objectStat =
                minioClient.statObject(
                        StatObjectArgs.builder().bucket("my-bucket").object(path).build());
        FileInfoDto info = new FileInfoDto();
        return info;
    }
}
