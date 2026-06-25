package com.dt_finma.dt_finma.controller;

import com.dt_finma.dt_finma.dto.BudgetRequest;
import com.dt_finma.dt_finma.dto.BudgetResponse;
import com.dt_finma.dt_finma.exception.ResourceNotFoundException;
import com.dt_finma.dt_finma.model.Budget;
import com.dt_finma.dt_finma.repository.UserRepository;
import com.dt_finma.dt_finma.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;
    private final UserRepository userRepository;

    public BudgetController(BudgetService budgetService, UserRepository userRepository) {
        this.budgetService = budgetService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<BudgetResponse>> getMyBudgets(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(budgetService.getBudgetsForUser(userId));
    }

    @PostMapping
    public ResponseEntity<Budget> createBudget(
            @Valid @RequestBody BudgetRequest request,
            Authentication authentication
    ) {
        Long userId = getCurrentUserId(authentication);
        Budget budget = budgetService.createBudget(request.getCategoryId(), request.getLimitAmount(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(budget);
    }

    private Long getCurrentUserId(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getId();
    }
}