package com.ketealare.identityService.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ketealare.identityService.dto.request.UserCreationRequest;
import com.ketealare.identityService.dto.response.UserResponse;
import com.ketealare.identityService.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;
    private UserCreationRequest userRequest;
    private UserResponse userResponse;
    private LocalDate dob;

    @BeforeEach
    void initData(){

        dob = LocalDate.of(2003, 3, 23);

        userRequest = UserCreationRequest.builder()
                .username("charlies")
                .password("12345678")
                .firstName("charlies")
                .lastName("adam")
                .dob(dob)
                .build();

        userResponse = UserResponse.builder()
                .id("41209acd")
                .username("charlies")
                .firstName("charlies")
                .lastName("adam")
                .dob(dob)
                .build();
    }

    @Test
    void createUser_valid_RequestTest() throws Exception {

        // Given
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        String content = objectMapper.writeValueAsString(userRequest);

//        when(userService.createUser(any())).thenReturn(userResponse);

        // When
        mockMvc.perform(MockMvcRequestBuilders
                .post("/users")
                .contentType(MediaType.APPLICATION_JSON_VALUE)

        // THEN
                .content(content)).andExpect(status().isOk())
                .andExpect(jsonPath("code").value("1000"))
                .andExpect(jsonPath("result.id").value("41209acd"))
        ;
    }

    @Test
    void createUser_username_invalid_RequestTest() throws Exception {

        // Given
        userRequest.setUsername("char");

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        String content = objectMapper.writeValueAsString(userRequest);

        when(userService.createUser(any())).thenReturn(userResponse);

        // When
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/users")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)

                        // THEN
                        .content(content)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value("1004"))
                .andExpect(jsonPath("message").value("User must be at least 8 character!"))
        ;
    }
}
