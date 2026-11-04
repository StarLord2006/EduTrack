package com.attendance.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminAccountBootstrap implements ApplicationRunner {

    private final UserAccountRepository accounts;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;

    public AdminAccountBootstrap(UserAccountRepository accounts, PasswordEncoder passwordEncoder,
                                 @Value("${app.security.admin.username:}") String username,
                                 @Value("${app.security.admin.password:}") String password) {
        this.accounts = accounts;
        this.passwordEncoder = passwordEncoder;
        this.username = username.trim();
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (username.isBlank()) return;
        if (!username.matches("[A-Za-z0-9_.-]{1,50}")) {
            throw new IllegalStateException("ADMIN_USERNAME must use letters, numbers, dots, underscores or hyphens (max 50 characters).");
        }
        UserAccount account = accounts.findByUsername(username).orElse(null);
        if (account == null) {
            if (password.length() < 8 || password.length() > 72) {
                throw new IllegalStateException("Set ADMIN_PASSWORD to 8–72 characters to create the configured admin account.");
            }
            account = new UserAccount(username, passwordEncoder.encode(password), AccountRole.ADMIN);
        } else {
            account.setRole(AccountRole.ADMIN);
        }
        accounts.save(account);
    }
}
