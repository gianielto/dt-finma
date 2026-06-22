package com.dt_finma.dt_finma.service;

import com.dt_finma.dt_finma.dto.RegisterRequest;
import com.dt_finma.dt_finma.exception.EmailAlreadyExistsException;
import com.dt_finma.dt_finma.model.Category;
import com.dt_finma.dt_finma.model.User;
import com.dt_finma.dt_finma.repository.CategoryRepository;
import com.dt_finma.dt_finma.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;
    private final DefaultCategoryProvider defaultCategoryProvider;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            CategoryRepository categoryRepository,
            DefaultCategoryProvider defaultCategoryProvider
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.categoryRepository = categoryRepository;
        this.defaultCategoryProvider = defaultCategoryProvider;
    }

    @Transactional
    public User registerUser(RegisterRequest request) {
        if(userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("email already exists " + request.getEmail());
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");

        User savedUser = userRepository.save(user);
        List<Category> defaultCategories = defaultCategoryProvider.buildDefaultCategories(savedUser);
        categoryRepository.saveAll(defaultCategories);


        return savedUser;
    }
}
