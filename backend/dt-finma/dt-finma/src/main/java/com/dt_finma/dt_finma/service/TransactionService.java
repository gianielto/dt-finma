package com.dt_finma.dt_finma.service;

import com.dt_finma.dt_finma.dto.TransactionRequest;
import com.dt_finma.dt_finma.exception.ResourceNotFoundException;
import com.dt_finma.dt_finma.model.Account;
import com.dt_finma.dt_finma.model.Category;
import com.dt_finma.dt_finma.model.Transaction;
import com.dt_finma.dt_finma.model.enums.TransactionType;
import com.dt_finma.dt_finma.repository.AccountRepository;
import com.dt_finma.dt_finma.repository.CategoryRepository;
import com.dt_finma.dt_finma.repository.TransactionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    public TransactionService(
            TransactionRepository transactionRepository,
            AccountRepository accountRepository,
            CategoryRepository categoryRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public Transaction createTransaction(TransactionRequest request, Long userId) {

        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cuenta no encontrada con id: " + request.getAccountId()));

        if (!account.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException(
                    "Cuenta no encontrada con id: " + request.getAccountId());
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Categoria no encontrada con id: " + request.getCategoryId()));

        if (!category.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException(
                    "Categoria no encontrada con id: " + request.getCategoryId());
        }

        Transaction transaction = new Transaction();
        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setDescription(request.getDescription());
        transaction.setDate(request.getDate());
        transaction.setAccount(account);
        transaction.setCategory(category);

        Transaction savedTransaction = transactionRepository.save(transaction);

        applyBalanceChange(account, request.getAmount(), request.getType());
        accountRepository.save(account);

        return savedTransaction;
    }

    public List<Transaction> getTransactionsForUser(Long userId) {
        return transactionRepository.findByAccount_User_Id(userId);
    }

    private void applyBalanceChange(Account account, BigDecimal amount, TransactionType type) {
        BigDecimal currentBalance = account.getBalance();

        if (type == TransactionType.INCOME) {
            account.setBalance(currentBalance.add(amount));
        } else {
            account.setBalance(currentBalance.subtract(amount));
        }
    }
}