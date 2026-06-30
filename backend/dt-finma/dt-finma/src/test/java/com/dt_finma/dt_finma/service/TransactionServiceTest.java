package com.dt_finma.dt_finma.service;

import com.dt_finma.dt_finma.dto.TransactionRequest;
import com.dt_finma.dt_finma.exception.ResourceNotFoundException;
import com.dt_finma.dt_finma.model.Account;
import com.dt_finma.dt_finma.model.Category;
import com.dt_finma.dt_finma.model.Transaction;
import com.dt_finma.dt_finma.model.User;
import com.dt_finma.dt_finma.model.enums.TransactionType;
import com.dt_finma.dt_finma.repository.AccountRepository;
import com.dt_finma.dt_finma.repository.CategoryRepository;
import com.dt_finma.dt_finma.repository.SavingsGoalRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SavingsGoalRepository savingsGoalRepository;

    @InjectMocks
    private TransactionService transactionService;

    private User user;
    private Account account;
    private Category category;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@spfm.com");

        account = new Account();
        account.setId(1L);
        account.setBalance(new BigDecimal("1000.00"));
        account.setUser(user);

        category = new Category();
        category.setId(1L);
        category.setUser(user);
    }

    // ─────────────────────────────────────────
    // TESTS DE INCOME (suma al balance)
    // ─────────────────────────────────────────

    @Test
    @DisplayName("Creating an INCOME transaction adds the amount to the account balance")
    void createTransaction_income_shouldIncreaseAccountBalance() {
        // ARRANGE
        TransactionRequest request = buildRequest("500.00", TransactionType.INCOME);
        Transaction savedTransaction = new Transaction();
        savedTransaction.setAmount(request.getAmount());
        savedTransaction.setType(request.getType());
        savedTransaction.setAccount(account);
        savedTransaction.setCategory(category);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        // ACT
        transactionService.createTransaction(request, 1L);

        // ASSERT
        assertEquals(new BigDecimal("1500.00"), account.getBalance());
    }

    @Test
    @DisplayName("Creating an EXPENSE transaction subtracts the amount from the account balance")
    void createTransaction_expense_shouldDecreaseAccountBalance() {
        // ARRANGE
        TransactionRequest request = buildRequest("200.00", TransactionType.EXPENSE);
        Transaction savedTransaction = new Transaction();
        savedTransaction.setAmount(request.getAmount());
        savedTransaction.setType(request.getType());
        savedTransaction.setAccount(account);
        savedTransaction.setCategory(category);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        // ACT
        transactionService.createTransaction(request, 1L);

        // ASSERT
        assertEquals(new BigDecimal("800.00"), account.getBalance());
    }

    // ─────────────────────────────────────────
    // TESTS DE PROTECCION IDOR
    // ─────────────────────────────────────────

    @Test
    @DisplayName("Creating a transaction with another user's account throws a ResourceNotFoundException")
    void createTransaction_accountBelongsToAnotherUser_shouldThrowException() {
        // ARRANGE
        User otherUser = new User();
        otherUser.setId(99L);
        account.setUser(otherUser);

        TransactionRequest request = buildRequest("100.00", TransactionType.EXPENSE);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        // ACT + ASSERT
        assertThrows(
                ResourceNotFoundException.class,
                () -> transactionService.createTransaction(request, 1L)
        );
    }

    @Test
    @DisplayName("Creating a transaction with a non-existent account throws a ResourceNotFoundException.")
    void createTransaction_accountNotFound_shouldThrowException() {
        // ARRANGE
        TransactionRequest request = buildRequest("100.00", TransactionType.EXPENSE);
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThrows(
                ResourceNotFoundException.class,
                () -> transactionService.createTransaction(request, 1L)
        );
    }

    // ─────────────────────────────────────────
    // TESTS DE ELIMINACION Y REVERSION
    // ─────────────────────────────────────────

    @Test
    @DisplayName("Deleting an INCOME transaction reverses the balance amount.")
    void deleteTransaction_income_shouldRevertBalance() {
        // ARRANGE
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setAmount(new BigDecimal("500.00"));
        transaction.setType(TransactionType.INCOME);
        transaction.setAccount(account);

        account.setBalance(new BigDecimal("1500.00")); // simula que ya se habia sumado

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        // ACT
        transactionService.deleteTransaction(1L, 1L);

        // ASSERT — el balance debe volver al valor antes del ingreso
        assertEquals(new BigDecimal("1000.00"), account.getBalance());
        verify(transactionRepository, times(1)).delete(transaction);
    }

    @Test
    @DisplayName("Deleting the EXPENSE transaction reverts the amount to the balance.")
    void deleteTransaction_expense_shouldRevertBalance() {
        // ARRANGE
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setAmount(new BigDecimal("200.00"));
        transaction.setType(TransactionType.EXPENSE);
        transaction.setAccount(account);

        account.setBalance(new BigDecimal("800.00")); // simula que ya se habia restado

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        // ACT
        transactionService.deleteTransaction(1L, 1L);

        // ASSERT
        assertEquals(new BigDecimal("1000.00"), account.getBalance());
        verify(transactionRepository, times(1)).delete(transaction);
    }

    @Test
    @DisplayName("Deleting another user's transaction throws a ResourceNotFoundException")
    void deleteTransaction_belongsToAnotherUser_shouldThrowException() {
        // ARRANGE
        User otherUser = new User();
        otherUser.setId(99L);
        account.setUser(otherUser);

        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setAccount(account);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        // ACT + ASSERT
        assertThrows(
                ResourceNotFoundException.class,
                () -> transactionService.deleteTransaction(1L, 1L)
        );
    }


    private TransactionRequest buildRequest(String amount, TransactionType type) {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal(amount));
        request.setType(type);
        request.setDate(LocalDate.now());
        request.setAccountId(1L);
        request.setCategoryId(1L);
        return request;
    }
}