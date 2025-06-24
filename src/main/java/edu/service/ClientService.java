package edu.service;

import edu.entity.Course;
import edu.entity.Enrollment;
import edu.entity.User;

import java.util.List;

public interface ClientService {
    List<Course> paginateCourses(int page, int size, String sortBy, String order, String status, String keyword);
    int countEnrollmentsByUserIdAndKeyword(int userId, String keyword, String status);
    boolean cancelEnrollment(int userId, Enrollment enrollment);
    boolean updateProfile(User user);
    User findUserById(int userId);
    boolean updatePassword(int userId, String newPassword);
    List<Course> getAllCourses(String sortBy, String order, String keyword);

}
