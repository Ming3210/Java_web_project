package edu.repository;

import edu.entity.Course;

import java.util.List;

public interface AdminRepository {
    List<Course> getAllCourses();
    int countAllCourse();
    List<Course> paginateCourses(int page, int size);
    boolean saveCourse(Course course);
    Course getCourseById(int id);
    boolean editCourse(Course course);
}
