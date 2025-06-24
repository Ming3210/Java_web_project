package edu.service;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import edu.entity.User;
import edu.repository.AuthRepository;

@Service
public class AuthServiceImp implements AuthService{

    @Autowired
    private AuthRepository authRepository;
    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public boolean register(User user) {
        return authRepository.register(user);
    }

    @Override
    public User login(String email, String password) {
        return authRepository.login(email, password);
    }

    @Override
    public boolean existsByEmail(String email) {
        return authRepository.existsByEmail(email);
    }

    @Override
    public User findUserByEmail(String email) {
        return authRepository.findUserByEmail(email);
    }
}
