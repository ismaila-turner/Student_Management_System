package com.example.Student_Management_System.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResponseDTO {
    
    private Long id;
    private String courseCode;
    private String courseName;
    private String description;
    private Integer credits;
}
