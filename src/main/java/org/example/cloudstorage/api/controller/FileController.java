package org.example.cloudstorage.api.controller;

import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.api.dto.response.FileInfoDto;
import org.example.cloudstorage.core.service.FileService;
import org.example.cloudstorage.core.validation.ValidPath;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("resource")
@RequiredArgsConstructor
@Validated
public class FileController {

    private final FileService fileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileInfoDto> uploadFile(@RequestPart("file") @NotNull MultipartFile file, @RequestParam @ValidPath String path) {
        FileInfoDto fileInfo = fileService.upload(file, path);
        return ResponseEntity.ok(fileInfo);
    }

    @GetMapping
    public ResponseEntity<FileInfoDto> getFileInfo(@RequestParam @ValidPath String path)  {
        FileInfoDto fileInfo = fileService.getInfo(path);
        return ResponseEntity.ok(fileInfo);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteFile(@RequestParam @ValidPath String path)  {
        fileService.delete(path);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam @ValidPath String path)  {
        InputStreamResource file = fileService.getFile(path);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(file);
    }

    @GetMapping("/search")
    public ResponseEntity<List<FileInfoDto>> searchFiles(@RequestParam String query){
        List<FileInfoDto> fileInfoDtos = fileService.findByQuery(query);
        return ResponseEntity.ok(fileInfoDtos);
    }
}
