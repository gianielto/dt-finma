package com.dt_finma.dt_finma.controller;

import com.dt_finma.dt_finma.dto.TransactionRequest;
import com.dt_finma.dt_finma.exception.ResourceNotFoundException;
import com.dt_finma.dt_finma.model.Transaction;
import com.dt_finma.dt_finma.repository.UserRepository;
import com.dt_finma.dt_finma.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final UserRepository userRepository;

    public TransactionController (TransactionService transactionService, UserRepository userRepository) {
        this.transactionService = transactionService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getMyTransactions(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(transactionService.getTransactionsForUser(userId));
    }

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(
            @Valid @RequestBody TransactionRequest request,
            Authentication authentication
    ) {
        Long userId = getCurrentUserId(authentication);
        Transaction transaction = transactionService.createTransaction(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    private Long getCurrentUserId(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"))
                .getId();
    }
}