package com.dt_finma.dt_finma.controller;

import com.dt_finma.dt_finma.dto.AuthResponse;
import com.dt_finma.dt_finma.dto.LoginRequest;
import com.dt_finma.dt_finma.dto.RegisterRequest;
import com.dt_finma.dt_finma.model.User;
import com.dt_finma.dt_finma.security.jwtService;
import com.dt_finma.dt_finma.service.UserService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.userdetails.UserDetails;

@RestController
@RequestMapping("/api/auth")

public class AuthController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final jwtService jwtService;
    private final com.dt_finma.dt_finma.security.CustomUserDetailsService userDetailsService;

    public AuthController(
            UserService userService,
            AuthenticationManager authenticationManager,
            jwtService jwtService,
            com.dt_finma.dt_finma.security.CustomUserDetailsService userDetailsService
    ){
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request)  {
        User user = userService.registerUser(request);
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(token, user.getEmail(), user.getRole()));

    }

   @PostMapping("/login")
   public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request)  {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String token = jwtService.generateToken(userDetails);
       String role = userDetails.getAuthorities().iterator()
               .next().getAuthority().replace("ROLE_", "");
       return ResponseEntity.ok(new AuthResponse(token, request.getEmail(), role));

   }  
}
