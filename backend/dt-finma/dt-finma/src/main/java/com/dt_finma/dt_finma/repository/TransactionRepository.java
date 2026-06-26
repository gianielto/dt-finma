package com.dt_finma.dt_finma.repository;

import com.dt_finma.dt_finma.dto.CategorySpending;
import com.dt_finma.dt_finma.model.Transaction;
import com.dt_finma.dt_finma.model.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountId(Long accountId);

    List<Transaction> findByAccount_User_Id(Long userId);
    @Query("""
            SELECT COALESCE(SUM(t.amount), 0)
            FROM Transaction t
            WHERE t.category.id = :categoryId
            AND t.type = 'EXPENSE'
            AND t.date BETWEEN :startDate AND :endDate
            """)
    BigDecimal sumExpensesByCategoryAndDateRange(
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    @Query("""
        SELECT COALESCE(SUM(
            CASE WHEN t.type = 'INCOME' THEN t.amount ELSE -t.amount END
        ), 0)
        FROM Transaction t
        WHERE t.savingsGoal.id = :savingsGoalId
        """)
    BigDecimal sumContributionsBySavingsGoal(@Param("savingsGoalId") Long savingsGoalId);

    @Query("""
        SELECT COALESCE (SUM(t.amount),0)
        FROM Transaction t
        WHERE t.account.user.id =:userId
        AND t.type =:type
        and  t.date BETWEEN :startDate AND :endDate
    """)
    BigDecimal sumByTypeAndDateRange(
            @Param("userId") Long userId,
            @Param("type")TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
            );

    @Query("""
        SELECT new com.dt_finma.dt_finma.dto.CategorySpending(
            t.category.name, SUM(t.amount)
            )
        FROM Transaction t
        WHERE t.account.user.id = :userId
        AND t.type = 'EXPENSE'
        AND t.date BETWEEN :startDate AND :endDate
        GROUP BY t.category.name
        ORDER BY SUM(t.amount) DESC
    """)
    List<CategorySpending> sumExpensesGroupedByCategory(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}