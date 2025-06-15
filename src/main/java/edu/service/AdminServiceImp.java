package edu.service;

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
}
