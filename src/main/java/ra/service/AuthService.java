package ra.service;

import ra.entity.User;

public interface AuthService {
    boolean register(User user);

    User login(String email, String password);
}
