package com.example.Student_Management_System.service;

import com.example.Student_Management_System.dto.CourseRegistrationResponseDTO;
import com.example.Student_Management_System.entity.Course;
import com.example.Student_Management_System.entity.CourseRegistration;
import com.example.Student_Management_System.entity.Student;
import com.example.Student_Management_System.exception.DuplicateResourceException;
import com.example.Student_Management_System.exception.ResourceNotFoundException;
import com.example.Student_Management_System.repository.CourseRegistrationRepository;
import com.example.Student_Management_System.repository.CourseRepository;
import com.example.Student_Management_System.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseRegistrationService {

    private final CourseRegistrationRepository courseRegistrationRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public CourseRegistrationResponseDTO registerForCourse(String studentId, String courseCode) {
        // Check if student exists by studentId string
        Student student = studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "studentId", studentId));

        // Check if course exists by courseCode string
        Course course = courseRepository.findByCourseCode(courseCode)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "courseCode", courseCode));

        // Check if already registered
        if (courseRegistrationRepository.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
            throw new DuplicateResourceException("CourseRegistration", "studentId and courseCode", 
                    studentId + " and " + courseCode);
        }

        // Create course registration
        CourseRegistration registration = CourseRegistration.builder()
                .student(student)
                .course(course)
                .registrationDate(LocalDateTime.now())
                .build();

        // Save registration
        CourseRegistration savedRegistration = courseRegistrationRepository.save(registration);

        // Convert to DTO and return
        return convertToDTO(savedRegistration);
    }

    public CourseRegistrationResponseDTO registerForCourse(Long studentId, Long courseId) {
        // Check if student exists
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));

        // Check if course exists
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        // Check if already registered
        if (courseRegistrationRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new DuplicateResourceException("CourseRegistration", "studentId and courseId", 
                    studentId + " and " + courseId);
        }

        // Create course registration
        CourseRegistration registration = CourseRegistration.builder()
                .student(student)
                .course(course)
                .registrationDate(LocalDateTime.now())
                .build();

        // Save registration
        CourseRegistration savedRegistration = courseRegistrationRepository.save(registration);

        // Convert to DTO and return
        return convertToDTO(savedRegistration);
    }

    public void unregisterFromCourse(String studentId, String courseCode) {
        // Check if student exists by studentId string
        Student student = studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "studentId", studentId));

        // Check if course exists by courseCode string
        Course course = courseRepository.findByCourseCode(courseCode)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "courseCode", courseCode));

        // Check if registration exists
        if (!courseRegistrationRepository.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
            throw new ResourceNotFoundException("CourseRegistration", "studentId and courseCode", 
                    studentId + " and " + courseCode);
        }

        // Delete registration
        courseRegistrationRepository.deleteByStudentIdAndCourseId(student.getId(), course.getId());
    }

    public void unregisterFromCourse(Long studentId, Long courseId) {
        // Check if student exists
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student", "id", studentId);
        }

        // Check if course exists
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course", "id", courseId);
        }

        // Check if registration exists
        if (!courseRegistrationRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new ResourceNotFoundException("CourseRegistration", "studentId and courseId", 
                    studentId + " and " + courseId);
        }

        // Delete registration
        courseRegistrationRepository.deleteByStudentIdAndCourseId(studentId, courseId);
    }

    @Transactional(readOnly = true)
    public List<CourseRegistrationResponseDTO> getStudentCourses(String studentId) {
        // Check if student exists by studentId string
        Student student = studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "studentId", studentId));

        // Get all registrations for the student
        List<CourseRegistration> registrations = courseRegistrationRepository.findByStudentId(student.getId());

        // Convert to DTOs and return
        return registrations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CourseRegistrationResponseDTO> getStudentCourses(Long studentId) {
        // Check if student exists
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student", "id", studentId);
        }

        // Get all registrations for the student
        List<CourseRegistration> registrations = courseRegistrationRepository.findByStudentId(studentId);

        // Convert to DTOs and return
        return registrations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Converts CourseRegistration entity to CourseRegistrationResponseDTO
     */
    private CourseRegistrationResponseDTO convertToDTO(CourseRegistration registration) {
        Course course = registration.getCourse();
        return CourseRegistrationResponseDTO.builder()
                .id(registration.getId())
                .courseCode(course.getCourseCode())
                .courseName(course.getCourseName())
                .description(course.getDescription())
                .credits(course.getCredits())
                .registrationDate(registration.getRegistrationDate())
                .build();
    }
}
