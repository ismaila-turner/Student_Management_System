package com.example.Student_Management_System.controller;

import com.example.Student_Management_System.dto.CourseRegistrationResponseDTO;
import com.example.Student_Management_System.service.CourseRegistrationService;
import com.example.Student_Management_System.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students/{studentId}/courses")
@RequiredArgsConstructor
public class CourseRegistrationController {

    private final CourseRegistrationService courseRegistrationService;
    private final StudentService studentService;

    @PostMapping("/{courseCode}/register")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('STUDENT') and @studentService.isOwnStudentId(#studentId))")
    public ResponseEntity<CourseRegistrationResponseDTO> registerForCourse(
            @PathVariable String studentId,
            @PathVariable String courseCode) {
        CourseRegistrationResponseDTO responseDTO = courseRegistrationService.registerForCourse(studentId, courseCode);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @DeleteMapping("/{courseCode}/unregister")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('STUDENT') and @studentService.isOwnStudentId(#studentId))")
    public ResponseEntity<Void> unregisterFromCourse(
            @PathVariable String studentId,
            @PathVariable String courseCode) {
        courseRegistrationService.unregisterFromCourse(studentId, courseCode);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or (hasRole('STUDENT') and @studentService.isOwnStudentId(#studentId))")
    public ResponseEntity<List<CourseRegistrationResponseDTO>> getStudentCourses(@PathVariable String studentId) {
        List<CourseRegistrationResponseDTO> courses = courseRegistrationService.getStudentCourses(studentId);
        return new ResponseEntity<>(courses, HttpStatus.OK);
    }
}
