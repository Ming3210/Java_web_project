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

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

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
                        Model model,
                        RedirectAttributes redirectAttributes
                        ) {

        if (result.hasErrors()) {
            return "auth/login";
        }

        User loggedInUser = authService.login(loginDTO.getEmail(), loginDTO.getPassword());

        if (loggedInUser != null && loggedInUser.isStatus()) {
            session.setMaxInactiveInterval(3600);
            session.setAttribute("user", loggedInUser);

            if ("ADMIN".equalsIgnoreCase(loggedInUser.getRole())) {
                return "redirect:/admin/dashboard";
            } else {
                redirectAttributes.addFlashAttribute("successMessage", true);
                return "redirect:/";
            }
        } else {
            model.addAttribute("loginError", "Email hoặc mật khẩu không đúng");
            if (loggedInUser != null && !loggedInUser.isStatus()) {
                model.addAttribute("loginError", "Tài khoản của bạn đã bị khóa");
            }
            return "auth/login";
        }
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
}