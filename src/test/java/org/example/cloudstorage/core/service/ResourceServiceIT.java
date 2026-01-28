package org.example.cloudstorage.core.service;

import org.example.cloudstorage.api.dto.response.ResourceInfoDto;
import org.example.cloudstorage.core.security.JwtService;
import org.example.cloudstorage.core.security.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Testcontainers
@SpringBootTest
@TestPropertySource(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
                "org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration"
})
public class ResourceServiceIT {

    @Container
    static GenericContainer<?> minio =
            new GenericContainer<>("minio/minio:latest")
                    .withExposedPorts(9000)
                    .withEnv("MINIO_ROOT_USER", "minioadmin")
                    .withEnv("MINIO_ROOT_PASSWORD", "minioadmin")
                    .withCommand("server /data");

    @DynamicPropertySource
    static void minioProperties(DynamicPropertyRegistry registry) {
        String url = "http://" + minio.getHost() + ":" + minio.getMappedPort(9000);
        registry.add("spring.cloud.minio.url", () -> url);
        registry.add("spring.cloud.minio.access-key", () -> "minioadmin");
        registry.add("spring.cloud.minio.secret-key", () -> "minioadmin");
    }

    @Autowired
    ResourceService resourceService;

    @MockitoBean
    AuthService authService;

    @MockitoBean
    UserService userService;

    @Test
    void upload_Success(){
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "I love dota".getBytes()
        );


        ResourceInfoDto uploadedInfo =  resourceService.upload(file, "/");

        assertNotNull(uploadedInfo);
        assertNotNull(uploadedInfo.getName());
        assertEquals(file.getOriginalFilename(), uploadedInfo.getName());
    }
}
