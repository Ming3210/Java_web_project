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
    public List<Course> paginateCourses(int page, int size) {
        return adminRepository.paginateCourses(page, size);
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
}
