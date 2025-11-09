package org.example.cloudstorage;

import org.example.cloudstorage.database.repository.UserRepository;
import org.example.cloudstorage.dto.UserRegisterDto;
import org.example.cloudstorage.mapper.UserRegisterMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
public class AuthenticationControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest");

    @BeforeEach
    void cleanDatabase(@Autowired UserRepository userRepository) {
        userRepository.deleteAll();
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Test
    void testRegistration_Success() throws Exception {
        UserRegisterDto userRegisterDto = new UserRegisterDto("pudge", "111");
        mockMvc.perform(post("/api/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegisterDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(userRegisterDto.getUsername()));
        assertTrue(userRepository.findUserByUsername("pudge").isPresent());
    }

    @Test
    void testRegistrationUniqueUsername_Failed() throws Exception {
        UserRegisterDto userRegisterDto1 = new UserRegisterDto("pudge", "111");
        UserRegisterDto userRegisterDto2 = new UserRegisterDto("pudge", "123");
        mockMvc.perform(post("/api/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegisterDto1)));
        mockMvc.perform(post("/api/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRegisterDto2))).andExpect(status().isConflict());
    }

    @Test
    void testLogin_Success() throws Exception {
        UserRegisterDto userRegisterDto = new UserRegisterDto("pudge", "111");
        mockMvc.perform(post("/api/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegisterDto)));
        mockMvc.perform(post("/api/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegisterDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(userRegisterDto.getUsername()));
    }

    @Test
    void testLogin_Failed() throws Exception {
        UserRegisterDto userRegisterDto1 = new UserRegisterDto("pudge", "111");
        UserRegisterDto userRegisterDto2 = new UserRegisterDto("pudge", "1121");
        mockMvc.perform(post("/api/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRegisterDto1)));
        mockMvc.perform(post("/api/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegisterDto2)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCheckCurrentUser_Success() throws Exception {
        UserRegisterDto userRegisterDto = new UserRegisterDto("pudge", "111");
        mockMvc.perform(post("/api/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRegisterDto)));
        MvcResult result = mockMvc.perform(post("/api/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRegisterDto))).andExpect(status().isOk()).andReturn();
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession();
        mockMvc.perform(get("/api/me").session(session)).andExpect(status().isOk()).andExpect(jsonPath("$.username").value(userRegisterDto.getUsername()));
    }
}
