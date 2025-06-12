package ra.repository;

import ra.entity.User;

public interface AuthRepository {
    boolean register(User user);

    User login(String email, String password);

}
