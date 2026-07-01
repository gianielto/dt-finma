package com.dt_finma.dt_finma.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.dt_finma.dt_finma.BaseIntegrationTest;
import com.dt_finma.dt_finma.dto.RegisterRequest;
import com.dt_finma.dt_finma.repository.CategoryRepository;
import com.dt_finma.dt_finma.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class AuthIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Registro exitoso crea usuario, devuelve token JWT y categorias por defecto")
    void register_validRequest_shouldCreateUserAndReturnToken() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("nuevo@test.com");
        request.setPassword("claveSegura123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", not(emptyString())))
                .andExpect(jsonPath("$.email", is("nuevo@test.com")))
                .andExpect(jsonPath("$.role", is("USER")));

        // Verificar en base de datos que el usuario existe y las categorias por defecto
        assertEquals(1, userRepository.count());
        assertEquals(8, categoryRepository.count()); // las 8 categorias por defecto
    }

    @Test
    @DisplayName("Registro con email duplicado retorna 409 Conflict")
    void register_duplicateEmail_shouldReturn409() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("duplicado@test.com");
        request.setPassword("claveSegura123");

        // Primer registro — debe funcionar
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Segundo registro con el mismo email — debe fallar
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Conflict")));
    }

    @Test
    @DisplayName("Registro con email invalido retorna 400 con detalle de validacion")
    void register_invalidEmail_shouldReturn400WithValidationDetail() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("esto-no-es-email");
        request.setPassword("claveSegura123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.email", notNullValue()));
    }

    @Test
    @DisplayName("Login exitoso retorna token JWT valido")
    void login_validCredentials_shouldReturnToken() throws Exception {
        // ARRANGE: registrar primero
        RegisterRequest register = new RegisterRequest();
        register.setEmail("login@test.com");
        register.setPassword("claveSegura123");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)));

        // ACT: hacer login
        String loginBody = """
                {
                    "email": "login@test.com",
                    "password": "claveSegura123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(emptyString())))
                .andExpect(jsonPath("$.email", is("login@test.com")));
    }

    @Test
    @DisplayName("Login con contrasena incorrecta retorna 401 Unauthorized")
    void login_wrongPassword_shouldReturn401() throws Exception {
        // ARRANGE: registrar primero
        RegisterRequest register = new RegisterRequest();
        register.setEmail("wrongpass@test.com");
        register.setPassword("claveCorrecta123");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)));

        // ACT: intentar login con contraseña incorrecta
        String loginBody = """
                {
                    "email": "wrongpass@test.com",
                    "password": "claveIncorrecta"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));
    }
}