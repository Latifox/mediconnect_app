package com.mediconnect.backend.controller;

import com.mediconnect.backend.dto.AuthResponse;
import com.mediconnect.backend.dto.LoginRequest;
import com.mediconnect.backend.dto.RegisterRequest;
import com.mediconnect.backend.model.User;
import com.mediconnect.backend.security.JwtService;
import com.mediconnect.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            User user = (User) authentication.getPrincipal();
            String jwt = jwtService.generateToken(user);

            AuthResponse response = new AuthResponse(
                    jwt,
                    user.getId(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getRole(),
                    user.getSpeciality()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body("Error: Invalid email or password!");
        }
    }

    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Register a new user (patient or doctor)")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            System.out.println("Registration request received for: " + registerRequest.getEmail());
            
            // Create user
            User user = userService.createUser(registerRequest);
            System.out.println("User created successfully with ID: " + user.getId());
            
            // Generate JWT token
            String jwt = jwtService.generateToken(user);
            System.out.println("JWT token generated successfully");

            // Create response
            AuthResponse response = new AuthResponse(
                    jwt,
                    user.getId(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getRole(),
                    user.getSpeciality()
            );
            System.out.println("AuthResponse created successfully");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.err.println("Registration runtime error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body("Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Registration unexpected error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body("Error: Registration failed - " + e.getMessage());
        }
    }

    @GetMapping("/validate")
    @Operation(summary = "Validate token", description = "Validate JWT token and return user info")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                String jwt = token.substring(7);
                String email = jwtService.extractUsername(jwt);
                
                User user = userService.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("User not found"));

                if (jwtService.isTokenValid(jwt, user)) {
                    AuthResponse response = new AuthResponse(
                            jwt,
                            user.getId(),
                            user.getEmail(),
                            user.getFirstName(),
                            user.getLastName(),
                            user.getRole(),
                            user.getSpeciality()
                    );
                    return ResponseEntity.ok(response);
                }
            }
            return ResponseEntity.badRequest().body("Invalid token");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
} 