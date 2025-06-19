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

    private LocalDate createdAt;

    private String image;

    @Column (nullable = false, columnDefinition = "BOOLEAN DEFAULT(1)")
    private boolean status;

    @OneToMany (mappedBy = "course", cascade = CascadeType.ALL)
    private List<Enrollment> enrollments;
}
