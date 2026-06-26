package com.dt_finma.dt_finma.service;

import com.dt_finma.dt_finma.dto.CategorySpending;
import com.dt_finma.dt_finma.dto.DashboardResponse;
import com.dt_finma.dt_finma.model.enums.TransactionType;
import com.dt_finma.dt_finma.repository.AccountRepository;
import com.dt_finma.dt_finma.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class DashboardService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public DashboardService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public DashboardResponse getDashboard(Long userId) {

        BigDecimal totalBalance = accountRepository.findByUserId(userId).stream()
                .map(account -> account.getBalance())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        YearMonth currentMonth = YearMonth.now();
        LocalDate currentStart = currentMonth.atDay(1);
        LocalDate currentEnd = currentMonth.atEndOfMonth();

        YearMonth previousMonth = currentMonth.minusMonths(1);
        LocalDate previousStart = previousMonth.atDay(1);
        LocalDate previousEnd = previousMonth.atEndOfMonth();

        BigDecimal currentIncome = transactionRepository.sumByTypeAndDateRange(
                userId, TransactionType.INCOME, currentStart, currentEnd);

        BigDecimal currentExpenses = transactionRepository.sumByTypeAndDateRange(
                userId, TransactionType.EXPENSE, currentStart, currentEnd);

        BigDecimal previousExpenses = transactionRepository.sumByTypeAndDateRange(
                userId, TransactionType.EXPENSE, previousStart, previousEnd);

        BigDecimal currentSavings = currentIncome.subtract(currentExpenses);

        double variationPercentage = calculateVariationPercentage(previousExpenses, currentExpenses);

        List<CategorySpending> expensesByCategory = transactionRepository.sumExpensesGroupedByCategory(
                userId, currentStart, currentEnd);

        return new DashboardResponse(
                totalBalance,
                currentIncome,
                currentExpenses,
                currentSavings,
                previousExpenses,
                variationPercentage,
                expensesByCategory
        );
    }

    private double calculateVariationPercentage(BigDecimal previous, BigDecimal current) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) == 0 ? 0.0 : 100.0;
        }

        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }
}