package frauas.teilr.config;

import frauas.teilr.entity.User;
import frauas.teilr.service.UserService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Bridges Spring Security and the session-based controllers.
 *
 * <p>Form login authenticates by email (see {@code UserService.loadUserByUsername}),
 * but every controller reads {@code session.getAttribute("userId")}. Nothing else
 * populates that attribute, so without this advice the first authenticated request
 * would always see {@code userId == null}.
 *
 * <p>This {@code @ModelAttribute} method runs before every controller handler. When
 * the request is authenticated it ensures {@code session.userId} is set and exposes
 * the resolved {@link User} as {@code currentUser} for templates (shell greeting,
 * profile page, etc.).
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final UserService userService;

    @ModelAttribute
    public void populateCurrentUser(HttpSession session, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(String.valueOf(auth.getPrincipal()))) {
            return;
        }

        Long userId = (Long) session.getAttribute("userId");
        User user;
        if (userId == null) {
            // auth.getName() is the email (the principal's username).
            user = userService.findByEmail(auth.getName()).orElse(null);
            if (user != null) {
                session.setAttribute("userId", user.getId());
            }
        } else {
            user = userService.findById(userId).orElse(null);
        }

        if (user != null) {
            model.addAttribute("currentUser", user);
        }
    }
}
