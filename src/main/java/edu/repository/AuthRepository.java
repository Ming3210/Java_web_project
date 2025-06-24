package edu.repository;

import edu.entity.User;

public interface AuthRepository {
    boolean register(User user);

    User login(String email, String password);

    boolean existsByEmail(String email);

    User findUserByEmail(String email);

}
