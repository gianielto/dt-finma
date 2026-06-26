package com.dt_finma.dt_finma.dto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class DashboardResponse {
    private BigDecimal totalBalance;
    private BigDecimal currentMonthIncome;
    private BigDecimal currentMonthExpenses;
    private BigDecimal currentMonthSavings;
    private BigDecimal previousMonthExpenses;
    private double expenseVariationPercentage;
    private List<CategorySpending> expensesByCategory;
}
