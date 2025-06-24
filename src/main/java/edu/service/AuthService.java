package edu.service;

import edu.entity.User;

public interface AuthService {
    boolean register(User user);

    User login(String email, String password);

    boolean existsByEmail(String email);

    User findUserByEmail(String email);

}
