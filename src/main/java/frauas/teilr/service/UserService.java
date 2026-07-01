package frauas.teilr.service;

import frauas.teilr.dto.RegisterRequest;
import frauas.teilr.entity.User;
import frauas.teilr.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;

    /** Find a user by their 4-digit ID. */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Find a user by email. Used by {@code GlobalControllerAdvice} to map the
     * Spring Security principal (whose username is the email) back to a User.
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Register a new account from a validated sign-up form. The account starts
     * disabled and only becomes usable once the email is confirmed via
     * {@link #confirmEmail(String)}.
     */
    public User register(RegisterRequest request) {
        String username = request.getUsername() == null ? "" : request.getUsername().trim();
        String email = request.getEmail() == null ? "" : request.getEmail().trim();
        String rawPassword = request.getPassword();

        if (username.isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (email.isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("Password is required.");
        }

        if (userRepository.count() >= 10000) {
            throw new IllegalStateException("Maximum user capacity reached (10,000 users).");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email '" + email + "' is already taken.");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(BCrypt.hashpw(rawPassword, BCrypt.gensalt()));
        user.setVerificationToken(UUID.randomUUID().toString());
        user.setEnabled(false);
        user.setId(generateId());

        return userRepository.save(user);
    }

    /**
     * Confirm a pending registration. Returns the now-enabled user, or empty if
     * the token is unknown or already consumed.
     */
    public Optional<User> confirmEmail(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return userRepository.findByVerificationToken(token).map(user -> {
            user.setEnabled(true);
            // We intentionally do NOT set the token to null here.
            // Email prefetchers will "consume" the link before the user clicks it.
            // By keeping the token in the DB, when the user physically clicks the link a second later,
            // they still get a friendly "Success" message instead of a confusing "Invalid link" error!
            return userRepository.save(user);
        });
    }

    /**
     * Used by Spring Security's {@code DaoAuthenticationProvider}. The login form
     * accepts a 4-digit ID, an email, or a @username as the identifier.
     */
    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user = resolve(identifier)
                .orElseThrow(() -> new UsernameNotFoundException("No account found for '" + identifier + "'."));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .disabled(!user.isEnabled())
                .authorities("ROLE_USER")
                .build();
    }

    private Optional<User> resolve(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return Optional.empty();
        }
        String trimmed = identifier.trim();
        try {
            return userRepository.findById(Long.parseLong(trimmed));
        } catch (NumberFormatException ignored) {
            // Not numeric — fall back to email, then username.
        }
        Optional<User> byEmail = userRepository.findByEmail(trimmed);
        return byEmail.isPresent() ? byEmail : userRepository.findByUsername(trimmed);
    }

    /** Assign a free 4-digit ID (0000–9999). */
    private Long generateId() {
        // Fast path: try a few random guesses (O(1) for a sparse table).
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 3; i++) {
            Long guess = (long) random.nextInt(10000); // 0 to 9999
            if (!userRepository.existsById(guess)) {
                return guess;
            }
        }

        // Fallback: scan for the first available gap when the table is dense.
        if (!userRepository.existsById(0L)) {
            return 0L;
        }
        Long available = userRepository.findFirstAvailableId();
        if (available == null) {
            throw new IllegalStateException("Maximum user capacity reached (10,000 users).");
        }
        return available;
    }
}
