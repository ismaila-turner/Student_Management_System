package com.example.Student_Management_System.repository;

import com.example.Student_Management_System.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    
    Optional<Student> findByEmail(String email);
    
    Optional<Student> findByStudentId(String studentId);
    
    Optional<Student> findByUserId(Long userId);
    
    boolean existsByEmail(String email);
    
    boolean existsByStudentId(String studentId);
    
    Optional<Student> findTopByOrderByStudentIdDesc();
}
