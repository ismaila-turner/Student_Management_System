package com.example.Student_Management_System.config;

import com.example.Student_Management_System.entity.User;
import com.example.Student_Management_System.enums.Role;
import com.example.Student_Management_System.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        // Check if admin already exists
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            log.info("Creating admin user...");

            // Hash password
            String hashedPassword = passwordEncoder.encode(adminPassword);

            // Create admin user entity
            User admin = User.builder()
                    .email(adminEmail)
                    .password(hashedPassword)
                    .role(Role.ADMIN)
                    .build();

            // Save admin
            userRepository.save(admin);
            log.info("Admin user created successfully with email: {}", adminEmail);
        } else {
            log.info("Admin user already exists with email: {}", adminEmail);
        }
    }
}
