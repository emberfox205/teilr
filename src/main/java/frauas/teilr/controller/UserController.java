package frauas.teilr.controller;

import frauas.teilr.entity.User;
import frauas.teilr.service.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller // Spring: HTTP request and returns HTML
@CrossOrigin(origins = "*")
@RequestMapping("/api/users") // All endpoints in this class start with /users
@RequiredArgsConstructor // Lombok: generates a construtor that auto-injects userService
public class UserController {
    private final UserService userService;

    /**
     * HTMX: search for a user by @username.
     * Called by: hx-get="/users/search?username=alice"
     * Template:  templates/fragments/user-search-result.html
     */
    @GetMapping("/search")
    // GET  request -> /user/search
    // Full URL = /users (class) + /search (method) = GET /users/search
    public String searchUser(@RequestParam Long userId,
                             @RequestParam(defaultValue = "friend") String context,
                             Model model) {
        Optional<User> result = userService.findById(userId);
        model.addAttribute("user", result.orElse(null));
        // ← Puts the User object into the model with the key "user"
        // ← In the HTML: th:text="${user.username}" reads this
        // ← If not found → puts null (the template handles this case)
        model.addAttribute("notFound", result.isEmpty());
        // ← Puts true/false into the model with the key "notFound"
        // ← In the HTML: th:if="${notFound}" shows a "User not found" message
        // context drives which action button the result shows: "friend" | "newGroup"
        model.addAttribute("context", context);
        return "fragments/user-search-result :: searchResultContent";
        //      ↑ file path                      ↑ fragment name inside that file
        // Thymeleaf looks for: src/main/resources/templates/fragments/user-search-result.html
        // It finds the <div th:fragment="searchResultContent"> inside it
        // HTMX receives just that fragment (not the full page) and swaps it into the DOM
    }
}
