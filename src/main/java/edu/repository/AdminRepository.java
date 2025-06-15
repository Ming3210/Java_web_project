package edu.repository;

import edu.entity.Course;

import java.util.List;

public interface AdminRepository {
    List<Course> getAllCourses();
    int countAllCourse();
    List<Course> paginateCourses(int page, int size, String sortBy, String order, String status, String keyword);
    boolean saveCourse(Course course);
    Course getCourseById(int id);
    boolean editCourse(Course course);
    boolean deleteCourse(int id);
    int countCoursesByStatusAndKeyword(String status, String keyword);
}
