package com.example.Student_Management_System.service;

import com.example.Student_Management_System.dto.StudentCreateWithPasswordDTO;
import com.example.Student_Management_System.dto.StudentRequestDTO;
import com.example.Student_Management_System.dto.StudentResponseDTO;
import com.example.Student_Management_System.entity.Student;
import com.example.Student_Management_System.entity.User;
import com.example.Student_Management_System.enums.Role;
import com.example.Student_Management_System.exception.DuplicateResourceException;
import com.example.Student_Management_System.exception.ResourceNotFoundException;
import com.example.Student_Management_System.repository.StudentRepository;
import com.example.Student_Management_System.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentResponseDTO createStudent(StudentRequestDTO requestDTO) {
        // Check if email already exists
        if (studentRepository.existsByEmail(requestDTO.getEmail())) {
            throw new DuplicateResourceException("Student", "email", requestDTO.getEmail());
        }

        // Generate student ID
        String studentId = generateStudentId();

        // Create student entity (without password - this method is for backward compatibility)
        Student student = Student.builder()
                .firstName(requestDTO.getFirstName())
                .lastName(requestDTO.getLastName())
                .email(requestDTO.getEmail())
                .studentId(studentId)
                .userId(null) // No user account for students without password
                .build();

        // Save student
        Student savedStudent = studentRepository.save(student);

        // Convert to DTO and return
        return convertToDTO(savedStudent);
    }

    public StudentResponseDTO createStudentWithPassword(StudentCreateWithPasswordDTO requestDTO) {
        // Check if email already exists in User table
        if (userRepository.existsByEmail(requestDTO.getEmail())) {
            throw new DuplicateResourceException("User", "email", requestDTO.getEmail());
        }

        // Check if email already exists in Student table
        if (studentRepository.existsByEmail(requestDTO.getEmail())) {
            throw new DuplicateResourceException("Student", "email", requestDTO.getEmail());
        }

        // Generate student ID
        String studentId = generateStudentId();

        // Hash password
        String hashedPassword = passwordEncoder.encode(requestDTO.getPassword());

        // Create User account first (with STUDENT role)
        User user = User.builder()
                .email(requestDTO.getEmail())
                .password(hashedPassword)
                .role(requestDTO.getRole() != null ? requestDTO.getRole() : Role.STUDENT)
                .build();

        // Save user
        User savedUser = userRepository.save(user);

        // Create student entity linked to User
        Student student = Student.builder()
                .firstName(requestDTO.getFirstName())
                .lastName(requestDTO.getLastName())
                .email(requestDTO.getEmail())
                .studentId(studentId)
                .userId(savedUser.getId())
                .build();

        // Save student
        Student savedStudent = studentRepository.save(student);

        // Convert to DTO and return
        return convertToDTO(savedStudent);
    }

    @Transactional(readOnly = true)
    public StudentResponseDTO getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));
        return convertToDTO(student);
    }

    @Transactional(readOnly = true)
    public List<StudentResponseDTO> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public StudentResponseDTO updateStudent(Long id, StudentRequestDTO requestDTO) {
        // Find existing student
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));

        // Check if email is being changed and if new email already exists
        if (!student.getEmail().equals(requestDTO.getEmail()) && 
            studentRepository.existsByEmail(requestDTO.getEmail())) {
            throw new DuplicateResourceException("Student", "email", requestDTO.getEmail());
        }

        // Update student fields
        student.setFirstName(requestDTO.getFirstName());
        student.setLastName(requestDTO.getLastName());
        student.setEmail(requestDTO.getEmail());
        // Note: studentId is not updated as it's auto-generated and should remain constant

        // Save updated student
        Student updatedStudent = studentRepository.save(student);

        // Convert to DTO and return
        return convertToDTO(updatedStudent);
    }

    public void deleteStudent(Long id) {
        // Check if student exists
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Student", "id", id);
        }

        // Delete student
        studentRepository.deleteById(id);
    }

    /**
     * Checks if the current authenticated user owns the student ID
     * Used for @PreAuthorize SpEL expressions
     */
    @Transactional(readOnly = true)
    public Boolean isOwnId(Long id) {
        try {
            org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return false;
            }

            String email = authentication.getName();
            Student student = studentRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));

            return student.getEmail().equals(email);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if the current authenticated user owns the student ID (by string code)
     * Used for @PreAuthorize SpEL expressions with string studentId
     */
    @Transactional(readOnly = true)
    public Boolean isOwnStudentId(String studentId) {
        try {
            org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return false;
            }

            String email = authentication.getName();
            Student student = studentRepository.findByStudentId(studentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student", "studentId", studentId));

            return student.getEmail().equals(email);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Generates the next student ID in format STU001, STU002, etc.
     * @return the next student ID
     */
    private String generateStudentId() {
        Optional<Student> lastStudent = studentRepository.findTopByOrderByStudentIdDesc();
        
        if (lastStudent.isEmpty()) {
            // First student
            return "STU001";
        }
        
        String lastStudentId = lastStudent.get().getStudentId();
        
        // Extract the numeric part (assuming format STU###)
        if (lastStudentId.startsWith("STU") && lastStudentId.length() >= 4) {
            try {
                String numberPart = lastStudentId.substring(3);
                int nextNumber = Integer.parseInt(numberPart) + 1;
                return String.format("STU%03d", nextNumber);
            } catch (NumberFormatException e) {
                // If parsing fails, start from STU001
                return "STU001";
            }
        }
        
        // Default to STU001 if format is unexpected
        return "STU001";
    }

    /**
     * Converts Student entity to StudentResponseDTO
     */
    private StudentResponseDTO convertToDTO(Student student) {
        return StudentResponseDTO.builder()
                .id(student.getId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .email(student.getEmail())
                .studentId(student.getStudentId())
                .build();
    }
}
