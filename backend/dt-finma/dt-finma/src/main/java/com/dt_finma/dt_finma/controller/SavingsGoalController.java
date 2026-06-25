package com.dt_finma.dt_finma.controller;

import com.dt_finma.dt_finma.dto.SavingsGoalRequest;
import com.dt_finma.dt_finma.dto.SavingsGoalResponse;
import com.dt_finma.dt_finma.exception.ResourceNotFoundException;
import com.dt_finma.dt_finma.model.SavingsGoal;
import com.dt_finma.dt_finma.repository.UserRepository;
import com.dt_finma.dt_finma.service.SavingsGoalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/savings-goals")
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;
    private final UserRepository userRepository;

    public SavingsGoalController(SavingsGoalService savingsGoalService, UserRepository userRepository) {
        this.savingsGoalService = savingsGoalService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<SavingsGoalResponse>> getMyGoals(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(savingsGoalService.getGoalsForUser(userId));
    }

    @PostMapping
    public ResponseEntity<SavingsGoal> createGoal(
            @Valid @RequestBody SavingsGoalRequest request,
            Authentication authentication
    ) {
        Long userId = getCurrentUserId(authentication);
        SavingsGoal goal = savingsGoalService.createSavingsGoal(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(goal);
    }

    private Long getCurrentUserId(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"))
                .getId();
    }
}