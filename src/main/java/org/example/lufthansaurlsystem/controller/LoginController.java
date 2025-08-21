package org.example.lufthansaurlsystem.controller;

import lombok.RequiredArgsConstructor;
import org.example.lufthansaurlsystem.Entity.User;
import org.example.lufthansaurlsystem.security.JwtUtils;
import org.example.lufthansaurlsystem.dto.auth.AuthRequest;
import org.example.lufthansaurlsystem.dto.auth.AuthResponse;
import org.example.lufthansaurlsystem.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class LoginController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody AuthRequest request) {
        try {
            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                return ResponseEntity.badRequest().body("Username already exists");
            }

            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));

            userRepository.save(user);

            return ResponseEntity.ok("User registered successfully: " + request.getUsername());

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    /**
     * Authenticate a user and return a JWT token.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        try {
            // Authenticate username and password
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(),
                            authRequest.getPassword()
                    )
            );

            // Generate JWT token
            String token = jwtUtils.generateToken(authRequest.getUsername());

            return ResponseEntity.ok(new AuthResponse(token));
        } catch (AuthenticationException e) {
            // Authentication failed
            return ResponseEntity.status(401).build();
        }
    }
}
