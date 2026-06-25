package com.dt_finma.dt_finma.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SavingsGoalRequest {

    @NotBlank(message = "El nombre de la meta es obligatorio")
    private String name;

    @NotNull(message = "El monto objetivo es obligatorio")
    @Positive(message = "El monto objetivo debe ser positivo")
    private BigDecimal targetAmount;

    @NotNull(message = "La fecha objetivo es obligatoria")
    @Future(message = "La fecha objetivo debe ser en el futuro")
    private LocalDate targetDate;
}