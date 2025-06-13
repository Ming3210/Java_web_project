package edu.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table (name = "users")
@Data

public class User {

    @Id
    @GeneratedValue (strategy = javax.persistence.GenerationType.IDENTITY)
    private int id;


    @Column (name = "username", nullable = false)
    private String username;


    @Column (name = "password", nullable = false)
    private String password;

    @Column (name = "email", nullable = false, unique = true)
    private String email;

    @Column (name = "role", nullable = false, columnDefinition = "varchar(20) default 'USER'")
    private String role;

    @Column (name = "dob")
    private LocalDate dob;

    @Column (name = "status", nullable = false, columnDefinition = "boolean default true")
    private boolean status;

    @Column (name = "gender", nullable = false)
    private boolean gender;

    @Column (name = "phone", nullable = false, unique = true)
    private String phone;

    private String avatar;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Enrollment> enrollments;
}
