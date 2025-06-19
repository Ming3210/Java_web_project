package edu.controller;

import com.cloudinary.Cloudinary;
import com.sun.source.tree.Tree;
import edu.dto.UserDTO;
import edu.entity.Enrollment;
import edu.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import edu.dto.CourseDTO;
import edu.entity.Course;
import edu.service.AdminService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private Cloudinary cloudinary;

    @GetMapping("/admin")
    public String showDashboard(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");

        if (user == null || !"ADMIN".equalsIgnoreCase(user.getRole())) {
            redirectAttributes.addFlashAttribute("error", "Please login to register for a course.");
            return "redirect:/login";
        }

        return "admin/layout";
    }

    @GetMapping("/admin/dashboard")
    public String showAdminDashboard(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");

        if (user == null || !"ADMIN".equalsIgnoreCase(user.getRole())) {
            redirectAttributes.addFlashAttribute("error", "Please login to register for a course.");
            return "redirect:/login";
        }

        model.addAttribute("activePage", "dashboard");
        model.addAttribute("content", "admin/dashboard");
        int totalCourses = adminService.countAllCourse();
        int totalStudents = adminService.countAllStudents();
        int totalEnrollments = adminService.countAllConfirmedEnrollments();
        model.addAttribute("totalCourses", totalCourses);
        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("totalEnrollments", totalEnrollments);
        List<Course> courses = adminService.getAllCourses();
        List<Course> top5Courses = adminService.top5CourseWithHighestEnrollments();
        Comparator<CourseDTO> comparator = Comparator
                .comparingInt(CourseDTO::getTotalStudents)
                .reversed()
                .thenComparing(CourseDTO::getId);

        TreeSet<CourseDTO> sortedSet = new TreeSet<>(comparator);

        List<CourseDTO> courseDTOS = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Course course : top5Courses) {
            CourseDTO dto = new CourseDTO();
            dto.setId(course.getId());
            dto.setName(course.getName());
            dto.setDuration(course.getDuration());
            dto.setInstructor(course.getInstructor());
            dto.setImage(course.getImage());
            dto.setTotalStudents(adminService.staticticsStudentsByCourseId(course.getId()));
            dto.setCreatedAtFormatted(course.getCreatedAt().format(formatter));

            sortedSet.add(dto);

            if (sortedSet.size() > 5) {
                sortedSet.pollLast();
            }
        }

        List<CourseDTO> top5CourseDTOS = new ArrayList<>(sortedSet);
        for (Course course : courses) {
            CourseDTO courseDTO = new CourseDTO();
            int staticticsStudentsByCourseId = adminService.staticticsStudentsByCourseId(course.getId());
            courseDTO.setId(course.getId());
            courseDTO.setName(course.getName());
            courseDTO.setDuration(course.getDuration());
            courseDTO.setInstructor(course.getInstructor());
            courseDTO.setImage(course.getImage());
            courseDTO.setTotalStudents(staticticsStudentsByCourseId);
            courseDTO.setCreatedAtFormatted(course.getCreatedAt().format(formatter));
            courseDTOS.add(courseDTO);
        }
        model.addAttribute("courses", courseDTOS);
        model.addAttribute("top5Courses", top5CourseDTOS);

        return "admin/layout";
    }

    @GetMapping("/admin/students")
    public String showStudents(Model model,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "8") int size,
                               @RequestParam(defaultValue = "id") String sortBy,
                               @RequestParam(defaultValue = "asc") String order,
                               @RequestParam(defaultValue = "ALL") String status,
                               @RequestParam(required = false) String keyword,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");

        if (user == null || !"ADMIN".equalsIgnoreCase(user.getRole())) {
            redirectAttributes.addFlashAttribute("error", "Please login to register for a course.");
            return "redirect:/login";
        }

        model.addAttribute("activePage", "students");
        model.addAttribute("content", "admin/students");
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("order", order);
        model.addAttribute("status", status);

        int totalStudents = adminService.countAllStudentsByStatusAndKeyword(status, keyword);
        int totalPages = (int) Math.ceil((double) totalStudents / size);

        List<User> students = adminService.paginateUsers(page, size, sortBy, order, status, keyword);

        List<UserDTO> studentDTOs = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (User student : students) {
            UserDTO studentDTO = new UserDTO();
            studentDTO.setId(student.getId());
            studentDTO.setUsername(student.getUsername());
            studentDTO.setEmail(student.getEmail());
            studentDTO.setPhone(student.getPhone());
            studentDTO.setRole(student.getRole());
            studentDTO.setStatus(student.isStatus());
            studentDTO.setGender(student.isGender());
            studentDTO.setDob(student.getDob());
            studentDTO.setAvatar(student.getAvatar());
            studentDTOs.add(studentDTO);
        }

        model.addAttribute("students", studentDTOs);
        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("keyword", keyword);

        return "admin/layout";
    }

    @PostMapping("/admin/students/update-status/{id}")
    public String updateStudentStatus(@PathVariable int id, RedirectAttributes redirectAttributes) {

        if (id <= 0) {
            redirectAttributes.addFlashAttribute("error", "Invalid student ID");
            return "redirect:/admin/students";
        }

        try {
            boolean isUpdated = adminService.updateUserStatus(id);
            if (isUpdated) {
                redirectAttributes.addFlashAttribute("success", "Student status updated successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to update student status");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating student status: " + e.getMessage());
        }

        return "redirect:/admin/students";
    }


    @GetMapping("/admin/courses")
    public String showCourses(Model model,
                              @RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "6") int size,
                              @RequestParam(defaultValue = "id") String sortBy,
                              @RequestParam(defaultValue = "asc") String order,
                              @RequestParam(defaultValue = "ACTIVE") String status,
                              @RequestParam(required = false) String keyword,
                              HttpSession session,
                              RedirectAttributes redirectAttributes
                              ) {

        User user = (User) session.getAttribute("user");

        if (user == null || !"ADMIN".equalsIgnoreCase(user.getRole())) {
            redirectAttributes.addFlashAttribute("error", "Please login to register for a course.");
            return "redirect:/login";
        }

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
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            courseDTO.setCreatedAtFormatted(course.getCreatedAt().format(formatter));
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
        course.setStatus(true);
        course.setCreatedAt(LocalDate.now());
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
    public String showEnrollments(Model model,
                                  @RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "8") int size,
                                  @RequestParam(defaultValue = "registeredAt") String sortBy,
                                  @RequestParam(defaultValue = "desc") String order,
                                  @RequestParam(defaultValue = "") String status,
                                  @RequestParam(required = false) String keyword,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");

        if (user == null || !"ADMIN".equalsIgnoreCase(user.getRole())) {
            redirectAttributes.addFlashAttribute("error", "Please login to register for a course.");
            return "redirect:/login";
        }

        model.addAttribute("activePage", "enrollments");
        model.addAttribute("content", "admin/enrollments");
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("order", order);
        model.addAttribute("status", status);

        int totalEnrollments = adminService.countEnrollmentsByStatusAndKeyword(status, keyword);
        int totalPages = (int) Math.ceil((double) totalEnrollments / size);

        List<Enrollment> enrollments = adminService.paginateEnrollments(page, size, sortBy, order, status, keyword);

        model.addAttribute("enrollments", enrollments);
        model.addAttribute("totalEnrollments", totalEnrollments);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("keyword", keyword);

        return "admin/layout";
    }

    @GetMapping("/admin/enrollments/confirm/{id}")
    public String confirmEnrollment(@PathVariable int id, RedirectAttributes redirectAttributes) {
        if (id <= 0) {
            redirectAttributes.addFlashAttribute("error", "Invalid enrollment ID");
            return "redirect:/admin/enrollments";
        }
        Enrollment enrollment = adminService.getEnrollmentById(id);

        try {
            boolean isConfirmed = adminService.aproveEnrollment(enrollment);
            if (isConfirmed) {
                redirectAttributes.addFlashAttribute("success", "Enrollment confirmed successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to confirm enrollment");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error confirming enrollment: " + e.getMessage());
        }

        return "redirect:/admin/enrollments";
    }

    @GetMapping("/admin/enrollments/cancel/{id}")
    public String cancelEnrollment(@PathVariable int id, RedirectAttributes redirectAttributes) {
        if (id <= 0) {
            redirectAttributes.addFlashAttribute("error", "Invalid enrollment ID");
            return "redirect:/admin/enrollments";
        }
        Enrollment enrollment = adminService.getEnrollmentById(id);

        try {
            boolean isCancelled = adminService.rejectEnrollment(enrollment);
            if (isCancelled) {
                redirectAttributes.addFlashAttribute("success", "Enrollment cancelled successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to cancel enrollment");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error cancelling enrollment: " + e.getMessage());
        }

        return "redirect:/admin/enrollments";
    }


}
