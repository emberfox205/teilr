package frauas.teilr.service;

import frauas.teilr.dto.RegisterRequest;
import frauas.teilr.entity.User;
import frauas.teilr.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MAX_ID_RETRIES = 20;

    /** Find a user by their 4-digit ID. */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Register a new user. Generates a random 4-digit ID (0000–9999) and a
     * verification token. The returned user is unverified until the token
     * link is opened.
     */
    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("An account with that email already exists.");
        }

        User user = new User();
        user.setId(generateUniqueId());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setEmailVerified(false);
        user.setVerificationToken(UUID.randomUUID().toString());
        return userRepository.save(user);
    }

    /**
     * Confirm a user's email via the token sent to their inbox.
     * Returns the verified user on success.
     */
    @Transactional
    public Optional<User> confirmEmail(String token) {
        return userRepository.findByVerificationToken(token).map(user -> {
            user.setEmailVerified(true);
            user.setVerificationToken(null);
            return userRepository.save(user);
        });
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No account with that username."));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .disabled(!user.isEmailVerified())
                .authorities("ROLE_USER")
                .build();
    }

    /** Generate a random unused 4-digit ID. Retries on rare collisions. */
    private Long generateUniqueId() {
        for (int i = 0; i < MAX_ID_RETRIES; i++) {
            long candidate = RANDOM.nextInt(10_000);
            if (!userRepository.existsById(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Could not allocate a free 4-digit user ID after " + MAX_ID_RETRIES + " tries.");
    }
}
