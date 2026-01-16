package org.example.cloudstorage.api.controller;

import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.api.dto.response.ResourceInfoDto;
import org.example.cloudstorage.core.service.ResourceService;
import org.example.cloudstorage.core.validation.ValidPath;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
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
@RequiredArgsConstructor
@Validated
public class ResourceController {

    private final ResourceService resourceService;

    @PostMapping(name = "resource", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResourceInfoDto> uploadResource(@RequestPart("file") @NotNull MultipartFile file, @RequestParam(required = false, defaultValue = "/") @ValidPath String path) {
        ResourceInfoDto fileInfo = resourceService.upload(file, path);
        return ResponseEntity.ok(fileInfo);
    }

    @GetMapping("resource")
    public ResponseEntity<ResourceInfoDto> getResourceInfo(@RequestParam @ValidPath String path)  {
        ResourceInfoDto fileInfo = resourceService.getInfo(path);
        return ResponseEntity.ok(fileInfo);
    }

    @DeleteMapping("resource/delete")
    public ResponseEntity<Void> deleteFile(@RequestParam @ValidPath String path)  {
        resourceService.delete(path);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("resource/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam @ValidPath String path)  {
        InputStreamResource file = resourceService.getResource(path);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(file);
    }

    @GetMapping("resource/search")
    public ResponseEntity<List<ResourceInfoDto>> searchFiles(@RequestParam String query){
        List<ResourceInfoDto> resourceInfoDtos = resourceService.findByQuery(query);
        return ResponseEntity.ok(resourceInfoDtos);
    }

    @GetMapping("/directory")
    public ResponseEntity<List<ResourceInfoDto>> getDirectoryResourcesInfo(@RequestParam @ValidPath String path){
        List<ResourceInfoDto> resources = resourceService.getDirectoryResources(path);
        return ResponseEntity.ok(resources);
    }

    @PostMapping("/directory")
    public ResponseEntity<ResourceInfoDto> createDirectory(@RequestParam @ValidPath String path){
        ResourceInfoDto directory = resourceService.createDirectory(path);
        return ResponseEntity.status(HttpStatus.CREATED).body(directory);
    }

}
