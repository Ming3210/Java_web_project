package edu.entity;


import lombok.Data;
import edu.utils.CourseStatus;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table (name = "enrollments")
@Data
public class Enrollment {

    @Id
    @GeneratedValue (strategy = javax.persistence.GenerationType.IDENTITY)
    private int id;


    @Column(name = "registered_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDate registeredAt;

    private CourseStatus status;

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "courseId", nullable = false)
    private Course course;
}
