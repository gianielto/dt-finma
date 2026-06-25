package com.dt_finma.dt_finma.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BudgetRequest {

    @NotNull(message = "Category is mandatory")
    private Long categoryId;

    @NotNull(message = "Amount limit is required")
    @Positive(message = "The limit amount must be positive.")
    private BigDecimal limitAmount;
}