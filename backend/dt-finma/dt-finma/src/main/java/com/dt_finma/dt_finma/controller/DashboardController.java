package com.dt_finma.dt_finma.controller;

import com.dt_finma.dt_finma.dto.DashboardResponse;
import com.dt_finma.dt_finma.exception.ResourceNotFoundException;
import com.dt_finma.dt_finma.repository.UserRepository;
import com.dt_finma.dt_finma.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserRepository userRepository;

    public DashboardController(DashboardService dashboardService, UserRepository userRepository) {
        this.dashboardService = dashboardService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(dashboardService.getDashboard(userId));
    }

    private Long getCurrentUserId(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("user not found"))
                .getId();
    }
}