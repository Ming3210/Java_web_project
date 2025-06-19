package edu.service;

import edu.entity.Enrollment;
import edu.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import edu.entity.Course;
import edu.repository.AdminRepository;

import java.util.List;
@Service
public class AdminServiceImp implements AdminService{

    @Autowired
    private AdminRepository adminRepository;
    @Override
    public List<Course> getAllCourses() {
        return adminRepository.getAllCourses();
    }

    @Override
    public int countAllCourse() {
        return adminRepository.countAllCourse();
    }

    @Override
    public List<Course> paginateCourses(int page, int size, String sortBy, String order, String status, String keyword) {
        return adminRepository.paginateCourses(page, size, sortBy, order, status, keyword);
    }

    @Override
    public boolean saveCourse(Course course) {
        return adminRepository.saveCourse(course);
    }

    @Override
    public Course getCourseById(int id) {
        return adminRepository.getCourseById(id);
    }

    @Override
    public boolean editCourse(Course course) {
        return adminRepository.editCourse(course);
    }

    @Override
    public boolean deleteCourse(int id) {
        return adminRepository.deleteCourse(id);
    }

    @Override
    public int countCoursesByStatusAndKeyword(String status, String keyword) {
        return adminRepository.countCoursesByStatusAndKeyword(status, keyword);
    }

    @Override
    public boolean enrollmentCourse(int userId, int courseId) {
        return adminRepository.enrollmentCourse(userId, courseId);
    }

    @Override
    public List<Enrollment> getEnrollmentsByUserId(int userId) {
        return adminRepository.getEnrollmentsByUserId(userId);
    }

    @Override
    public List<Enrollment> getEnrollmentsByUserIdAndFilter(int userId, String status, String keyword, int page, int size) {
        return adminRepository.getEnrollmentsByUserIdAndFilter(userId, status, keyword, page, size);
    }

    @Override
    public List<User> paginateUsers(int page, int size, String sortBy, String order, String status, String keyword) {
        return adminRepository.paginateUsers(page, size, sortBy, order, status, keyword);
    }

    @Override
    public int countAllStudentsByStatusAndKeyword(String status, String keyword) {
        return adminRepository.countAllStudentsByStatusAndKeyword( status, keyword);
    }

    @Override
    public boolean updateUserStatus(int userId) {
        return adminRepository.updateUserStatus(userId);
    }

    @Override
    public int countAllStudents() {
        return adminRepository.countAllStudents();
    }

    @Override
    public int countAllConfirmedEnrollments() {
        return adminRepository.countAllConfirmedEnrollments();
    }

    @Override
    public int staticticsStudentsByCourseId(int courseId) {
        return adminRepository.staticticsStudentsByCourseId(courseId);
    }

    @Override
    public List<Course> top5CourseWithHighestEnrollments() {
        return adminRepository.top5CourseWithHighestEnrollments();
    }

    @Override
    public List<Enrollment> paginateEnrollments(int page, int size, String sortBy, String order, String status, String keyword) {
        return adminRepository.paginateEnrollments(page, size, sortBy, order, status, keyword);
    }

    @Override
    public int countEnrollmentsByStatusAndKeyword(String status, String keyword) {
        return adminRepository.countEnrollmentsByStatusAndKeyword(status, keyword);
    }

    @Override
    public boolean aproveEnrollment(Enrollment enrollment) {
        return adminRepository.aproveEnrollment(enrollment);
    }

    @Override
    public boolean rejectEnrollment(Enrollment enrollment) {
        return adminRepository.rejectEnrollment(enrollment);
    }

    @Override
    public Enrollment getEnrollmentById(int id) {
        return adminRepository.getEnrollmentById(id);
    }

}
