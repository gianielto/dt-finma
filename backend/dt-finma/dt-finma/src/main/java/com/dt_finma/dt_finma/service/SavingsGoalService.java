package com.dt_finma.dt_finma.service;

import com.dt_finma.dt_finma.dto.SavingsGoalRequest;
import com.dt_finma.dt_finma.dto.SavingsGoalResponse;
import com.dt_finma.dt_finma.exception.ResourceNotFoundException;
import com.dt_finma.dt_finma.model.SavingsGoal;
import com.dt_finma.dt_finma.model.User;
import com.dt_finma.dt_finma.repository.SavingsGoalRepository;
import com.dt_finma.dt_finma.repository.TransactionRepository;
import com.dt_finma.dt_finma.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public SavingsGoalService(
            SavingsGoalRepository savingsGoalRepository,
            UserRepository userRepository,
            TransactionRepository transactionRepository
    ) {
        this.savingsGoalRepository = savingsGoalRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    public SavingsGoal createSavingsGoal(SavingsGoalRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        SavingsGoal goal = new SavingsGoal();
        goal.setName(request.getName());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setTargetDate(request.getTargetDate());
        goal.setUser(user);

        return savingsGoalRepository.save(goal);
    }

    public List<SavingsGoalResponse> getGoalsForUser(Long userId) {
        List<SavingsGoal> goals = savingsGoalRepository.findByUserId(userId);
        return goals.stream()
                .map(this::buildGoalResponse)
                .toList();
    }

    private SavingsGoalResponse buildGoalResponse(SavingsGoal goal) {
        BigDecimal current = transactionRepository.sumContributionsBySavingsGoal(goal.getId());
        BigDecimal target = goal.getTargetAmount();
        BigDecimal remaining = target.subtract(current);

        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            remaining = BigDecimal.ZERO;
        }

        double percentageCompleted = current
                .divide(target, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();

        boolean achieved = current.compareTo(target) >= 0;

        return new SavingsGoalResponse(
                goal.getId(),
                goal.getName(),
                target,
                current,
                remaining,
                percentageCompleted,
                goal.getTargetDate(),
                achieved
        );
    }
}