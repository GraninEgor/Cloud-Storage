package org.example.cloudstorage.controller;

import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.dto.FileInfoDto;
import org.example.cloudstorage.service.FileService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping(value = "/api/resource")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileInfoDto> upload(@RequestPart("file") MultipartFile file, @RequestParam String path) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        FileInfoDto fileInfo = fileService.upload(file, path);
        return ResponseEntity.ok(fileInfo);
    }

    @GetMapping
    public ResponseEntity<FileInfoDto> get(@RequestParam String path) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        FileInfoDto fileInfo = fileService.getInfo(path);
        return ResponseEntity.ok(fileInfo);
    }

}
