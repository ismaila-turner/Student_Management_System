package com.example.Student_Management_System.service;

import com.example.Student_Management_System.dto.LoginRequestDTO;
import com.example.Student_Management_System.dto.LoginResponseDTO;
import com.example.Student_Management_System.entity.User;
import com.example.Student_Management_System.repository.StudentRepository;
import com.example.Student_Management_System.repository.UserRepository;
import com.example.Student_Management_System.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public LoginResponseDTO login(LoginRequestDTO requestDTO) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        requestDTO.getEmail(),
                        requestDTO.getPassword()
                )
        );

        // Load user
        User user = userRepository.findByEmail(requestDTO.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + requestDTO.getEmail()));

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        // Build response - include studentId if user is a student
        LoginResponseDTO.LoginResponseDTOBuilder responseBuilder = LoginResponseDTO.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole().name());

        // If user is a student, find their student record and include studentId
        if (user.getRole().name().equals("STUDENT")) {
            studentRepository.findByUserId(user.getId())
                    .ifPresent(student -> responseBuilder.studentId(student.getStudentId()));
        }

        return responseBuilder.build();
    }

    public Boolean validateToken(String token, String email) {
        return jwtUtil.validateToken(token, email);
    }
}
