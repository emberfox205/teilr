package frauas.teilr.controller;

import frauas.teilr.dto.RegisterRequest;
import frauas.teilr.entity.User;
import frauas.teilr.service.MailService;
import frauas.teilr.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final MailService mailService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            @RequestParam(required = false) String registered,
                            Model model) {
        if (error != null)      model.addAttribute("errorMessage", "Wrong username or password — or your email isn't verified yet.");
        if (logout != null)     model.addAttribute("infoMessage", "You've been logged out.");
        if (registered != null) model.addAttribute("infoMessage", "Check your inbox to confirm your account, then sign in.");
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("registerRequest")) {
            model.addAttribute("registerRequest", new RegisterRequest());
        }
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerRequest") RegisterRequest request,
                           BindingResult bindingResult,
                           Model model) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            User created = userService.register(request);
            mailService.sendVerificationEmail(created.getEmail(), created.getUsername(), created.getVerificationToken());
            model.addAttribute("email", created.getEmail());
            model.addAttribute("userId", String.format("%04d", created.getId()));
            return "verify-pending";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/verify")
    public String verify(@RequestParam String token, Model model) {
        Optional<User> verified = userService.confirmEmail(token);
        if (verified.isEmpty()) {
            model.addAttribute("errorMessage", "This verification link is invalid or has already been used.");
            return "verify-result";
        }
        model.addAttribute("username", verified.get().getUsername());
        model.addAttribute("userId", String.format("%04d", verified.get().getId()));
        return "verify-result";
    }
}
