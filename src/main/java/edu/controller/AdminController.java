package edu.controller;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import edu.dto.CourseDTO;
import edu.entity.Course;
import edu.service.AdminService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
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
                              @RequestParam(defaultValue = "8") int size,
                              @RequestParam(defaultValue = "id") String sortBy,
                              @RequestParam(defaultValue = "asc") String order,
                              @RequestParam(defaultValue = "ALL") String status,
                              @RequestParam(required = false) String keyword


                              ) {
        model.addAttribute("activePage", "courses");
        model.addAttribute("content", "admin/courses");
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("order", order);
        model.addAttribute("status", status);

        int totalCourses = adminService.countCoursesByStatusAndKeyword(status, keyword);
        int totalPages = (int) Math.ceil((double) totalCourses / size);

        List<Course> courses = adminService.paginateCourses(page, size, sortBy, order, status, keyword);

        List<CourseDTO> courseDTOS = new ArrayList<>();
        for (Course course : courses) {
            CourseDTO courseDTO = new CourseDTO();
            courseDTO.setId(course.getId());
            courseDTO.setName(course.getName());
            courseDTO.setDuration(course.getDuration());
            courseDTO.setInstructor(course.getInstructor());
            courseDTO.setImage(course.getImage());
            courseDTO.setCreatedAt(course.getCreatedAt());
            courseDTOS.add(courseDTO);
        }

        model.addAttribute("courses", courseDTOS);
        model.addAttribute("totalCourses", totalCourses);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("keyword", keyword);

        if (!model.containsAttribute("course")) {
            model.addAttribute("course", new CourseDTO());
            model.addAttribute("isEdit", false);
        }

        return "admin/layout";
    }



//    @GetMapping("/admin/courses/add")
//    public String showAddCourseForm(Model model) {
//        CourseDTO courseDTO = new CourseDTO();
//        courseDTO.setId(null);
//        model.addAttribute("course", courseDTO);
//
//        return "admin/layout";
//    }

    @PostMapping("/admin/courses/save")
    public String saveCourse(@Valid @ModelAttribute("course") CourseDTO courseDTO, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        System.out.println("=== SAVE COURSE DEBUG ===");
        System.out.println("Course ID: " + courseDTO.getId());
        System.out.println("Course Name: " + courseDTO.getName());
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.course", result);
            redirectAttributes.addFlashAttribute("course", courseDTO);
            redirectAttributes.addFlashAttribute("openModal", true);
            model.addAttribute("openModal", true);
            return "redirect:/admin/courses";
        }
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
    public String showEditCourseForm(@PathVariable int id, RedirectAttributes redirectAttributes) {
        Course course = adminService.getCourseById(id);
        if (course != null) {
            CourseDTO courseDTO = new CourseDTO();
            courseDTO.setId(course.getId());
            courseDTO.setName(course.getName());
            courseDTO.setDuration(course.getDuration());
            courseDTO.setInstructor(course.getInstructor());
            courseDTO.setImage(course.getImage());
            courseDTO.setCreatedAt(course.getCreatedAt());

            redirectAttributes.addFlashAttribute("course", courseDTO);
            redirectAttributes.addFlashAttribute("isEdit", true);
            redirectAttributes.addFlashAttribute("openModal", true);
        }
        return "redirect:/admin/courses";
    }


    @PostMapping("/admin/courses/update")
    public String updateCourse(@Valid @ModelAttribute("course") CourseDTO courseDTO, BindingResult result, Model model) {
        System.out.println("=== UPDATE COURSE DEBUG ===");
        System.out.println("Course ID: " + courseDTO.getId());
        System.out.println("Course Name: " + courseDTO.getName());

        if(result.hasErrors()) {
            model.addAttribute("course", courseDTO);
            model.addAttribute("activePage", "courses");
            model.addAttribute("content", "fragments/course-form");
            model.addAttribute("isEdit", true);
            return "admin/layout";
        }

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
                model.addAttribute("uploadError", "Failed to upload image. Please try again.");
                model.addAttribute("course", courseDTO);
                model.addAttribute("activePage", "courses");
                model.addAttribute("content", "fragments/course-form");
                model.addAttribute("isEdit", true);
                return "admin/layout";
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
        } catch (Exception e) {
            System.err.println("Error updating course: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("saveError", "Failed to update course. Please try again.");
            model.addAttribute("course", courseDTO);
            model.addAttribute("activePage", "courses");
            model.addAttribute("content", "fragments/course-form");
            model.addAttribute("isEdit", true);
            return "admin/layout";
        }

        return "redirect:/admin/courses";
    }

    @GetMapping("/admin/courses/delete/{id}")
        public String deleteCourse(@PathVariable int id) {

            if (id <= 0) {
                System.err.println("Invalid course ID for deletion");
                return "redirect:/admin/courses";
            }

            try {
                boolean isDeleted = adminService.deleteCourse(id);

            } catch (Exception e) {
                System.err.println("Error deleting course: " + e.getMessage());
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
