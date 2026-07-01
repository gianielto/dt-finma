package com.dt_finma.dt_finma.service;

import com.dt_finma.dt_finma.BaseIntegrationTest;
import com.dt_finma.dt_finma.model.*;
import com.dt_finma.dt_finma.model.enums.AccountType;
import com.dt_finma.dt_finma.model.enums.CategoryType;
import com.dt_finma.dt_finma.model.enums.TransactionType;
import com.dt_finma.dt_finma.repository.AccountRepository;
import com.dt_finma.dt_finma.repository.CategoryRepository;
import com.dt_finma.dt_finma.repository.TransactionRepository;
import com.dt_finma.dt_finma.repository.UserRepository;
import com.dt_finma.dt_finma.repository.SavingsGoalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SavingsGoalRepository savingsGoalRepository;

    private User user;
    private Account account;
    private Category categoryFood;
    private Category categoryTransport;

    @BeforeEach
    void setUp() {
        // Limpiar en orden correcto para respetar foreign keys
        transactionRepository.deleteAll();
        savingsGoalRepository.deleteAll();
        accountRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        user = new User();
        user.setEmail("integration@test.com");
        user.setPassword("hashedPassword");
        user.setRole("USER");
        user = userRepository.save(user);

        account = new Account();
        account.setName("Cuenta Principal");
        account.setType(AccountType.BANK_ACCOUNT);
        account.setBalance(new BigDecimal("5000.00"));
        account.setUser(user);
        account = accountRepository.save(account);

        categoryFood = new Category();
        categoryFood.setName("Alimentacion");
        categoryFood.setType(CategoryType.EXPENSE);
        categoryFood.setDefault(true);
        categoryFood.setUser(user);
        categoryFood = categoryRepository.save(categoryFood);

        categoryTransport = new Category();
        categoryTransport.setName("Transporte");
        categoryTransport.setType(CategoryType.EXPENSE);
        categoryTransport.setDefault(true);
        categoryTransport.setUser(user);
        categoryTransport = categoryRepository.save(categoryTransport);
    }

    // ─────────────────────────────────────────
    // sumByTypeAndDateRange
    // ─────────────────────────────────────────

    @Test
    @DisplayName("sumByTypeAndDateRange correctly sums the INCOME for the current month")
    void sumByTypeAndDateRange_income_currentMonth_shouldSumCorrectly() {
        // ARRANGE
        YearMonth current = YearMonth.now();
        saveTransaction("1000.00", TransactionType.INCOME, current.atDay(1));
        saveTransaction("500.00", TransactionType.INCOME, current.atDay(15));
        saveTransaction("200.00", TransactionType.EXPENSE, current.atDay(10)); // no debe sumar

        // ACT
        BigDecimal result = transactionRepository.sumByTypeAndDateRange(
                user.getId(),
                TransactionType.INCOME,
                current.atDay(1),
                current.atEndOfMonth()
        );

        // ASSERT
        assertEquals(new BigDecimal("1500.00"), result);
    }

    @Test
    @DisplayName("sumByTypeAndDateRange excluye transacciones de otros meses")
    void sumByTypeAndDateRange_excludesOtherMonths() {
        YearMonth current = YearMonth.now();
        YearMonth previous = current.minusMonths(1);

        saveTransaction("1000.00", TransactionType.EXPENSE, current.atDay(1));
        saveTransaction("9999.00", TransactionType.EXPENSE, previous.atDay(1));

        BigDecimal result = transactionRepository.sumByTypeAndDateRange(
                user.getId(),
                TransactionType.EXPENSE,
                current.atDay(1),
                current.atEndOfMonth()
        );

        assertEquals(new BigDecimal("1000.00"), result);
    }

    @Test
    @DisplayName("sumByTypeAndDateRange retorna 0 cuando no hay transacciones (COALESCE)")
    void sumByTypeAndDateRange_noTransactions_shouldReturnZero() {
        YearMonth current = YearMonth.now();

        BigDecimal result = transactionRepository.sumByTypeAndDateRange(
                user.getId(),
                TransactionType.EXPENSE,
                current.atDay(1),
                current.atEndOfMonth()
        );


        assertNotNull(result);
        assertEquals(0, result.compareTo(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("sumByTypeAndDateRange excluye transacciones de otro usuario")
    void sumByTypeAndDateRange_excludesOtherUsersTransactions() {

        User otherUser = new User();
        otherUser.setEmail("other@test.com");
        otherUser.setPassword("hashedPassword");
        otherUser.setRole("USER");
        otherUser = userRepository.save(otherUser);

        Account otherAccount = new Account();
        otherAccount.setName("Otra Cuenta");
        otherAccount.setType(AccountType.CASH);
        otherAccount.setBalance(new BigDecimal("1000.00"));
        otherAccount.setUser(otherUser);
        otherAccount = accountRepository.save(otherAccount);

        YearMonth current = YearMonth.now();
        saveTransaction("500.00", TransactionType.EXPENSE, current.atDay(1));   // usuario 1
        saveTransactionForAccount("9000.00", TransactionType.EXPENSE,           // usuario 2
                current.atDay(1), otherAccount);

        BigDecimal result = transactionRepository.sumByTypeAndDateRange(
                user.getId(),
                TransactionType.EXPENSE,
                current.atDay(1),
                current.atEndOfMonth()
        );

        // Solo debe sumar la del usuario 1, no la del usuario 2
        assertEquals(new BigDecimal("500.00"), result);
    }

    // ─────────────────────────────────────────
    // sumExpensesGroupedByCategory
    // ─────────────────────────────────────────

    @Test
    @DisplayName("sumExpensesGroupedByCategory agrupa y ordena correctamente por categoria")
    void sumExpensesGroupedByCategory_shouldGroupAndOrderCorrectly() {
        YearMonth current = YearMonth.now();

        // Alimentacion: 300 + 200 = 500
        saveTransactionWithCategory("300.00", TransactionType.EXPENSE, current.atDay(1), categoryFood);
        saveTransactionWithCategory("200.00", TransactionType.EXPENSE, current.atDay(5), categoryFood);

        // Transporte: 150
        saveTransactionWithCategory("150.00", TransactionType.EXPENSE, current.atDay(3), categoryTransport);

        var results = transactionRepository.sumExpensesGroupedByCategory(
                user.getId(), current.atDay(1), current.atEndOfMonth());

        assertEquals(2, results.size());

        // Debe ordenar de mayor a menor: Alimentacion (500) primero, Transporte (150) segundo
        assertEquals("Alimentacion", results.get(0).categoryName());
        assertEquals(new BigDecimal("500.00"), results.get(0).totalSpend());
        assertEquals("Transporte", results.get(1).categoryName());
        assertEquals(new BigDecimal("150.00"), results.get(1).totalSpend());
    }

    @Test
    @DisplayName("sumExpensesGroupedByCategory no incluye transacciones INCOME")
    void sumExpensesGroupedByCategory_excludesIncome() {
        YearMonth current = YearMonth.now();

        Category categorySalary = new Category();
        categorySalary.setName("Salario");
        categorySalary.setType(CategoryType.INCOME);
        categorySalary.setDefault(true);
        categorySalary.setUser(user);
        categorySalary = categoryRepository.save(categorySalary);

        saveTransactionWithCategory("500.00", TransactionType.EXPENSE, current.atDay(1), categoryFood);
        saveTransactionWithCategory("3000.00", TransactionType.INCOME, current.atDay(1), categorySalary);

        var results = transactionRepository.sumExpensesGroupedByCategory(
                user.getId(), current.atDay(1), current.atEndOfMonth());

        // Solo debe aparecer Alimentacion, no Salario
        assertEquals(1, results.size());
        assertEquals("Alimentacion", results.get(0).categoryName());
    }

    // ─────────────────────────────────────────
    // sumContributionsBySavingsGoal
    // ─────────────────────────────────────────

    @Test
    @DisplayName("sumContributionsBySavingsGoal calcula progreso neto correctamente con INCOME y EXPENSE")
    void sumContributionsBySavingsGoal_shouldCalculateNetProgress() {
        SavingsGoal goal = new SavingsGoal();
        goal.setName("Laptop");
        goal.setTargetAmount(new BigDecimal("20000.00"));
        goal.setTargetDate(LocalDate.now().plusMonths(6));
        goal.setUser(user);


        goal = savingsGoalRepository.save(goal);

        // Aportes: +3000 y +2000 = 5000
        saveTransactionWithGoal("3000.00", TransactionType.INCOME, goal);
        saveTransactionWithGoal("2000.00", TransactionType.INCOME, goal);
        // Retiro: -500 (EXPENSE ligado a la meta)
        saveTransactionWithGoal("500.00", TransactionType.EXPENSE, goal);

        BigDecimal progress = transactionRepository.sumContributionsBySavingsGoal(goal.getId());

        // Progreso neto: 3000 + 2000 - 500 = 4500
        assertEquals(new BigDecimal("4500.00"), progress);
    }

    // ─────────────────────────────────────────
    // HELPERS PRIVADOS
    // ─────────────────────────────────────────

    private void saveTransaction(String amount, TransactionType type, LocalDate date) {
        saveTransactionWithCategory(amount, type, date, categoryFood);
    }

    private void saveTransactionWithCategory(String amount, TransactionType type,
                                             LocalDate date, Category category) {
        Transaction t = new Transaction();
        t.setAmount(new BigDecimal(amount));
        t.setType(type);
        t.setDate(date);
        t.setAccount(account);
        t.setCategory(category);
        transactionRepository.save(t);
    }

    private void saveTransactionForAccount(String amount, TransactionType type,
                                           LocalDate date, Account targetAccount) {
        Transaction t = new Transaction();
        t.setAmount(new BigDecimal(amount));
        t.setType(type);
        t.setDate(date);
        t.setAccount(targetAccount);
        t.setCategory(categoryFood);
        transactionRepository.save(t);
    }

    private void saveTransactionWithGoal(String amount, TransactionType type, SavingsGoal goal) {
        Transaction t = new Transaction();
        t.setAmount(new BigDecimal(amount));
        t.setType(type);
        t.setDate(LocalDate.now());
        t.setAccount(account);
        t.setCategory(categoryFood);
        t.setSavingsGoal(goal);
        transactionRepository.save(t);
    }
}