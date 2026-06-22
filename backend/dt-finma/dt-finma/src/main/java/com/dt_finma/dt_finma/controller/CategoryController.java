package com.dt_finma.dt_finma.controller;

import com.dt_finma.dt_finma.exception.ResourceNotFoundException;
import com.dt_finma.dt_finma.model.Category;
import com.dt_finma.dt_finma.repository.CategoryRepository;
import com.dt_finma.dt_finma.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryController(CategoryRepository categoryRepository, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<Category>> getMyCategories(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(categoryRepository.findByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<Category> createCategory(
            @Valid @RequestBody Category category,
            Authentication authentication
    ) {
        var user = userRepository.findById(getCurrentUserId(authentication))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        category.setDefault(false);
        category.setUser(user);
        Category savedCategory = categoryRepository.save(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCategory);
    }

    private Long getCurrentUserId(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getId();
    }

}
