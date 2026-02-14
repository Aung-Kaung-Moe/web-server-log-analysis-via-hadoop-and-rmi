package com.example.shop.auth;

import com.example.shop.auth.dto.*;
import com.example.shop.user.*;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwt;

    public AuthService(UserRepository users,
                       PasswordEncoder encoder,
                       AuthenticationManager authManager,
                       JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.authManager = authManager;
        this.jwt = jwt;
    }

    public void register(RegisterRequest req) {
        if (users.existsByUsername(req.username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        AppUser u = new AppUser();
        u.setUsername(req.username);
        u.setPasswordHash(encoder.encode(req.password));
        u.setRole("USER");
        users.save(u);
    }

    public TokenResponse login(LoginRequest req) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username, req.password)
        );

        AppUser u = users.findByUsername(req.username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String token = jwt.createToken(u.getUsername(), u.getRole());
        return new TokenResponse(token);
    }
}
