package edu.dto;

import edu.entity.Course;
import edu.entity.User;
import edu.utils.CourseStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class EnrollmentDTO {
    private int id;
    private LocalDate registeredAt;
    private User user;
    private Course course;
    private CourseStatus status;

}
