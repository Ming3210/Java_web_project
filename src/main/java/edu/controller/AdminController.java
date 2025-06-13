package edu.controller;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import edu.dto.CourseDTO;
import edu.entity.Course;
import edu.service.AdminService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private Cloudinary cloudinary;

    @GetMapping("/admin")
    public String showDashboard() {
        return "admin/layout";
    }

    @GetMapping("/admin/dashboard")
    public String showAdminDashboard(Model model) {
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("content", "admin/dashboard");
        return "admin/layout";
    }

    @GetMapping("/admin/students")
    public String showStudents(Model model) {
        model.addAttribute("activePage", "students");
        model.addAttribute("content", "admin/students");
        return "admin/layout";
    }

    @GetMapping("/admin/courses")
    public String showCourses(Model model,
                              @RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "8") int size) {
        model.addAttribute("activePage", "courses");
        model.addAttribute("content", "admin/courses");
        int totalCourses = adminService.countAllCourse();
        int totalPages = (int) Math.ceil((double) totalCourses / size);
        List<CourseDTO> courseDTOS = new ArrayList<>();
        List<Course> courses = adminService.paginateCourses(page, size);
        for (Course course : courses) {
            CourseDTO courseDTO = new CourseDTO();
            courseDTO.setId(course.getId());
            courseDTO.setName(course.getName());
            courseDTO.setDuration(course.getDuration());
            courseDTO.setCreatedAt(course.getCreatedAt());
            courseDTO.setImage(course.getImage());
            courseDTOS.add(courseDTO);
        }
        model.addAttribute("courses", courseDTOS);
        model.addAttribute("totalCourses", totalCourses);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        return "admin/layout";
    }

    @GetMapping("/admin/courses/add")
    public String showAddCourseForm(Model model) {
        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setId(null);
        model.addAttribute("course", courseDTO);
        model.addAttribute("activePage", "courses");
        model.addAttribute("content", "fragments/course-form");
        model.addAttribute("isEdit", false); // Thêm flag này
        return "admin/layout";
    }

    @PostMapping("/admin/courses/save")
    public String saveCourse(@ModelAttribute CourseDTO courseDTO) {
        System.out.println("=== SAVE COURSE DEBUG ===");
        System.out.println("Course ID: " + courseDTO.getId());
        System.out.println("Course Name: " + courseDTO.getName());

        String imageUrl = null;

        if (courseDTO.getImageFile() != null && !courseDTO.getImageFile().isEmpty()) {
            try {
                Map<String, Object> uploadParams = new HashMap<>();
                uploadParams.put("folder", "courses");
                Map uploadResult = cloudinary.uploader().upload(
                        courseDTO.getImageFile().getBytes(), uploadParams
                );
                imageUrl = uploadResult.get("secure_url").toString();
                System.out.println("Image uploaded: " + imageUrl);
            } catch (Exception e) {
                System.err.println("Error uploading image: " + e.getMessage());
                e.printStackTrace();
            }
        }

        Course course = new Course();
        // Không set ID cho course mới, để database tự generate
        course.setName(courseDTO.getName());
        course.setDuration(courseDTO.getDuration());
        course.setInstructor(courseDTO.getInstructor());
        course.setImage(imageUrl);
        course.setCreatedAt(LocalDateTime.now());

        try {
            adminService.saveCourse(course);
            System.out.println("Course saved successfully");
        } catch (Exception e) {
            System.err.println("Error saving course: " + e.getMessage());
            e.printStackTrace();
        }

        return "redirect:/admin/courses";
    }

    @GetMapping("/admin/courses/edit/{id}")
    public String showEditCourseForm(@PathVariable int id, Model model) {
        Course course = adminService.getCourseById(id);
        if (course != null) {
            CourseDTO courseDTO = new CourseDTO();
            courseDTO.setId(course.getId());
            courseDTO.setName(course.getName());
            courseDTO.setDuration(course.getDuration());
            courseDTO.setInstructor(course.getInstructor());
            courseDTO.setImage(course.getImage());
            courseDTO.setCreatedAt(course.getCreatedAt());
            model.addAttribute("course", courseDTO);
        }
        model.addAttribute("activePage", "courses");
        model.addAttribute("content", "fragments/course-form");
        model.addAttribute("isEdit", true); // Thêm flag này
        return "admin/layout";
    }

    @PostMapping("/admin/courses/update")
    public String updateCourse(@ModelAttribute CourseDTO courseDTO) {
        System.out.println("=== UPDATE COURSE DEBUG ===");
        System.out.println("Course ID: " + courseDTO.getId());
        System.out.println("Course Name: " + courseDTO.getName());

        if (courseDTO.getId() == null || courseDTO.getId() <= 0) {
            System.err.println("Invalid course ID for update");
            return "redirect:/admin/courses";
        }

        Course existingCourse = adminService.getCourseById(courseDTO.getId());
        if (existingCourse == null) {
            System.err.println("Course not found with ID: " + courseDTO.getId());
            return "redirect:/admin/courses";
        }

        String imageUrl = existingCourse.getImage();

        if (courseDTO.getImageFile() != null && !courseDTO.getImageFile().isEmpty()) {
            try {
                Map<String, Object> uploadParams = new HashMap<>();
                uploadParams.put("folder", "courses");
                Map uploadResult = cloudinary.uploader().upload(
                        courseDTO.getImageFile().getBytes(), uploadParams
                );
                imageUrl = uploadResult.get("secure_url").toString();
                System.out.println("Image updated: " + imageUrl);
            } catch (Exception e) {
                System.err.println("Error uploading image: " + e.getMessage());
                e.printStackTrace();
            }
        }

        Course course = new Course();
        course.setId(courseDTO.getId());
        course.setName(courseDTO.getName());
        course.setDuration(courseDTO.getDuration());
        course.setInstructor(courseDTO.getInstructor());
        course.setImage(imageUrl);
        course.setCreatedAt(existingCourse.getCreatedAt());

        try {
            adminService.editCourse(course);
            System.out.println("Course updated successfully");
        } catch (Exception e) {
            System.err.println("Error updating course: " + e.getMessage());
            e.printStackTrace();
        }

        return "redirect:/admin/courses";
    }

    @GetMapping("/admin/enrollments")
    public String showEnrollments(Model model) {
        model.addAttribute("activePage", "enrollments");
        model.addAttribute("content", "admin/enrollments");
        return "admin/layout";
    }
}