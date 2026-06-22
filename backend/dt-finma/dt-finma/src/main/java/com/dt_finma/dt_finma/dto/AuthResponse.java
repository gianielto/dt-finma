package com.dt_finma.dt_finma.dto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

public class AuthResponse {
    private String token;
    private String email;
    private String role;
}
