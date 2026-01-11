package com.example.Student_Management_System.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentResponseDTO {
    
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String studentId;
}
