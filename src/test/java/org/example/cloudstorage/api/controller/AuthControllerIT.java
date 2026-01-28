package org.example.cloudstorage.api.controller;

import io.minio.MinioClient;
import org.example.cloudstorage.testutil.objects.TestUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonNode;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private MinioClient minioClient;

    @Test
    void signIn_Success() throws Exception {

        TestUser user = registerUser();

        mockMvc.perform(post("/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                        "username": "%s",
                        "password": "%s"
                        }
                        """.formatted(user.username(), user.password()))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("accessToken").exists())
                .andExpect(jsonPath("refreshToken").exists());
    }

    @Test
    void refresh_Success() throws Exception{

        TestUser user = registerUser();

        MvcResult result = mockMvc.perform(post("/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                        "username": "%s",
                        "password": "%s"
                        }
                        """.formatted(user.username(), user.password())))
                .andReturn();

        String signInResponse = result.getResponse().getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(signInResponse);

        String refreshToken = jsonNode.get("refreshToken").asText();

        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "refreshToken": "%s"
                        }
                        """.formatted(refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("accessToken").exists())
                .andExpect(jsonPath("refreshToken").exists());
    }

    private TestUser registerUser() throws Exception {
        TestUser user = new TestUser("user_" + UUID.randomUUID(), "pudge123");

        mockMvc.perform(post("/sign-up")
                        .contentType("application/json")
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s"
                                }
                                """.formatted(user.username(), user.password())))
                .andExpect(status().isCreated());

        return user;
    }
}


