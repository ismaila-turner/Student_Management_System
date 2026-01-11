package com.example.Student_Management_System.service;

import com.example.Student_Management_System.dto.CourseRequestDTO;
import com.example.Student_Management_System.dto.CourseResponseDTO;
import com.example.Student_Management_System.entity.Course;
import com.example.Student_Management_System.exception.DuplicateResourceException;
import com.example.Student_Management_System.exception.ResourceNotFoundException;
import com.example.Student_Management_System.repository.CourseRegistrationRepository;
import com.example.Student_Management_System.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseRegistrationRepository courseRegistrationRepository;

    public CourseResponseDTO createCourse(CourseRequestDTO requestDTO) {
        // Check if course code already exists
        if (courseRepository.existsByCourseCode(requestDTO.getCourseCode())) {
            throw new DuplicateResourceException("Course", "courseCode", requestDTO.getCourseCode());
        }

        // Create course entity
        Course course = Course.builder()
                .courseCode(requestDTO.getCourseCode())
                .courseName(requestDTO.getCourseName())
                .description(requestDTO.getDescription())
                .credits(requestDTO.getCredits())
                .build();

        // Save course
        Course savedCourse = courseRepository.save(course);

        // Convert to DTO and return
        return convertToDTO(savedCourse);
    }

    @Transactional(readOnly = true)
    public CourseResponseDTO getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
        return convertToDTO(course);
    }

    @Transactional(readOnly = true)
    public List<CourseResponseDTO> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public CourseResponseDTO updateCourse(Long id, CourseRequestDTO requestDTO) {
        // Find existing course
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));

        // Check if course code is being changed and if new course code already exists
        if (!course.getCourseCode().equals(requestDTO.getCourseCode()) && 
            courseRepository.existsByCourseCode(requestDTO.getCourseCode())) {
            throw new DuplicateResourceException("Course", "courseCode", requestDTO.getCourseCode());
        }

        // Update course fields
        course.setCourseCode(requestDTO.getCourseCode());
        course.setCourseName(requestDTO.getCourseName());
        course.setDescription(requestDTO.getDescription());
        course.setCredits(requestDTO.getCredits());

        // Save updated course
        Course updatedCourse = courseRepository.save(course);

        // Convert to DTO and return
        return convertToDTO(updatedCourse);
    }

    public void deleteCourse(Long id) {
        // Check if course exists
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));

        // Delete all registrations for this course first
        courseRegistrationRepository.deleteAll(courseRegistrationRepository.findByCourseId(id));

        // Delete course
        courseRepository.deleteById(id);
    }

    /**
     * Converts Course entity to CourseResponseDTO
     */
    private CourseResponseDTO convertToDTO(Course course) {
        return CourseResponseDTO.builder()
                .id(course.getId())
                .courseCode(course.getCourseCode())
                .courseName(course.getCourseName())
                .description(course.getDescription())
                .credits(course.getCredits())
                .build();
    }
}
