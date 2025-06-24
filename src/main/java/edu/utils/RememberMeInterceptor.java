package edu.utils;

import edu.entity.User;

import edu.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Base64;

@Component
public class RememberMeInterceptor implements HandlerInterceptor {

    @Autowired
    private AuthService authService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession session = request.getSession();

        // Nếu chưa login và có remember-me cookie
        if (session.getAttribute("user") == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("remember-me".equals(cookie.getName())) {
                        String token = cookie.getValue();
                        User user = validateRememberMeToken(token);
                        if (user != null && user.isStatus()) {
                            session.setAttribute("user", user);
                            // Renew cookie
                            cookie.setMaxAge(60 * 60 * 24 * 30); // 30 days
                            response.addCookie(cookie);
                        } else {
                            // Invalid token, remove cookie
                            cookie.setMaxAge(0);
                            cookie.setPath("/");
                            response.addCookie(cookie);
                        }
                        break;
                    }
                }
            }
        }
        return true;
    }

    private User validateRememberMeToken(String token) {
        try {
            String decoded = new String(Base64.getDecoder().decode(token));
            String[] parts = decoded.split(":");
            if (parts.length >= 2) {
                String email = parts[0];
                // Validate user exists and is active
                return authService.findUserByEmail(email);
            }
        } catch (Exception e) {
            System.out.println("Invalid remember me token: " + e.getMessage());
        }
        return null;
    }
}