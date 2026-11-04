package com.attendance.security;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserAccountRepository accounts;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;

    public AuthController(UserAccountRepository accounts, PasswordEncoder passwordEncoder,
                          AuthenticationManager authenticationManager, JwtTokenService jwtTokenService) {
        this.accounts = accounts;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody Credentials credentials) {
        if (accounts.existsByUsername(credentials.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "That username is already registered.");
        }
        accounts.save(new UserAccount(credentials.username(), passwordEncoder.encode(credentials.password()), AccountRole.STUDENT));
        return new AuthResponse(credentials.username(), "Account created as STUDENT. An admin can link it to a student record.", null, null, null, AccountRole.STUDENT);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody Credentials credentials) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(credentials.username(), credentials.password()));
            AccountRole role = accounts.findByUsername(authentication.getName()).orElseThrow().getRole();
            JwtTokenService.Token token = jwtTokenService.create(authentication.getName(), role.name());
            return new AuthResponse(authentication.getName(), "Login successful.", token.value(), "Bearer",
                    JwtTokenService.EXPIRY_SECONDS, role);
        } catch (BadCredentialsException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username or password is incorrect.");
        }
    }

    @GetMapping("/me")
    public AuthResponse me(Authentication authentication) {
        String authority = authentication.getAuthorities().stream().findFirst()
                .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "This token has no role. Please log in again."));
        return new AuthResponse(authentication.getName(), "You are logged in.", null, null, null,
                AccountRole.valueOf(authority.replace("ROLE_", "")));
    }

    public record Credentials(
            @NotBlank @Size(max = 50) @Pattern(regexp = "[A-Za-z0-9_.-]+", message = "Use letters, numbers, dots, underscores or hyphens.") String username,
            @NotBlank @Size(min = 8, max = 72) String password) { }

    public record AuthResponse(String username, String message, String accessToken,
                               String tokenType, Long expiresIn, AccountRole role) { }
}
