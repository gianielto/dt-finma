package com.dt_finma.dt_finma.controller;

import com.dt_finma.dt_finma.exception.ResourceNotFoundException;
import com.dt_finma.dt_finma.model.Account;
import com.dt_finma.dt_finma.repository.AccountRepository;
import com.dt_finma.dt_finma.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountController(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<Account>> getMyAccounts(Authentication authentication ) {
        Long userId= getCurrentUserId(authentication);
        return ResponseEntity.ok(accountRepository.findByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(
            @Valid @RequestBody Account account,
            Authentication authentication
    ) {
        var user = userRepository.findById(getCurrentUserId(authentication))
                .orElseThrow(()->new ResourceNotFoundException("user not found"));
        account.setUser(user);
        Account savedAccount = accountRepository.save(account);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAccount);
    }

    private Long getCurrentUserId(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(()-> new ResourceNotFoundException("user not found"))
                .getId();
    }
}
