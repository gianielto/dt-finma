package com.dt_finma.dt_finma.dto;
import java.math.BigDecimal;


public record CategorySpending (String categoryName, BigDecimal totalSpend) {
}
