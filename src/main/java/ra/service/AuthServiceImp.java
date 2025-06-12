package ra.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ra.entity.User;
import ra.repository.AuthRepository;

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
}
