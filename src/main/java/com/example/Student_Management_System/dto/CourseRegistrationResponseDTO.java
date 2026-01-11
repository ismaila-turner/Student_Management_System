package com.example.Student_Management_System.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseRegistrationResponseDTO {
    
    private Long id;
    private String courseCode;
    private String courseName;
    private String description;
    private Integer credits;
    private LocalDateTime registrationDate;
}
