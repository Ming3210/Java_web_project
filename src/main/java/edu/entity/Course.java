package edu.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table (name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    @Id
    @GeneratedValue (strategy = javax.persistence.GenerationType.IDENTITY)
    private Integer id;


    private String name;

    private int duration;

    private String instructor;

    @Column(columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    private String image;

    @Column ( columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean status;

    @OneToMany (mappedBy = "course", cascade = CascadeType.ALL)
    private List<Enrollment> enrollments;
}
