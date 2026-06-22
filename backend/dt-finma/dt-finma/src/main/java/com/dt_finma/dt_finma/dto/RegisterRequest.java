package com.dt_finma.dt_finma.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data


public class RegisterRequest {
    @NotBlank(message = "the email is mandatory")
    @Email(message = "Email must be valid format")
    private String email;

    @NotBlank(message = "Password is mandatory")
    @Size (min = 8, message = "Password must have at least 8 characters")
    private String password;
}
