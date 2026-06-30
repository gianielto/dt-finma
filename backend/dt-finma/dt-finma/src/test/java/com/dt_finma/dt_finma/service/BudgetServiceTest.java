package com.dt_finma.dt_finma.service;

import com.dt_finma.dt_finma.dto.BudgetResponse;
import com.dt_finma.dt_finma.model.Budget;
import com.dt_finma.dt_finma.model.Category;
import com.dt_finma.dt_finma.model.User;
import com.dt_finma.dt_finma.repository.BudgetRepository;
import com.dt_finma.dt_finma.repository.CategoryRepository;
import com.dt_finma.dt_finma.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private BudgetService budgetService;

    private User user;
    private Category category;
    private Budget budget;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        category = new Category();
        category.setId(1L);
        category.setName("Alimentacion");
        category.setUser(user);

        budget = new Budget();
        budget.setId(1L);
        budget.setLimitAmount(new BigDecimal("4000.00"));
        budget.setCategory(category);
    }

    @Test
    @DisplayName("The alert level is OK when the spending is less than 80% of the limit." )
    void getBudgets_spendingBelow80Percent_shouldReturnOk() {
        when(budgetRepository.findByCategory_User_Id(1L)).thenReturn(List.of(budget));
        when(transactionRepository.sumExpensesByCategoryAndDateRange(
                eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("2000.00")); // 50%

        List<BudgetResponse> responses = budgetService.getBudgetsForUser(1L);

        assertEquals("OK", responses.get(0).getAlertLevel());
        assertEquals(50.0, responses.get(0).getPercentageUsed(), 0.01);
    }

    @Test
    @DisplayName("The alert level is WARNING when spending is between 80% and 99.99%.")
    void getBudgets_spendingBetween80And100Percent_shouldReturnWarning() {
        when(budgetRepository.findByCategory_User_Id(1L)).thenReturn(List.of(budget));
        when(transactionRepository.sumExpensesByCategoryAndDateRange(
                eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("3500.00")); // 87.5%

        List<BudgetResponse> responses = budgetService.getBudgetsForUser(1L);

        assertEquals("WARNING", responses.get(0).getAlertLevel());
    }

    @Test
    @DisplayName("alertLevel is EXCEEDED when the spend is exactly 100%.")
    void getBudgets_spendingAt100Percent_shouldReturnExceeded() {
        when(budgetRepository.findByCategory_User_Id(1L)).thenReturn(List.of(budget));
        when(transactionRepository.sumExpensesByCategoryAndDateRange(
                eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("4000.00")); // 100%

        List<BudgetResponse> responses = budgetService.getBudgetsForUser(1L);

        assertEquals("EXCEEDED", responses.get(0).getAlertLevel());
    }

    @Test
    @DisplayName("alertLevel is OVER_BUDGET when spending exceeds the limit")
    void getBudgets_spendingOver100Percent_shouldReturnOverBudget() {
        when(budgetRepository.findByCategory_User_Id(1L)).thenReturn(List.of(budget));
        when(transactionRepository.sumExpensesByCategoryAndDateRange(
                eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("4500.00")); // 112.5%

        List<BudgetResponse> responses = budgetService.getBudgetsForUser(1L);

        assertEquals("OVER_BUDGET", responses.get(0).getAlertLevel());
    }

    @Test
    @DisplayName("Available is correct when the expenditure is less than the limit.")
    void getBudgets_shouldCalculateAvailableAmountCorrectly() {
        when(budgetRepository.findByCategory_User_Id(1L)).thenReturn(List.of(budget));
        when(transactionRepository.sumExpensesByCategoryAndDateRange(
                eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("1500.00"));

        List<BudgetResponse> responses = budgetService.getBudgetsForUser(1L);

        assertEquals(new BigDecimal("2500.00"), responses.get(0).getAvailableAmount());
    }

    @Test
    @DisplayName("user without budget returns empty list")
    void getBudgets_userWithNoBudgets_shouldReturnEmptyList() {
        when(budgetRepository.findByCategory_User_Id(1L)).thenReturn(List.of());

        List<BudgetResponse> responses = budgetService.getBudgetsForUser(1L);

        assertEquals(0, responses.size());
    }
}