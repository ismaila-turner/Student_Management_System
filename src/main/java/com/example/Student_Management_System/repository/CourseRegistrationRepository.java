package com.example.Student_Management_System.repository;

import com.example.Student_Management_System.entity.CourseRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRegistrationRepository extends JpaRepository<CourseRegistration, Long> {
    
    List<CourseRegistration> findByStudentId(Long studentId);
    
    List<CourseRegistration> findByCourseId(Long courseId);
    
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
    
    Optional<CourseRegistration> findByStudentIdAndCourseId(Long studentId, Long courseId);
    
    void deleteByStudentIdAndCourseId(Long studentId, Long courseId);
}
