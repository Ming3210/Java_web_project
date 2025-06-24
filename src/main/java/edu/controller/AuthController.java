package edu.controller;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import edu.dto.LoginDTO;
import edu.dto.UserDTO;
import edu.entity.User;
import edu.service.AuthService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Base64;

@Controller
public class AuthController {
    @Autowired
    private AuthService authService;

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("loginDTO", new LoginDTO());
        return "auth/login";
    }


    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginDTO") LoginDTO loginDTO,
                        BindingResult result,
                        HttpSession session,
                        HttpServletResponse response,
                        Model model,
                        RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "auth/login";
        }

        User loggedInUser = authService.login(loginDTO.getEmail(), loginDTO.getPassword());

        if (loggedInUser != null && loggedInUser.isStatus()) {
            // Set session timeout
            int timeout = loginDTO.isRememberMe() ? 60 * 60 * 24 * 7 : 3600;
            session.setMaxInactiveInterval(timeout);

            // Create remember me cookie if selected
            if (loginDTO.isRememberMe()) {
                Cookie rememberMeCookie = new Cookie("remember-me", generateRememberMeToken(loggedInUser));
                rememberMeCookie.setMaxAge(60 * 60 * 24 * 30); // 30 days
                rememberMeCookie.setPath("/");
                rememberMeCookie.setHttpOnly(true);
                rememberMeCookie.setSecure(false); // Set true if using HTTPS
                response.addCookie(rememberMeCookie);

                System.out.println("Remember me cookie created for: " + loggedInUser.getEmail());
            }

            session.setAttribute("user", loggedInUser);

            if ("ADMIN".equalsIgnoreCase(loggedInUser.getRole())) {
                return "redirect:/admin/dashboard";
            } else {
                redirectAttributes.addFlashAttribute("successMessage", true);
                return "redirect:/";
            }
        } else {
            if (loggedInUser != null && !loggedInUser.isStatus()) {
                model.addAttribute("loginError", "Tài khoản của bạn đã bị khóa");
            } else {
                model.addAttribute("loginError", "Email hoặc mật khẩu không đúng");
            }
            return "auth/login";
        }
    }

    private String generateRememberMeToken(User user) {
        // Implement a secure token generation method
        // This is just a simple example - use a more secure method in production
        return Base64.getEncoder().encodeToString(
                (user.getEmail() + ":" + System.currentTimeMillis()).getBytes()
        );
    }
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new UserDTO());
        return "auth/register";
    }



    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserDTO userDTO,
                               BindingResult bindingResult
                              ) {

        System.out.println("Has validation errors: " + bindingResult.hasErrors());
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        User user = new User();
        user.setEmail(userDTO.getEmail());
        String hashedPassword = BCrypt.hashpw(userDTO.getPassword(), BCrypt.gensalt(10));
        user.setPassword(hashedPassword);
        user.setUsername(userDTO.getUsername());
        user.setPhone(userDTO.getPhone());
        user.setAvatar("https://thumbs.dreamstime.com/b/default-avatar-profile-icon-vector-social-media-user-photo-183042379.jpg");
        user.setRole("USER");
        user.setStatus(true);
        user.setDob(userDTO.getDob());
        user.setGender(userDTO.getGender() != null ? userDTO.getGender() : false);

        try {
            boolean isRegistered = authService.register(user);
            if (isRegistered) {
                return "redirect:/login";
            } else {

                return "auth/register";
            }
        } catch (Exception e) {

            return "auth/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session, HttpServletResponse response) {
        session.invalidate();

        Cookie rememberMeCookie = new Cookie("remember-me", null);
        rememberMeCookie.setMaxAge(0);
        rememberMeCookie.setPath("/");
        response.addCookie(rememberMeCookie);

        return "redirect:/login";
    }
}