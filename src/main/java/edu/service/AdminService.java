package edu.service;

import edu.entity.Course;
import edu.entity.Enrollment;
import edu.entity.User;

import java.util.List;

public interface AdminService {
    List<Course> getAllCourses();
    int countAllCourse();
    List<Course> paginateCourses(int page, int size, String sortBy, String order, String status, String keyword);
    boolean saveCourse(Course course);
    Course getCourseById(int id);
    boolean editCourse(Course course);
    boolean deleteCourse(int id);
    int countCoursesByStatusAndKeyword(String status, String keyword);
    boolean enrollmentCourse(int userId, int courseId);
    List<Enrollment> getEnrollmentsByUserId(int userId);
    List<Enrollment> getEnrollmentsByUserIdAndFilter(int userId, String status, String keyword, int page, int size);
    List<User> paginateUsers(int page, int size, String sortBy, String order, String status, String keyword);
    int countAllStudentsByStatusAndKeyword( String status, String keyword);
    boolean updateUserStatus(int userId);
    int countAllStudents();
    int countAllConfirmedEnrollments();
    int staticticsStudentsByCourseId(int courseId);
    List<Course> top5CourseWithHighestEnrollments();
    List<Enrollment> paginateEnrollments(int page, int size, String sortBy, String order, String status, String keyword);
    int countEnrollmentsByStatusAndKeyword(String status, String keyword);


    boolean aproveEnrollment(Enrollment enrollment);
    boolean rejectEnrollment(Enrollment enrollment);

    Enrollment getEnrollmentById(int id);
}
