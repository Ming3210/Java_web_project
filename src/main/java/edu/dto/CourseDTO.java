package edu.dto;

import edu.validation.NotEmptyMultipartFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CourseDTO {
    private Integer id;
    @NotBlank(message = "Course name is required")
    @Size(max = 100, message = "Course name must be less than 100 characters")
    private String name;

    @Min( value = 1, message = "Duration must be at least 1 hour")
    @NotNull(message = "Duration is required")
    private Integer duration;


    @NotBlank (message = "Instructor name is required")
    private String instructor;

    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate createdAt;

    private boolean status;

    private String image;
    private String createdAtFormatted;

    private int totalStudents;

//    @NotEmptyMultipartFile(message = "Image is required")
    private MultipartFile imageFile;
}
