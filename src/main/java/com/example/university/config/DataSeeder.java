package com.example.university.config;

import com.example.university.model.Role;
import com.example.university.model.UserAccount;
import com.example.university.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        if (!repo.existsByUsername("admin")) {
            UserAccount admin = new UserAccount();
            admin.setUsername("admin");
            admin.setPassword(encoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setFullName("System Admin");
            admin.setEmail("admin@university.edu");
            repo.save(admin);
        }
    }
}