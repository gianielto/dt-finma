package com.dt_finma.dt_finma.service;

import com.dt_finma.dt_finma.dto.DashboardResponse;
import com.dt_finma.dt_finma.model.enums.TransactionType;
import com.dt_finma.dt_finma.repository.AccountRepository;
import com.dt_finma.dt_finma.repository.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    @DisplayName("Usuario nuevo sin datos retorna dashboard en cero sin lanzar excepcion")
    void getDashboard_newUserWithNoData_shouldReturnZeroesWithoutException() {
        // ARRANGE
        when(accountRepository.findByUserId(1L)).thenReturn(List.of());
        when(transactionRepository.sumByTypeAndDateRange(
                eq(1L), eq(TransactionType.INCOME), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(BigDecimal.ZERO);
        when(transactionRepository.sumByTypeAndDateRange(
                eq(1L), eq(TransactionType.EXPENSE), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(BigDecimal.ZERO);
        when(transactionRepository.sumExpensesGroupedByCategory(
                eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());

        // ACT
        DashboardResponse response = dashboardService.getDashboard(1L);

        // ASSERT
        assertNotNull(response);
        assertEquals(BigDecimal.ZERO, response.getTotalBalance());
        assertEquals(BigDecimal.ZERO, response.getCurrentMonthIncome());
        assertEquals(BigDecimal.ZERO, response.getCurrentMonthExpenses());
        assertEquals(0.0, response.getExpenseVariationPercentage());
        assertTrue(response.getExpensesByCategory().isEmpty());
    }

    @Test
    @DisplayName("Variacion positiva cuando los gastos del mes actual superan el mes anterior")
    void getDashboard_higherCurrentExpenses_shouldReturnPositiveVariation() {
        // ARRANGE
        when(accountRepository.findByUserId(1L)).thenReturn(List.of());
        when(transactionRepository.sumByTypeAndDateRange(
                eq(1L), eq(TransactionType.INCOME), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(BigDecimal.ZERO);

        // Gastos del mes anterior vs mes actual
        when(transactionRepository.sumByTypeAndDateRange(
                eq(1L), eq(TransactionType.EXPENSE), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("1000.00"))  // primer call: mes actual
                .thenReturn(new BigDecimal("500.00"));  // segundo call: mes anterior

        when(transactionRepository.sumExpensesGroupedByCategory(
                eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());

        // ACT
        DashboardResponse response = dashboardService.getDashboard(1L);

        // ASSERT — gastaste el doble que el mes pasado: 100% de variacion positiva
        assertEquals(100.0, response.getExpenseVariationPercentage(), 0.01);
    }

    @Test
    @DisplayName("Variacion negativa cuando los gastos actuales son menores al mes anterior")
    void getDashboard_lowerCurrentExpenses_shouldReturnNegativeVariation() {
        when(accountRepository.findByUserId(1L)).thenReturn(List.of());
        when(transactionRepository.sumByTypeAndDateRange(
                eq(1L), eq(TransactionType.INCOME), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(BigDecimal.ZERO);
        when(transactionRepository.sumByTypeAndDateRange(
                eq(1L), eq(TransactionType.EXPENSE), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("500.00"))   // mes actual
                .thenReturn(new BigDecimal("1000.00")); // mes anterior

        when(transactionRepository.sumExpensesGroupedByCategory(
                eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());

        DashboardResponse response = dashboardService.getDashboard(1L);

        // Gastaste la mitad que el mes pasado: -50% de variacion
        assertEquals(-50.0, response.getExpenseVariationPercentage(), 0.01);
    }
}