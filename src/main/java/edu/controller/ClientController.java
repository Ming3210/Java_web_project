package edu.controller;

import edu.dto.*;
import edu.entity.Course;
import edu.entity.Enrollment;
import edu.entity.User;
import edu.service.AdminService;
import edu.service.ClientService;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ClientController {



    @Autowired
    private AdminService adminService;



    @Autowired
    ClientService clientService;


    @GetMapping("/")
    public String home(Model model,
                       HttpSession session,
                       @RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "5") int size,
                       @RequestParam(defaultValue = "id") String sortBy,
                       @RequestParam(defaultValue = "asc") String order,
                       @RequestParam(defaultValue = "ACTIVE") String status,
                       @RequestParam(required = false) String keyword) {

        User user = (User) session.getAttribute("user");
        Set<Integer> enrolledCourseIds;

        if (user != null) {
            List<Enrollment> enrollments = adminService.getEnrollmentsByUserId(user.getId());
            enrolledCourseIds = enrollments.stream()
                    .map(e -> e.getCourse().getId())
                    .collect(Collectors.toSet());
        } else {
            enrolledCourseIds = new HashSet<>();
        }

        // Lấy toàn bộ danh sách course (không phân trang)
        List<Course> allCourses = clientService.getAllCourses(sortBy, order, keyword);

        // Lọc course: chỉ giữ course ACTIVE hoặc đã đăng ký
        List<Course> filteredCourses = allCourses.stream()
                .filter(course -> course.isStatus() || enrolledCourseIds.contains(course.getId()))
                .collect(Collectors.toList());

        // Tổng số khóa học sau khi lọc
        int totalCourses = filteredCourses.size();
        int totalPages = (int) Math.ceil((double) totalCourses / size);

        // Phân trang bằng Java
        int fromIndex = Math.max((page - 1) * size, 0);
        int toIndex = Math.min(fromIndex + size, totalCourses);
        List<Course> pagedCourses = filteredCourses.subList(fromIndex, toIndex);

        // Convert sang DTO
        List<CourseDTO> courseDTOS = new ArrayList<>();
        for (Course course : pagedCourses) {
            CourseDTO courseDTO = new CourseDTO();
            courseDTO.setId(course.getId());
            courseDTO.setName(course.getName());
            courseDTO.setDuration(course.getDuration());
            courseDTO.setInstructor(course.getInstructor());
            courseDTO.setImage(course.getImage());
            courseDTO.setCreatedAt(course.getCreatedAt());
            courseDTO.setStatus(course.isStatus());
            courseDTOS.add(courseDTO);
        }

        // Gửi dữ liệu ra view
        model.addAttribute("courses", courseDTOS);
        model.addAttribute("enrolledCourseIds", enrolledCourseIds);
        model.addAttribute("totalCourses", totalCourses);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("order", order);
        model.addAttribute("status", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("activePage", "courses");
        model.addAttribute("content", "admin/courses");

        if (!model.containsAttribute("course")) {
            model.addAttribute("course", new CourseDTO());
            model.addAttribute("isEdit", false);
        }

        return "client/home";
    }


    @PostMapping("/enroll")
    public String enroll(@RequestParam("courseId") int courseId,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {

        User user = (User) session.getAttribute("user");

        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to register for a course.");
            return "redirect:/login";
        }

        int userId = user.getId();
        boolean success = adminService.enrollmentCourse(userId, courseId);

        if (success) {
            redirectAttributes.addFlashAttribute("success", "You have successfully enrolled in the course.");
            redirectAttributes.addFlashAttribute("successEnrollment", true);
        } else {
            // More specific error message
            redirectAttributes.addFlashAttribute("error",
                    "Enrollment failed. You might be already enrolled or the course is not available.");
        }

        return "redirect:/";
    }


    @GetMapping("/enrollment")
    public String viewEnrollments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            HttpSession session,
            Model model) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        int totalEnrollments = clientService.countEnrollmentsByUserIdAndKeyword(user.getId(), keyword, status);
        int totalPages = (int) Math.ceil((double) totalEnrollments / size);

        List<Enrollment> enrollments = adminService.getEnrollmentsByUserIdAndFilter(
                user.getId(), status, keyword, page, size
        );




        List<EnrollmentDTO> enrollmentDTOS = enrollments.stream().map(e -> {
            EnrollmentDTO dto = new EnrollmentDTO();
            dto.setId(e.getId());
            dto.setCourse(e.getCourse());
            dto.setRegisteredAt(e.getRegisteredAt());
            dto.setStatus(e.getStatus());
            return dto;
        }).collect(Collectors.toList());

        model.addAttribute("enrollments", enrollmentDTOS);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("size", size);
        model.addAttribute("status", status);
        model.addAttribute("keyword", keyword);

        model.addAttribute("activePage", "enrollments");
        model.addAttribute("content", "client/enrollments");

        return "client/enrollment";
    }



    @GetMapping("/enrollment/cancel/{id}")
    public String cancelEnrollment(@PathVariable("id") int enrollmentId,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to cancel enrollment.");
            return "redirect:/login";
        }

        List<Enrollment> enrollments = adminService.getEnrollmentsByUserId(user.getId());
        Enrollment enrollment = enrollments.stream()
                .filter(e -> e.getId() == enrollmentId)
                .findFirst()
                .orElse(null);

        if (enrollment == null) {
            redirectAttributes.addFlashAttribute("error", "Enrollment not found.");
            return "redirect:/enrollment";
        }

        boolean success = clientService.cancelEnrollment(user.getId(), enrollment);
        if (success) {
            redirectAttributes.addFlashAttribute("success", "Enrollment cancelled successfully.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to cancel enrollment. Please try again.");
        }

        return "redirect:/enrollment";
    }

    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login";
        }
        if (!model.containsAttribute("passwordDTO")) {
            model.addAttribute("passwordDTO", new PasswordDTO());
        }
        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setId(currentUser.getId());
        profileDTO.setUsername(currentUser.getUsername());
        profileDTO.setEmail(currentUser.getEmail());
        profileDTO.setPhone(currentUser.getPhone());
        profileDTO.setGender(currentUser.isGender());
        profileDTO.setDob(currentUser.getDob());

        model.addAttribute("user", profileDTO);
        return "client/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@Valid @ModelAttribute("user") ProfileDTO profileDTO,
                                BindingResult bindingResult,
                                HttpSession session,
                                RedirectAttributes redirectAttributes,
                                Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", profileDTO);
            return "client/profile";
        }

        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Session expired. Please login again.");
            return "redirect:/login";
        }

        try {
            currentUser.setUsername(profileDTO.getUsername() != null ? profileDTO.getUsername().trim() : currentUser.getUsername());
            currentUser.setPhone(profileDTO.getPhone() != null ? profileDTO.getPhone().trim() : currentUser.getPhone());
            currentUser.setEmail(profileDTO.getEmail() != null ? profileDTO.getEmail().trim() : currentUser.getEmail());
            currentUser.setGender(profileDTO.getGender() != null ? profileDTO.getGender() : currentUser.isGender());

            if (profileDTO.getDob() != null) {
                currentUser.setDob(profileDTO.getDob());
            }


            boolean updated = clientService.updateProfile(currentUser);
            if (updated) {
                session.setAttribute("user", currentUser);
                redirectAttributes.addFlashAttribute("success", "Profile updated successfully.");
                redirectAttributes.addFlashAttribute("successUpdate", true);

            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to update profile. Please try again.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An error occurred while updating profile. Please try again.");
        }

        return "redirect:/profile";
    }


    @PostMapping("/profile/change-password")
    @Transactional
    public String changePassword(@Valid @ModelAttribute("passwordDTO") PasswordDTO passwordDTO,
                                 BindingResult bindingResult,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {

        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Session expired. Please login again.");
            return "redirect:/login";
        }

        // Custom validations
        boolean hasErrors = false;

        // Validate password length
        if (passwordDTO.getNewPassword() != null && passwordDTO.getNewPassword().length() < 6) {
            bindingResult.rejectValue("newPassword", "error.newPassword", "Password must be at least 6 characters long");
            hasErrors = true;
        }

        // Verify current password only if not empty
        if (!bindingResult.hasFieldErrors("currentPassword") &&
                passwordDTO.getCurrentPassword() != null &&
                !passwordDTO.getCurrentPassword().trim().isEmpty()) {

            if (!BCrypt.checkpw(passwordDTO.getCurrentPassword(), currentUser.getPassword())) {
                bindingResult.rejectValue("currentPassword", "error.currentPassword", "Current password is incorrect");
                hasErrors = true;
            }
        }

        // Kiểm tra validation errors hoặc custom errors
        if (bindingResult.hasErrors() || hasErrors) {
            // For security, only preserve new password and confirm password if you really need to
            // Generally NOT recommended for password fields
            PasswordDTO preservedDTO = new PasswordDTO();
            preservedDTO.setCurrentPassword(""); // Never preserve current password
            preservedDTO.setNewPassword(passwordDTO.getNewPassword()); // Preserve if needed
            preservedDTO.setConfirmPassword(passwordDTO.getConfirmPassword()); // Preserve if needed

            redirectAttributes.addFlashAttribute("passwordDTO", preservedDTO);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.passwordDTO", bindingResult);
            redirectAttributes.addFlashAttribute("showPasswordModal", true);
            return "redirect:/profile";
        }

        // Update password
        try {
            String hashedPassword = BCrypt.hashpw(passwordDTO.getNewPassword(), BCrypt.gensalt());
            currentUser.setPassword(hashedPassword);

            boolean updated = clientService.updatePassword(currentUser.getId(), hashedPassword);

            if (updated) {
                session.setAttribute("user", currentUser);
                redirectAttributes.addFlashAttribute("success", "Password changed successfully.");
                redirectAttributes.addFlashAttribute("successUpdate", true);

            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to change password. Please try again.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An error occurred while changing password. Please try again.");
            e.printStackTrace();
        }

        return "redirect:/profile";
    }
}
