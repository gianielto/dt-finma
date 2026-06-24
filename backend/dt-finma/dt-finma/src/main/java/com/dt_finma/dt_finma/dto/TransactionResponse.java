package com.dt_finma.dt_finma.dto;

import com.dt_finma.dt_finma.model.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private BigDecimal amount;
    private TransactionType type;
    private String description;
    private LocalDate date;
    private LocalDateTime createdAt;
    private Long accountId;
    private Long categoryId;
    private String categoryName;
}