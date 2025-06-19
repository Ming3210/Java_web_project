package edu.service;

import edu.entity.Course;
import edu.entity.Enrollment;
import edu.entity.User;
import edu.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

public class ClientServiceImp implements ClientService{
    @Autowired
    private ClientRepository clientRepository;

    @Override
    public List<Course> paginateCourses(int page, int size, String sortBy, String order, String status, String keyword) {
        return clientRepository.paginateCourses(page, size, sortBy, order, status, keyword);
    }

    @Override
    public int countEnrollmentsByUserIdAndKeyword(int userId, String keyword, String status) {
        return clientRepository.countEnrollmentsByUserIdAndKeyword(userId, keyword, status);
    }

    @Override
    public boolean cancelEnrollment(int userId, Enrollment enrollment) {
        return clientRepository.cancelEnrollment(userId, enrollment);
    }

    @Override
    public boolean updateProfile(User user) {
        return clientRepository.updateProfile(user);
    }


}
