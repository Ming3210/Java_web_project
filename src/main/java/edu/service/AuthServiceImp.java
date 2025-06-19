package edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import edu.entity.User;
import edu.repository.AuthRepository;

@Service
public class AuthServiceImp implements AuthService{

    @Autowired
    private AuthRepository authRepository;
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
}
