package com.dt_finma.dt_finma.service;

import com.dt_finma.dt_finma.dto.BudgetResponse;
import com.dt_finma.dt_finma.exception.ResourceNotFoundException;
import com.dt_finma.dt_finma.model.Budget;
import com.dt_finma.dt_finma.model.Category;
import com.dt_finma.dt_finma.repository.BudgetRepository;
import com.dt_finma.dt_finma.repository.CategoryRepository;
import com.dt_finma.dt_finma.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public BudgetService(
            BudgetRepository budgetRepository,
            CategoryRepository categoryRepository,
            TransactionRepository transactionRepository
    ) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
    }

    public Budget createBudget(Long categoryId, BigDecimal limitAmount, Long userId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + categoryId));

        if (!category.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException(
                    "Category not found with id: " + categoryId);
        }

        Budget budget = new Budget();
        budget.setCategory(category);
        budget.setLimitAmount(limitAmount);

        return budgetRepository.save(budget);
    }

    public List<BudgetResponse> getBudgetsForUser(Long userId) {
        List<Budget> budgets = budgetRepository.findByCategory_User_Id(userId);

        YearMonth currentMonth = YearMonth.now();
        LocalDate startDate = currentMonth.atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();

        return budgets.stream()
                .map(budget -> buildBudgetResponse(budget, startDate, endDate))
                .toList();
    }

    private BudgetResponse buildBudgetResponse(Budget budget, LocalDate startDate, LocalDate endDate) {
        BigDecimal spent = transactionRepository.sumExpensesByCategoryAndDateRange(
                budget.getCategory().getId(), startDate, endDate);

        BigDecimal limit = budget.getLimitAmount();
        BigDecimal available = limit.subtract(spent);

        double percentageUsed = spent
                .divide(limit, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();

        String alertLevel = determineAlertLevel(percentageUsed);

        return new BudgetResponse(
                budget.getId(),
                budget.getCategory().getName(),
                limit,
                spent,
                available,
                percentageUsed,
                alertLevel
        );
    }

    private String determineAlertLevel(double percentageUsed) {
        if (percentageUsed > 100) {
            return "OVER_BUDGET";
        } else if (percentageUsed == 100) {
            return "EXCEEDED";
        } else if (percentageUsed >= 80) {
            return "WARNING";
        } else {
            return "OK";
        }
    }
}