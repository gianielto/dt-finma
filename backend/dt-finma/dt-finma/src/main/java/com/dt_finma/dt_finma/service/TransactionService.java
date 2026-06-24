package com.dt_finma.dt_finma.service;

import com.dt_finma.dt_finma.dto.TransactionResponse;
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
import java.util.stream.Collectors;

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
    public TransactionResponse createTransaction(TransactionRequest request, Long userId) {

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

        return toResponse(savedTransaction);
    }

    public List<TransactionResponse> getTransactionsForUser(Long userId) {
        return transactionRepository.findByAccount_User_Id(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private void applyBalanceChange(Account account, BigDecimal amount, TransactionType type) {
        BigDecimal currentBalance = account.getBalance();

        if (type == TransactionType.INCOME) {
            account.setBalance(currentBalance.add(amount));
        } else {
            account.setBalance(currentBalance.subtract(amount));
        }
    }
    private void revertBalanceChange(Account account, BigDecimal amount, TransactionType type) {
        BigDecimal currentBalance = account.getBalance();
        if(type == TransactionType.INCOME) {
            account.setBalance(currentBalance.subtract(amount));

        }
        else {
            account.setBalance(currentBalance.add(amount));
        }
    }
    @Transactional
    public TransactionResponse updateTransaction(Long transactionId, TransactionRequest request, Long userId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction did not found with id: " + transactionId
                ));
        if (!transaction.getAccount().getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException(
                    "Count did not found with id: " + userId
            );
        }
        Account newAccount = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Count did not found with id: " + request.getAccountId()
                ));
        if (!newAccount.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException(
                    "Count did not found with id: " + userId
            );
        }

        Category newCategory = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Categoria no encontrada con id: " + request.getCategoryId()));

        if (!newCategory.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException(
                    "Categoria no encontrada con id: " + request.getCategoryId());
        }

        Account oldAccount = transaction.getAccount();
        revertBalanceChange(oldAccount, transaction.getAmount(), transaction.getType());

        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setDescription(request.getDescription());
        transaction.setDate(request.getDate());
        transaction.setAccount(newAccount);
        transaction.setCategory(newCategory);

        applyBalanceChange(newAccount, request.getAmount(), request.getType());
        if (!oldAccount.getId().equals(newAccount.getId())) {
            accountRepository.save(oldAccount);
        }
        accountRepository.save(newAccount);

        Transaction updated = transactionRepository.save(transaction);
        return toResponse(updated);
    }


    @Transactional
    public void deleteTransaction(Long transactionId, Long userId) {

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaccion no encontrada con id: " + transactionId));

        if (!transaction.getAccount().getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException(
                    "Transaccion no encontrada con id: " + transactionId);
        }

        Account account = transaction.getAccount();
        revertBalanceChange(account, transaction.getAmount(), transaction.getType());
        accountRepository.save(account);

        transactionRepository.delete(transaction);
    }
    private TransactionResponse toResponse(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getAmount(),
                t.getType(),
                t.getDescription(),
                t.getDate(),
                t.getCreatedAt(),
                t.getAccount().getId(),
                t.getCategory().getId(),
                t.getCategory().getName()
        );
    }
}